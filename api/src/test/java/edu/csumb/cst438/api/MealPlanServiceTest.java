package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.*;
import edu.csumb.cst438.api.record.MealPlanEntryCreate;
import edu.csumb.cst438.api.record.MealPlanEntryPatch;
import edu.csumb.cst438.api.record.MealPlanEntryResponse;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.repository.MealPlanRepository;
import edu.csumb.cst438.api.repository.RecipeRepository;
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
import org.springframework.test.util.ReflectionTestUtils;
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

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private MealPlanService mealPlanService;

    private User owner;

    @BeforeEach
    void setUp() {
        owner = new User(EMAIL, "Owner", "USER");
        ReflectionTestUtils.setField(owner, "id", 1L);
    }

    private static User otherUser() {
        User other = new User("other@example.com", "Other", "USER");
        ReflectionTestUtils.setField(other, "id", 2L);
        return other;
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
        assertStatus(() -> mealPlanService.updateEntry(EMAIL, 7L, 5L,
                new MealPlanEntryPatch(null, null, mealType.LUNCH)), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.deleteEntry(EMAIL, 7L, 5L), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.listEntries(EMAIL, 7L), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.addEntry(EMAIL, 7L,
                new MealPlanEntryCreate(9L, WEEK, mealType.LUNCH)), HttpStatus.NOT_FOUND);
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
     * Updating the week keeps the existing entries and moves them to the new week
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
        assertThat(updated.entries().getFirst().plannedDate()).isEqualTo(LocalDate.of(2026, 10, 12));
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

    private MealPlanEntry ownedEntry(MealPlan mealPlan, Recipe recipe) {
        MealPlanEntry entry = new MealPlanEntry(mealPlan, recipe, WEEK, mealType.DINNER);
        ReflectionTestUtils.setField(entry, "id", 5L);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));
        return entry;
    }

    /**
     * Patching an entry changes only the fields that were sent
     */
    @Test
    void updateEntryChangesOnlySentFields() {
        signedIn();
        ownedEntry(new MealPlan(owner, WEEK), new Recipe("Pizza", null, "Eat it", owner));

        MealPlanEntryResponse updated = mealPlanService.updateEntry(EMAIL, 1L, 5L,
                new MealPlanEntryPatch(null, null, mealType.LUNCH));

        assertThat(updated.mealType()).isEqualTo(mealType.LUNCH);
        assertThat(updated.plannedDate()).isEqualTo(WEEK);
    }

    /**
     * Changing the recipe moves the entry from the old recipe to the new one
     */
    @Test
    void updateEntryMovesToPrepopulatedRecipe() {
        signedIn();
        Recipe pizza = new Recipe("Pizza", null, "Eat it", owner);
        Recipe soup = new Recipe("Soup", null, "Heat it", null);
        MealPlanEntry entry = ownedEntry(new MealPlan(owner, WEEK), pizza);
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(soup));

        mealPlanService.updateEntry(EMAIL, 1L, 5L,
                new MealPlanEntryPatch(9L, WEEK.plusDays(1), null));

        assertThat(entry.getRecipe()).isSameAs(soup);
        assertThat(entry.getPlannedDate()).isEqualTo(WEEK.plusDays(1));
        assertThat(entry.getMealType()).isEqualTo(mealType.DINNER);
        assertThat(pizza.getMealPlanEntries()).isEmpty();
        assertThat(soup.getMealPlanEntries()).containsExactly(entry);
    }

    /**
     * Another user's recipe and a missing recipe are both 404, and the entry is unchanged
     */
    @Test
    void updateEntryRejectsOthersRecipe() {
        signedIn();
        Recipe pizza = new Recipe("Pizza", null, "Eat it", owner);
        MealPlanEntry entry = ownedEntry(new MealPlan(owner, WEEK), pizza);
        Recipe othersRecipe = new Recipe("Secret", null, "Shh", otherUser());
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(othersRecipe));
        when(recipeRepository.findById(10L)).thenReturn(Optional.empty());

        assertStatus(() -> mealPlanService.updateEntry(EMAIL, 1L, 5L,
                new MealPlanEntryPatch(9L, null, null)), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.updateEntry(EMAIL, 1L, 5L,
                new MealPlanEntryPatch(10L, null, null)), HttpStatus.NOT_FOUND);
        assertThat(entry.getRecipe()).isSameAs(pizza);
    }

    /**
     * An entry id that isn't in the meal plan is 404 for update and delete
     */
    @Test
    void entryNotInMealPlanIsNotFound() {
        signedIn();
        ownedEntry(new MealPlan(owner, WEEK), new Recipe("Pizza", null, "Eat it", owner));

        assertStatus(() -> mealPlanService.updateEntry(EMAIL, 1L, 6L,
                new MealPlanEntryPatch(null, null, mealType.LUNCH)), HttpStatus.NOT_FOUND);
        assertStatus(() -> mealPlanService.deleteEntry(EMAIL, 1L, 6L), HttpStatus.NOT_FOUND);
    }

    /**
     * Deleting an entry removes it from the meal plan and the recipe, but keeps the recipe
     */
    @Test
    void deleteEntryKeepsRecipe() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        Recipe pizza = new Recipe("Pizza", null, "Eat it", owner);
        ownedEntry(mealPlan, pizza);

        mealPlanService.deleteEntry(EMAIL, 1L, 5L);

        assertThat(mealPlan.getMealPlanEntries()).isEmpty();
        assertThat(pizza.getMealPlanEntries()).isEmpty();
        assertThat(owner.getRecipes()).containsExactly(pizza);
    }

    /**
     * A meal plan's week is weekStart through the 6 days after it
     */
    @Test
    void weekCoversSevenDays() {
        MealPlan mealPlan = new MealPlan(owner, WEEK);

        assertThat(mealPlan.coversDate(WEEK)).isTrue();
        assertThat(mealPlan.coversDate(WEEK.plusDays(6))).isTrue();
        assertThat(mealPlan.coversDate(WEEK.minusDays(1))).isFalse();
        assertThat(mealPlan.coversDate(WEEK.plusDays(7))).isFalse();
    }

    /**
     * Adding an entry links it to both the meal plan and the recipe
     */
    @Test
    void addEntryLinksPlanAndRecipe() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        Recipe soup = new Recipe("Soup", null, "Heat it", null);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(soup));

        MealPlanEntryResponse created = mealPlanService.addEntry(EMAIL, 1L,
                new MealPlanEntryCreate(9L, WEEK.plusDays(2), mealType.LUNCH));

        assertThat(created.plannedDate()).isEqualTo(WEEK.plusDays(2));
        assertThat(created.mealType()).isEqualTo(mealType.LUNCH);
        assertThat(mealPlan.getMealPlanEntries()).singleElement()
                .satisfies(e -> assertThat(e.getRecipe()).isSameAs(soup));
        assertThat(soup.getMealPlanEntries()).hasSize(1);
    }

    /**
     * The user's own recipe is accepted even when it's loaded as a different User instance
     */
    @Test
    void addEntryAcceptsOwnRecipeWithDifferentOwnerInstance() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        User sameOwner = new User(EMAIL, "Owner", "USER");
        ReflectionTestUtils.setField(sameOwner, "id", 1L);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(new Recipe("Pizza", null, "Eat it", sameOwner)));

        mealPlanService.addEntry(EMAIL, 1L, new MealPlanEntryCreate(9L, WEEK, mealType.LUNCH));

        assertThat(mealPlan.getMealPlanEntries()).hasSize(1);
    }

    /**
     * Another user's recipe can't be added, and nothing is added to the plan
     */
    @Test
    void addEntryRejectsOthersRecipe() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        when(mealPlanRepository.findByIdAndOwner(1L, owner)).thenReturn(Optional.of(mealPlan));
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(
                new Recipe("Secret", null, "Shh", otherUser())));

        assertStatus(() -> mealPlanService.addEntry(EMAIL, 1L,
                new MealPlanEntryCreate(9L, WEEK, mealType.LUNCH)), HttpStatus.NOT_FOUND);
        assertThat(mealPlan.getMealPlanEntries()).isEmpty();
    }

    /**
     * A date outside the meal plan's week is rejected when adding or changing an entry
     */
    @Test
    void dateOutsideWeekIsRejected() {
        signedIn();
        MealPlan mealPlan = new MealPlan(owner, WEEK);
        Recipe pizza = new Recipe("Pizza", null, "Eat it", owner);
        MealPlanEntry entry = ownedEntry(mealPlan, pizza);
        when(recipeRepository.findById(9L)).thenReturn(Optional.of(pizza));

        assertThatThrownBy(() -> mealPlanService.addEntry(EMAIL, 1L,
                new MealPlanEntryCreate(9L, WEEK.plusDays(7), mealType.LUNCH)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> mealPlanService.updateEntry(EMAIL, 1L, 5L,
                new MealPlanEntryPatch(null, WEEK.minusDays(1), null)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(mealPlan.getMealPlanEntries()).containsExactly(entry);
        assertThat(entry.getPlannedDate()).isEqualTo(WEEK);
    }

    /**
     * Listing entries returns the meal plan's entries
     */
    @Test
    void listEntriesReturnsPlanEntries() {
        signedIn();
        ownedEntry(new MealPlan(owner, WEEK), new Recipe("Pizza", null, "Eat it", owner));

        assertThat(mealPlanService.listEntries(EMAIL, 1L)).singleElement()
                .satisfies(e -> assertThat(e.id()).isEqualTo(5L));
    }
}
