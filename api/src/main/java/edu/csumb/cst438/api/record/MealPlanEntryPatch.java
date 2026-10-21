package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.mealType;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

/**
 * Body for PATCH /api/v1/meal-plans/{mealPlanId}/entries/{entryId}.
 * Only the non-null fields are changed. An unknown mealType fails JSON parsing, which is a 400.
 *
 * @param recipeId the new recipe
 * @param plannedDate the new planned date
 * @param mealType the new meal type
 */
public record MealPlanEntryPatch(Long recipeId, LocalDate plannedDate, mealType mealType) {

    /**
     * The contract requires at least one property (minProperties: 1).
     *
     * @return true if at least one field was sent
     */
    @AssertTrue(message = "at least one field must be provided")
    public boolean isNotEmpty() {
        return recipeId != null || plannedDate != null || mealType != null;
    }
}
