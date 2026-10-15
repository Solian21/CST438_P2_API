package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.MealPlan;

import java.time.LocalDate;
import java.util.List;

/**
 * The MealPlan schema returned by the API.
 *
 * @param id the meal plan id
 * @param weekStart the week the meal plan starts
 * @param entries the meals planned in this meal plan
 */
public record MealPlanResponse(Long id, LocalDate weekStart, List<MealPlanEntryResponse> entries) {

    /**
     * Converts a meal plan entity to its API shape.
     *
     * @param mealPlan the meal plan entity
     * @return the API representation
     */
    public static MealPlanResponse from(MealPlan mealPlan) {
        return new MealPlanResponse(
                mealPlan.getId(),
                mealPlan.getWeekStart(),
                mealPlan.getMealPlanEntries().stream().map(MealPlanEntryResponse::from).toList());
    }
}
