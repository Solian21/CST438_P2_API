package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body for PATCH /api/v1/meal-plans/{mealPlanId}.
 * The contract requires at least one property, and weekStart is the only one,
 * so it is required here. Replace @NotNull with an "at least one non-null field" check
 * if more patchable fields are added.
 *
 * @param weekStart the new week the meal plan starts
 */
public record MealPlanPatch(@NotNull LocalDate weekStart) {
}
