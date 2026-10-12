package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.MealPlan;
import edu.csumb.cst438.api.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import static org.assertj.core.api.Assertions.assertThat;
import java.time.LocalDate;

@DataJpaTest
@ActiveProfiles("test")
public class MealPlanPersistenceTest {
    @Autowired
    private EntityManager entityManager;

    /**
     * Tests if there is persistence between the user and meal plan
     */
    @Test
    void mealPlanReload(){
        User owner = new User("owner@example.com", "Owner", "USER");
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 10));
        entityManager.persist(owner);
        entityManager.flush();
        Long mealId = mealPlan.getId();
        entityManager.clear();

        MealPlan reload = entityManager.find(MealPlan.class, mealId);

        assertThat(reload.getOwner().getEmail()).isEqualTo("owner@example.com");
        assertThat(reload.getWeekStart()).isEqualTo(LocalDate.of(2026, 10, 10));
    }

    /**
     * Tests if deletion of a user also deletes a meal plan connected to user
     */
    @Test
    void mealPlanUserDelete(){
        User owner = new User("owner@example.com", "Owner", "USER");
        MealPlan mealPlan = new MealPlan(owner, LocalDate.of(2026, 10, 10));
        entityManager.persist(owner);
        entityManager.flush();

        entityManager.remove(owner);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(MealPlan.class, mealPlan.getId())).isNull();
    }
}
