package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.MealPlan;
import edu.csumb.cst438.api.entity.MealPlanEntry;
import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.MealPlanEntryPatch;
import edu.csumb.cst438.api.record.MealPlanEntryResponse;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.repository.MealPlanRepository;
import edu.csumb.cst438.api.repository.RecipeRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

/**
 * Business logic for the signed-in user's meal plans.
 * A meal plan that doesn't exist and one owned by another user both return 404,
 * so the API never reveals that someone else's meal plan exists.
 */
@Service
@Transactional
public class MealPlanService {
    private final MealPlanRepository mealPlanRepository;
    private final RecipeRepository recipeRepository;

    /**
     * Constructor for MealPlanService
     * @param mealPlanRepository the meal plan repository
     * @param recipeRepository the recipe repository, for changing an entry's recipe
     */
    public MealPlanService(MealPlanRepository mealPlanRepository, RecipeRepository recipeRepository) {
        this.mealPlanRepository = mealPlanRepository;
        this.recipeRepository = recipeRepository;
    }

    /**
     * Lists one page of the user's meal plans, ordered by week start.
     *
     * @param email the signed-in user's email
     * @param page zero-based page number
     * @param size items per page
     * @return a page of meal plans with their entries
     */
    @Transactional(readOnly = true)
    public Page<MealPlanResponse> list(String email, int page, int size) {
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by("weekStart", "id"));
        return mealPlanRepository.findByOwner(currentUser(email), pageRequest).map(MealPlanResponse::from);
    }

    /**
     * Creates an empty meal plan owned by the user.
     *
     * @param email the signed-in user's email
     * @param weekStart the week the meal plan starts
     * @return the new meal plan
     */
    public MealPlanResponse create(String email, LocalDate weekStart) {
        MealPlan mealPlan = new MealPlan(currentUser(email), weekStart);
        return MealPlanResponse.from(mealPlanRepository.save(mealPlan));
    }

    /**
     * Gets one of the user's meal plans.
     *
     * @param email the signed-in user's email
     * @param mealPlanId the meal plan id
     * @return the meal plan with its entries
     * @throws ResponseStatusException 404 if missing or owned by another user
     */
    @Transactional(readOnly = true)
    public MealPlanResponse get(String email, Long mealPlanId) {
        return MealPlanResponse.from(findOwned(currentUser(email), mealPlanId));
    }

    /**
     * Changes the week of one of the user's meal plans. Entries are left unchanged.
     *
     * @param email the signed-in user's email
     * @param mealPlanId the meal plan id
     * @param weekStart the new week start
     * @return the updated meal plan
     * @throws ResponseStatusException 404 if missing or owned by another user
     */
    public MealPlanResponse update(String email, Long mealPlanId, LocalDate weekStart) {
        MealPlan mealPlan = findOwned(currentUser(email), mealPlanId);
        mealPlan.changeWeekStart(weekStart);
        return MealPlanResponse.from(mealPlan);
    }

    /**
     * Deletes one of the user's meal plans and all of its entries. Recipes are kept.
     *
     * @param email the signed-in user's email
     * @param mealPlanId the meal plan id
     * @throws ResponseStatusException 404 if missing or owned by another user
     */
    public void delete(String email, Long mealPlanId) {
        User owner = currentUser(email);
        // Removing through the owner (orphanRemoval) instead of repository.delete(), so User's
        // cascade = ALL collection can't re-save the deleted plan on flush.
        owner.removeMealPlan(findOwned(owner, mealPlanId));
    }

    /**
     * Changes the fields that were sent on one entry of the user's meal plan.
     *
     * @param email the signed-in user's email
     * @param mealPlanId the meal plan id
     * @param entryId the entry id
     * @param patch the fields to change; null fields are left as they are
     * @return the updated entry
     * @throws ResponseStatusException 404 if the meal plan, entry, or recipe isn't the user's
     */
    public MealPlanEntryResponse updateEntry(String email, Long mealPlanId, Long entryId, MealPlanEntryPatch patch) {
        User owner = currentUser(email);
        MealPlanEntry entry = findEntry(findOwned(owner, mealPlanId), entryId);
        if (patch.recipeId() != null) {
            entry.changeRecipe(findUsableRecipe(owner, patch.recipeId()));
        }
        if (patch.plannedDate() != null) {
            entry.changePlannedDate(patch.plannedDate());
        }
        if (patch.mealType() != null) {
            entry.changeMealType(patch.mealType());
        }
        return MealPlanEntryResponse.from(entry);
    }

    /**
     * Removes one entry from the user's meal plan. The recipe is kept.
     *
     * @param email the signed-in user's email
     * @param mealPlanId the meal plan id
     * @param entryId the entry id
     * @throws ResponseStatusException 404 if the meal plan or entry isn't the user's
     */
    public void deleteEntry(String email, Long mealPlanId, Long entryId) {
        MealPlan mealPlan = findOwned(currentUser(email), mealPlanId);
        MealPlanEntry entry = findEntry(mealPlan, entryId);
        entry.getRecipe().removeMealPlanEntry(entry);
        // orphanRemoval deletes the row
        mealPlan.removeMealPlanEntry(entry);
    }

    // TODO(#48): replace with userService.getUserByEmail(email); see MealPlanRepository.findUserByEmail
    private User currentUser(String email) {
        return mealPlanRepository.findUserByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED));
    }

    private MealPlan findOwned(User owner, Long mealPlanId) {
        return mealPlanRepository.findByIdAndOwner(mealPlanId, owner)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meal plan " + mealPlanId + " not found"));
    }

    private MealPlanEntry findEntry(MealPlan mealPlan, Long entryId) {
        return mealPlan.getMealPlanEntries().stream()
                .filter(e -> entryId.equals(e.getId()))
                .findFirst()
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Meal plan entry " + entryId + " not found"));
    }

    // A user can plan their own recipes and the prepopulated ones; anything else is hidden as 404.
    private Recipe findUsableRecipe(User owner, Long recipeId) {
        return recipeRepository.findById(recipeId)
                .filter(r -> r.isPrepopulated() || r.getOwner() == owner)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Recipe " + recipeId + " not found"));
    }
}
