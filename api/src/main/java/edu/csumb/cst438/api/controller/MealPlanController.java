package edu.csumb.cst438.api.controller;

import edu.csumb.cst438.api.record.MealPlanCreate;
import edu.csumb.cst438.api.record.MealPlanEntryCreate;
import edu.csumb.cst438.api.record.MealPlanEntryPatch;
import edu.csumb.cst438.api.record.MealPlanEntryResponse;
import edu.csumb.cst438.api.record.MealPlanPatch;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.service.MealPlanService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.security.Principal;
import java.util.List;

/**
 * API routes for the signed-in user's weekly meal plans, under /api/v1/meal-plans.
 * Principal.getName() is the user's email.
 *
 * TODO(#48): Spring Security comes with PR #48. Until it merges there is no Principal, so these
 *  routes only work in tests. 401 (as Problem Details) is handled by #48's SecurityConfig.
 */
@RestController
@RequestMapping("/api/v1/meal-plans")
public class MealPlanController {
    private final MealPlanService mealPlanService;

    /**
     * Constructor for MealPlanController
     * @param mealPlanService the meal plan service
     */
    public MealPlanController(MealPlanService mealPlanService) {
        this.mealPlanService = mealPlanService;
    }

    /**
     * Lists the user's meal plans. Uses plain request params instead of Pageable so that
     * size above 100 returns 400 instead of being silently capped.
     *
     * @param principal the signed-in user
     * @param page zero-based page number
     * @param size items per page, 1 to 100
     * @return 200 with a page of meal plans
     */
    @GetMapping
    public PagedModel<MealPlanResponse> listMealPlans(Principal principal,
                                                      @RequestParam(defaultValue = "0") @Min(0) int page,
                                                      @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return new PagedModel<>(mealPlanService.list(principal.getName(), page, size));
    }

    /**
     * Creates an empty meal plan.
     *
     * @param principal the signed-in user
     * @param body the week the plan starts
     * @return 201 with the new meal plan and its Location
     */
    @PostMapping
    public ResponseEntity<MealPlanResponse> createMealPlan(Principal principal, @Valid @RequestBody MealPlanCreate body) {
        MealPlanResponse created = mealPlanService.create(principal.getName(), body.weekStart());
        return ResponseEntity.created(URI.create("/api/v1/meal-plans/" + created.id())).body(created);
    }

    /**
     * Gets one meal plan and its entries.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @return 200 with the meal plan
     */
    @GetMapping("/{mealPlanId}")
    public MealPlanResponse getMealPlan(Principal principal, @PathVariable Long mealPlanId) {
        return mealPlanService.get(principal.getName(), mealPlanId);
    }

    /**
     * Changes a meal plan's week start.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @param body the fields to change
     * @return 200 with the updated meal plan
     */
    @PatchMapping("/{mealPlanId}")
    public MealPlanResponse updateMealPlan(Principal principal, @PathVariable Long mealPlanId,
                                           @Valid @RequestBody MealPlanPatch body) {
        return mealPlanService.update(principal.getName(), mealPlanId, body.weekStart());
    }

    /**
     * Deletes a meal plan and its entries.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @return 204 with no body
     */
    @DeleteMapping("/{mealPlanId}")
    public ResponseEntity<Void> deleteMealPlan(Principal principal, @PathVariable Long mealPlanId) {
        mealPlanService.delete(principal.getName(), mealPlanId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Lists the planned meals in a meal plan.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @return 200 with the entries
     */
    @GetMapping("/{mealPlanId}/entries")
    public List<MealPlanEntryResponse> listMealPlanEntries(Principal principal, @PathVariable Long mealPlanId) {
        return mealPlanService.listEntries(principal.getName(), mealPlanId);
    }

    /**
     * Adds a recipe to a meal plan.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @param body the recipe, date, and meal type
     * @return 201 with the new entry and its Location
     */
    @PostMapping("/{mealPlanId}/entries")
    public ResponseEntity<MealPlanEntryResponse> addMealPlanEntry(Principal principal, @PathVariable Long mealPlanId,
                                                                  @Valid @RequestBody MealPlanEntryCreate body) {
        MealPlanEntryResponse created = mealPlanService.addEntry(principal.getName(), mealPlanId, body);
        return ResponseEntity.created(
                URI.create("/api/v1/meal-plans/" + mealPlanId + "/entries/" + created.id())).body(created);
    }

    /**
     * Changes the fields that were sent on one planned meal.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @param entryId the entry id
     * @param body the fields to change, at least one
     * @return 200 with the updated entry
     */
    @PatchMapping("/{mealPlanId}/entries/{entryId}")
    public MealPlanEntryResponse updateMealPlanEntry(Principal principal, @PathVariable Long mealPlanId,
                                                     @PathVariable Long entryId,
                                                     @Valid @RequestBody MealPlanEntryPatch body) {
        return mealPlanService.updateEntry(principal.getName(), mealPlanId, entryId, body);
    }

    /**
     * Removes one planned meal. The recipe is kept.
     *
     * @param principal the signed-in user
     * @param mealPlanId the meal plan id
     * @param entryId the entry id
     * @return 204 with no body
     */
    @DeleteMapping("/{mealPlanId}/entries/{entryId}")
    public ResponseEntity<Void> deleteMealPlanEntry(Principal principal, @PathVariable Long mealPlanId,
                                                    @PathVariable Long entryId) {
        mealPlanService.deleteEntry(principal.getName(), mealPlanId, entryId);
        return ResponseEntity.noContent().build();
    }
}
