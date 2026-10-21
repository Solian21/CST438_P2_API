package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.mealType;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body for POST /api/v1/meal-plans/{mealPlanId}/entries.
 *
 * @param recipeId the recipe to plan
 * @param plannedDate the date the meal is planned for, within the meal plan's week
 * @param mealType breakfast, lunch, or dinner
 */
public record MealPlanEntryCreate(@NotNull Long recipeId, @NotNull LocalDate plannedDate, @NotNull mealType mealType) {
}
