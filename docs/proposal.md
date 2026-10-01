# <API name> Proposal

## 1. The pitch (one paragraph)
The Recipe and Meal Planner API allows users of our app to create, organize, and manage recipes and meal plans. Users can search through existing recipes as well as existing ingredients. The API stores recipes with their ingredients and cooking instructions. The Android app uses this API so users can save recipes, view their ingredients and instructions, and organize them into meal plans.

## 2. Resources
| Resource | Key fields | Relationships |
|---|---|---|
| Users | id, email, displayName, role | A User can own many Recipes and MealPlans. An ADMIN User can manage other Users. |
| Recipes | id, name, measurements, instructions | A Recipe can use many Ingredients and can appear in many MealPlanEntries. |
| Ingredients | id, ingredientName | An Ingredient can be used by many Recipes. |
| MealPlans | id, weekStart | A User owns many MealPlans. A MealPlan has many MealPlanEntries. |
| MealPlanEntries | id, mealPlanId, recipeId, plannedDate, mealType | Each entry belongs to one MealPlan and references one Recipe. |

## 3. ER sketch
Tables, primary and foreign keys, and cardinality. Edit this Mermaid diagram (it renders on GitHub;
try changes at https://mermaid.live):

```mermaid
erDiagram
    USER ||--o{ MEAL_PLAN : may_own
    USER o|--o{ RECIPE : may_own

    RECIPE ||--o{ RECIPE_INGREDIENT : contains
    INGREDIENT ||--o{ RECIPE_INGREDIENT : used_in

    MEAL_PLAN ||--o{ MEAL_PLAN_ENTRY : contains
    RECIPE ||--o{ MEAL_PLAN_ENTRY : scheduled_in

    USER {
        bigint id PK
        string email UK
        string display_name
        string role
    }

    RECIPE {
        bigint id PK
        bigint user_id FK "nullable for shared recipes"
        string name
        datetime created_at
        string measurements "nullable"
        string instructions "nullable"
    }

    INGREDIENT {
        bigint id PK
        string ingredient_name
    }

    RECIPE_INGREDIENT {
        bigint recipe_id PK, FK
        bigint ingredient_id PK, FK
    }

    MEAL_PLAN {
        bigint id PK
        bigint user_id FK
        date week_start
    }

    MEAL_PLAN_ENTRY {
        bigint id PK
        bigint meal_plan_id FK
        bigint recipe_id FK
        date planned_date
        string meal_type
    }
```

## 4. Endpoints
| Verb | Path | Auth | Purpose |
|---|---|---|---|
| GET | /api/v1/workouts?page=0&size=20 | user | list my workouts (paginated) |
| ... | ... | ... | ... |
Mark each endpoint `public`, `user`, or `admin`. Mark which collection paginates and which
filters or sorts.

## 5. Technical choices
- **Database host:** (Neon, Supabase, Railway, Atlas, ...) and why
- **OAuth2 provider:** (Google, GitHub, Auth0) and confirmation that it supports Authorization Code + PKCE from a native app
- **Repo layout:** monorepo or split, and why
These become your ADRs later.

## 6. Risks
- One risk is that deleting or changing one of the objects could affect how another object acts, like deleting an ingredient in recipes. What we will do first to find out is to test how the API handles deletions or changes.
- Another risk is that a user could try to edit or view another user's mealplan or edit a lot of things on the API at once. What we could do to find out is to test what are the limits of a single user and figure out a workaround if an issue shows up.

## 7. Team and Sprint 1
Who owns what in Sprint 1. Link your Project board and Sprint 1 milestone.
