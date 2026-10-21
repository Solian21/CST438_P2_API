package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.*;
import edu.csumb.cst438.api.record.MealPlanEntryPatch;
import edu.csumb.cst438.api.service.MealPlanService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Runs MealPlanService against a real database to check the queries and cascades.
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(MealPlanService.class)
public class MealPlanServiceJpaTest {
    @Autowired
    private EntityManager entityManager;

    @Autowired
    private MealPlanService mealPlanService;

    /**
     * Deleting a meal plan deletes its entries but keeps the recipes
     */
    @Test
    void deleteKeepsRecipes() {
        User owner = new User("owner@example.com", "Owner", "USER");
        entityManager.persist(owner);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 5));
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        entityManager.persist(mealPlan);
        entityManager.persist(recipe);
        MealPlanEntry entry = new MealPlanEntry(mealPlan, recipe, LocalDate.of(2026, 10, 6), mealType.DINNER);
        entityManager.flush();
        entityManager.clear();

        mealPlanService.delete("owner@example.com", mealPlan.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MealPlan.class, mealPlan.getId())).isNull();
        assertThat(entityManager.find(MealPlanEntry.class, entry.getId())).isNull();
        assertThat(entityManager.find(Recipe.class, recipe.getId())).isNotNull();
    }

    /**
     * Another user's meal plan is not found, and only the user's own plans are listed
     */
    @Test
    void otherUsersPlanIsHidden() {
        User owner = new User("owner@example.com", "Owner", "USER");
        User other = new User("other@example.com", "Other", "USER");
        entityManager.persist(owner);
        entityManager.persist(other);
        MealPlan othersPlan = new MealPlan(other, LocalDate.of(2026, 10, 5));
        entityManager.persist(othersPlan);
        entityManager.persist(new MealPlan(owner, LocalDate.of(2026, 10, 12)));
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> mealPlanService.get("owner@example.com", othersPlan.getId()))
                .isInstanceOf(ResponseStatusException.class);
        assertThat(mealPlanService.list("owner@example.com", 0, 20).getContent())
                .singleElement()
                .satisfies(p -> assertThat(p.weekStart()).isEqualTo(LocalDate.of(2026, 10, 12)));
    }

    /**
     * Deleting an entry deletes only that row and keeps the recipe
     */
    @Test
    void deleteEntryKeepsRecipe() {
        User owner = new User("owner@example.com", "Owner", "USER");
        entityManager.persist(owner);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 5));
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        entityManager.persist(mealPlan);
        entityManager.persist(recipe);
        MealPlanEntry entry = new MealPlanEntry(mealPlan, recipe, LocalDate.of(2026, 10, 6), mealType.DINNER);
        entityManager.flush();
        entityManager.clear();

        mealPlanService.deleteEntry("owner@example.com", mealPlan.getId(), entry.getId());
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MealPlanEntry.class, entry.getId())).isNull();
        assertThat(entityManager.find(MealPlan.class, mealPlan.getId())).isNotNull();
        assertThat(entityManager.find(Recipe.class, recipe.getId())).isNotNull();
    }

    /**
     * An entry can be moved to the user's own recipe, but another user's recipe is not found
     */
    @Test
    void updateEntryOnlyUsesOwnRecipes() {
        User owner = new User("owner@example.com", "Owner", "USER");
        User other = new User("other@example.com", "Other", "USER");
        entityManager.persist(owner);
        entityManager.persist(other);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 5));
        Recipe pizza = new Recipe("Pizza", null, "Eat it", owner);
        Recipe soup = new Recipe("Soup", null, "Heat it", owner);
        Recipe secret = new Recipe("Secret", null, "Shh", other);
        entityManager.persist(mealPlan);
        entityManager.persist(pizza);
        entityManager.persist(soup);
        entityManager.persist(secret);
        MealPlanEntry entry = new MealPlanEntry(mealPlan, pizza, LocalDate.of(2026, 10, 6), mealType.DINNER);
        entityManager.flush();
        entityManager.clear();

        assertThatThrownBy(() -> mealPlanService.updateEntry("owner@example.com", mealPlan.getId(), entry.getId(),
                new MealPlanEntryPatch(secret.getId(), null, null)))
                .isInstanceOf(ResponseStatusException.class);
        entityManager.clear();

        mealPlanService.updateEntry("owner@example.com", mealPlan.getId(), entry.getId(),
                new MealPlanEntryPatch(soup.getId(), null, mealType.LUNCH));
        entityManager.flush();
        entityManager.clear();

        MealPlanEntry saved = entityManager.find(MealPlanEntry.class, entry.getId());
        assertThat(saved.getRecipe().getId()).isEqualTo(soup.getId());
        assertThat(saved.getMealType()).isEqualTo(mealType.LUNCH);
    }
}
