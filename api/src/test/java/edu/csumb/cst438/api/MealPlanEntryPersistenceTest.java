package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.MealPlan;
import edu.csumb.cst438.api.entity.MealPlanEntry;
import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
public class MealPlanEntryPersistenceTest {
    @Autowired
    private EntityManager entityManager;

    /**
     * Tests if there is persistence between the user, recipe, meal plan, and entry
     */
    @Test
    void mealPlanEntryReload(){
        User owner = new User("owner@example.com", "Owner", "USER");
        entityManager.persist(owner);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 10));
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        entityManager.persist(mealPlan);
        entityManager.persist(recipe);

        MealPlanEntry mealPlanEntry = new MealPlanEntry(mealPlan, recipe, LocalDate.of(2026, 10, 31), "Dinner");
        entityManager.flush();
        Long entryId = mealPlanEntry.getId();
        entityManager.clear();

        MealPlanEntry reload = entityManager.find(MealPlanEntry.class, entryId);

        assertThat(reload.getMeal_plan().getOwner().getEmail().equals("owner@example.com"));
        assertThat(reload.getMeal_plan().getWeek_start().equals(LocalDate.of(2026, 10, 10)));
        assertThat(reload.getRecipe().getName().equals("Pizza"));
        assertThat(reload.getMeal_type().equals("Dinner"));
    }

    /**
     * Tests if deletion of a meal plan also deletes the entry
     */
    @Test
    void entryMPDelete(){
        User owner = new User("owner@example.com", "Owner", "USER");
        entityManager.persist(owner);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 10));
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        entityManager.persist(mealPlan);
        entityManager.persist(recipe);

        MealPlanEntry mealPlanEntry = new MealPlanEntry(mealPlan, recipe, LocalDate.of(2026, 10, 31), "Dinner");
        entityManager.flush();

        owner.getMealPlans().remove(mealPlan);
        entityManager.remove(mealPlan);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MealPlanEntry.class, mealPlanEntry.getId())).isNull();
    }

    /**
     * Tests if deletion of a recipe also deletes the meal plan entry
     */
    @Test
    void entryRecipeDelete(){
        User owner = new User("owner@example.com", "Owner", "USER");
        entityManager.persist(owner);
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 10));
        Recipe recipe = new Recipe("Pizza", null, "Eat it", owner);
        entityManager.persist(mealPlan);
        entityManager.persist(recipe);

        MealPlanEntry mealPlanEntry = new MealPlanEntry(mealPlan, recipe, LocalDate.of(2026, 10, 31), "Dinner");
        entityManager.flush();

        entityManager.remove(recipe);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MealPlanEntry.class, mealPlanEntry.getId())).isNull();
    }
}
