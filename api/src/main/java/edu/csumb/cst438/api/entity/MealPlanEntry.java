package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

/**
 * Represents an entry in a meal plan that a user can make, always has a recipe object in it
 */
@Entity
@Table(name="meal_plan_entry")
@Getter
public class MealPlanEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan mealPlan;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false, name = "planned_date")
    private LocalDate plannedDate;

    @Column(nullable = false, name = "meal_type")
    @Enumerated(EnumType.STRING)
    private mealType mealType;

    protected MealPlanEntry(){}

    /**
     * Creates a new meal plan entry
     *
     * @param meal_plan the meal plan this entry is added to
     * @param recipe the recipe this entry contains
     * @param planned_date the date the meal is planned to be made
     * @param meal_type the type of meal
     * @throws IllegalArgumentException if anything inputted is null
     */
    public MealPlanEntry(MealPlan meal_plan, Recipe recipe, LocalDate planned_date, mealType meal_type){
        if(meal_plan == null || recipe == null || planned_date == null || meal_type == null){
            throw new IllegalArgumentException("Meal plan entries must have a meal plan, recipe, planned date, and meal type.");
        }
        meal_plan.addMealPlanEntry(this);
        recipe.addMealPlanEntry(this);
        plannedDate = planned_date;
        mealType = meal_type;
    }

    /**
     * Moves this entry to a different recipe and keeps both recipes' entry collections in sync.
     *
     * @param recipe the new recipe, cannot be null
     * @throws IllegalArgumentException if recipe is null
     */
    public void changeRecipe(Recipe recipe){
        if(recipe == null){
            throw new IllegalArgumentException("Meal plan entries must have a recipe.");
        }
        this.recipe.removeMealPlanEntry(this);
        this.recipe = null;
        recipe.addMealPlanEntry(this);
    }

    /**
     * Changes the date the meal is planned for.
     *
     * @param plannedDate the new date, cannot be null
     * @throws IllegalArgumentException if plannedDate is null
     */
    public void changePlannedDate(LocalDate plannedDate){
        if(plannedDate == null){
            throw new IllegalArgumentException("Meal plan entries must have a planned date.");
        }
        this.plannedDate = plannedDate;
    }

    /**
     * Changes the type of meal.
     *
     * @param mealType the new meal type, cannot be null
     * @throws IllegalArgumentException if mealType is null
     */
    public void changeMealType(mealType mealType){
        if(mealType == null){
            throw new IllegalArgumentException("Meal plan entries must have a meal type.");
        }
        this.mealType = mealType;
    }

    /**
     * Called by MealPlan.addMealPlanEntry so the mealPlan side cannot get out of sync.
     */
    void setEntryFromMealPlan(MealPlan meal_plan){mealPlan = meal_plan;}

    /**
     * Called by Recipe.addMealPlanEntry so the recipe side cannot get out of sync.
     */
    void setEntryFromRecipe(Recipe recipe){this.recipe = recipe;}
}
