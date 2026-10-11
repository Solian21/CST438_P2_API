package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.Ingredient;
import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
class RecipePersistenceTest {

    @Autowired
    private EntityManager entityManager;

    @Test
    void savingAndReloadingRecipePreservesOwnerAndIngredients() {
        User owner = new User("owner@example.com", "Owner", "USER");
        Ingredient flour = new Ingredient("Flour");
        Ingredient salt = new Ingredient("Salt");
        entityManager.persist(flour);
        entityManager.persist(salt);

        Recipe recipe = new Recipe("Pancakes", "2 cups flour", "Mix and cook", owner);
        recipe.addIngredient(flour);
        recipe.addIngredient(salt);
        entityManager.persist(owner);
        entityManager.flush();
        Long recipeId = recipe.getId();
        entityManager.clear();

        Recipe reloaded = entityManager.find(Recipe.class, recipeId);

        assertThat(reloaded.getOwner().getEmail()).isEqualTo("owner@example.com");
        assertThat(reloaded.getMeasurements()).isEqualTo("2 cups flour");
        assertThat(reloaded.getIngredients())
                .extracting(Ingredient::getIngredientName)
                .containsExactlyInAnyOrder("Flour", "Salt");
    }

    @Test
    void deletingUserDeletesOwnedRecipesAndLinksButPreservesSharedData() {
        User owner = new User("owner@example.com", "Owner", "USER");
        Ingredient sharedIngredient = new Ingredient("Flour");
        Recipe ownedRecipe = new Recipe("Owned recipe", null, "Bake it", owner);
        ownedRecipe.addIngredient(sharedIngredient);
        Recipe prepopulatedRecipe = new Recipe("Shared recipe", null, "Serve it", null);
        prepopulatedRecipe.addIngredient(sharedIngredient);

        entityManager.persist(sharedIngredient);
        entityManager.persist(prepopulatedRecipe);
        entityManager.persist(owner);
        entityManager.flush();
        Long prepopulatedRecipeId = prepopulatedRecipe.getId();
        Long ingredientId = sharedIngredient.getId();

        entityManager.remove(owner);
        entityManager.flush();
        entityManager.clear();

        assertThat(entityManager.find(Recipe.class, ownedRecipe.getId())).isNull();
        assertThat(entityManager.find(Recipe.class, prepopulatedRecipeId)).isNotNull();
        assertThat(entityManager.find(Ingredient.class, ingredientId)).isNotNull();
        assertThat(joinRowCount()).isEqualTo(1L);
    }

    private long joinRowCount() {
        return ((Number) entityManager.createNativeQuery(
                "select count(*) from recipe_ingredients").getSingleResult()).longValue();
    }
}
