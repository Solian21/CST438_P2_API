package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.*;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.repository.MealPlanRepository;
import edu.csumb.cst438.api.service.MealPlanService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class MealPlanServiceTest {
    private static final String EMAIL = "owner@example.com";
    private static final LocalDate WEEK = LocalDate.of(2026, 10, 5);

    @Mock
    private MealPlanRepository mealPlanRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(EMAIL, "Owner", "USER");
    }

    private void signedIn() {
        when(mealPlanRepository.findUserByEmail(EMAIL)).thenReturn(Optional.of(owner));
    }

    private void assertStatus(Runnable call, HttpStatus status) {
        assertThatThrownBy(call::run)
                .isInstanceOfSatisfying(ResponseStatusException.class,
                        e -> assertThat(e.getStatusCode()).isEqualTo(status));
    }

    /**
     * A user that isn't in the database cannot use meal plans
     */
    @Test
    void unknownUserIsUnauthorized() {
        when(mealPlanRepository.findUserByEmail(EMAIL)).thenReturn(Optional.empty());

        assertStatus(() -> mealPlanService.get(EMAIL, 1L), HttpStatus.UNAUTHORIZED);
    }

    /**
     * Missing meal plans and other users' meal plans are both 404 for get, update, and delete
     */
    @Test
    void notOwnedMealPlanIsNotFound() {
        signedIn();
        when(mealPlanRepository.findByIdAndOwner(7L, owner)).thenReturn(Optional.empty());

        assertStatus(() -> mealPlanService.get(EMAIL, 7L), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.update(EMAIL, 7L, WEEK), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.delete(EMAIL, 7L), HttpStatus.NOT_FOUND);
    }

    /**
     * An empty result is still a page, with no content
     */
    @Test
    void listEmpty() {
        signedIn();
        when(mealPlanRepository.findByOwner(eq(owner), any(Pageable.class))).thenReturn(Page.empty());

        Page<MealPlanResponse> result = mealPlanService.list(EMAIL, 0, 20);

        assertThat(result.getContent()).isEmpty();
    }

    /**
     * A new meal plan belongs to the signed-in user and has no entries
     */
    @Test
    void createIsOwnedAndEmpty() {
        signedIn();
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(inv -> inv.getArgument(0));

        MealPlanResponse created = mealPlanService.create(EMAIL, WEEK);

        assertThat(created.weekStart()).isEqualTo(WEEK);
        assertThat(created.entries()).isEmpty();
        assertThat(owner.getMealPlans()).hasSize(1);
        assertThat(owner.getMealPlans().getFirst().getOwner()).isSameAs(owner);
    }

    /**
     * Updating the week keeps the existing entries
     */
    @Test
    void updateKeepsEntries() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        new MealPlanEntry(mealPlan, recipe, WEEK, mealType.DINNER);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));

        MealPlanResponse updated = mealPlanService.update(EMAIL, 1L, LocalDate.of(2026, 10, 12));

        assertThat(updated.weekStart()).isEqualTo(LocalDate.of(2026, 10, 12));
        assertThat(updated.entries()).hasSize(1);
        assertThat(updated.entries().getFirst().mealType()).isEqualTo(mealType.DINNER);
    }

    /**
     * Deleting removes the meal plan from its owner
     */
    @Test
    void deleteRemovesFromOwner() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));

        mealPlanService.delete(EMAIL, 1L);

        assertThat(owner.getMealPlans()).isEmpty();
        verify(mealPlanRepository).findByIdAndOwner(1L, owner);
    }
}
