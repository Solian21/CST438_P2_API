package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * Body for POST /api/v1/meal-plans.
 *
 * @param weekStart the week the meal plan starts
 */
public record MealPlanCreate(@NotNull LocalDate weekStart) {
}
