package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.MealPlanEntry;
import edu.csumb.cst438.api.entity.mealType;

import java.time.LocalDate;

/**
 * The MealPlanEntry schema returned by the API.
 *
 * @param id the entry id
 * @param mealPlanId the meal plan this entry belongs to
 * @param recipeId the planned recipe
 * @param plannedDate the date the meal is planned for
 * @param mealType breakfast, lunch, or dinner
 */
public record MealPlanEntryResponse(Long id, Long mealPlanId, Long recipeId, LocalDate plannedDate, mealType mealType) {

    /**
     * Converts an entry entity to its API shape.
     *
     * @param entry the entry entity
     * @return the API representation
     */
    public static MealPlanEntryResponse from(MealPlanEntry entry) {
        return new MealPlanEntryResponse(
                entry.getId(),
                entry.getMealPlan().getId(),
                entry.getRecipe().getId(),
                entry.getPlannedDate(),
                entry.getMealType());
    }
}
