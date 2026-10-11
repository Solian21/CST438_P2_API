package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;

@Entity
@Table(name="meal_plan_entry")
@Getter
public class MealPlanEntry {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "meal_plan_id", nullable = false)
    private MealPlan meal_plan;

    @ManyToOne(optional = false)
    @JoinColumn(name = "recipe_id", nullable = false)
    private Recipe recipe;

    @Column(nullable = false)
    private LocalDate planned_date;

    @Column(nullable = false)
    private String meal_type;

    protected MealPlanEntry(){}

    /**
     * Creates a new meal plan entry
     *
     * @param meal_plan the meal plan this entry is added to
     * @param recipe the recipe this entry contains
     * @param planned_date the date the meal is planned to be made
     * @param meal_type the type of meal
     * @throws IllegalArgumentException if the meal plan or recipe is null
     */
    public MealPlanEntry(MealPlan meal_plan, Recipe recipe, LocalDate planned_date, String meal_type){
        if(meal_plan == null || recipe == null){
            throw new IllegalArgumentException("Meal plan entries must have a meal plan and recipe.");
        }
        meal_plan.addMealPlanEntry(this);
        recipe.addMealPlanEntry(this);
        this.planned_date = planned_date;
        this.meal_type = meal_type;
    }

    /**
     * Called by MealPlan.addMealPlanEntry so the mealPlan side cannot get out of sync.
     */
    void setEntryFromMealPlan(MealPlan meal_plan){this.meal_plan = meal_plan;}

    /**
     * Called by Recipe.addRecipeEntry so the recipe side cannot get out of sync.
     */
    void setEntryFromRecipe(Recipe recipe){this.recipe = recipe;}
}
