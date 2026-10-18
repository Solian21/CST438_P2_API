package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.MealPlan;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.repository.MealPlanRepository;
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

    /**
     * Constructor for MealPlanService
     * @param mealPlanRepository the meal plan repository
     */
    public MealPlanService(MealPlanRepository mealPlanRepository) {
        this.mealPlanRepository = mealPlanRepository;
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
}
