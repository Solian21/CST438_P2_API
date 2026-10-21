package edu.csumb.cst438.api.service;

import tools.jackson.databind.node.JsonNodeFactory;
import edu.csumb.cst438.api.entity.Ingredient;
import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.RecipeCreate;
import edu.csumb.cst438.api.record.RecipePatch;
import edu.csumb.cst438.api.record.RecipeResponse;
import edu.csumb.cst438.api.repository.IngredientRepository;
import edu.csumb.cst438.api.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RecipeServiceTest {

    private RecipeRepository recipeRepository;
    private IngredientRepository ingredientRepository;
    private RecipeService recipeService;

    private User owner;
    private User otherUser;
    private Ingredient flour;
    private Ingredient sugar;
    private Recipe recipe;

    @BeforeEach
    void setUp() throws Exception {
        recipeRepository = mock(RecipeRepository.class);
        ingredientRepository = mock(IngredientRepository.class);

        recipeService = new RecipeService(
                recipeRepository,
                ingredientRepository
        );

        owner = new User(
                "owner@example.com",
                "Owner",
                "USER"
        );

        otherUser = new User(
                "other@example.com",
                "Other User",
                "USER"
        );

        setId(owner, 1L);
        setId(otherUser, 2L);

        flour = new Ingredient("Flour");
        sugar = new Ingredient("Sugar");

        setId(flour, 1L);
        setId(sugar, 2L);

        recipe = new Recipe(
                "Pancakes",
                "2 cups",
                "Mix and cook.",
                owner
        );

        recipe.addIngredient(flour);
        setId(recipe, 10L);
    }

    @Test
    void listRecipesReturnsRecipes() {
        Pageable pageable = PageRequest.of(0, 20);

        Page<Recipe> recipePage =
                new PageImpl<>(List.of(recipe));

        when(recipeRepository.findAll(pageable))
                .thenReturn(recipePage);

        Page<RecipeResponse> result =
                recipeService.listRecipes(pageable);

        assertEquals(1, result.getTotalElements());
        assertEquals("Pancakes", result.getContent().get(0).name());

        verify(recipeRepository).findAll(pageable);
    }

    @Test
    void getRecipeReturnsRecipe() {
        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        RecipeResponse result =
                recipeService.getRecipe(10L);

        assertEquals(10L, result.id());
        assertEquals("Pancakes", result.name());
    }

    @Test
    void getRecipeThrowsWhenRecipeDoesNotExist() {
        when(recipeRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                EntityNotFoundException.class,
                () -> recipeService.getRecipe(999L)
        );
    }

    @Test
    void createRecipeSavesRecipeWithIngredients() {
        RecipeCreate request = new RecipeCreate(
                "Sugar Pancakes",
                Set.of(1L, 2L),
                "3 cups",
                "Mix and cook."
        );

        when(ingredientRepository.findAllById(Set.of(1L, 2L)))
                .thenReturn(List.of(flour, sugar));

        when(recipeRepository.save(any(Recipe.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        RecipeResponse result =
                recipeService.createRecipe(request, owner);

        assertEquals("Sugar Pancakes", result.name());
        assertEquals(2, result.ingredients().size());

        verify(ingredientRepository)
                .findAllById(Set.of(1L, 2L));

        verify(recipeRepository).save(any(Recipe.class));
    }

    @Test
    void createRecipeThrowsWhenIngredientIsMissing() {
        RecipeCreate request = new RecipeCreate(
                "Invalid Recipe",
                Set.of(1L, 999L),
                "2 cups",
                "Instructions"
        );

        when(ingredientRepository.findAllById(Set.of(1L, 999L)))
                .thenReturn(List.of(flour));

        assertThrows(
                EntityNotFoundException.class,
                () -> recipeService.createRecipe(request, owner)
        );

        verify(recipeRepository, never())
                .save(any(Recipe.class));
    }

    @Test
    void replaceRecipeUpdatesAllRecipeFields() {
        RecipeCreate request = new RecipeCreate(
                "Updated Pancakes",
                Set.of(2L),
                "4 cups",
                "Updated instructions."
        );

        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        when(ingredientRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(sugar));

        RecipeResponse result =
                recipeService.replaceRecipe(
                        10L,
                        request,
                        owner
                );

        assertEquals("Updated Pancakes", result.name());
        assertEquals("4 cups", result.measurements());
        assertEquals(
                "Updated instructions.",
                result.instructions()
        );
        assertEquals(1, result.ingredients().size());
    }

    @Test
    void patchRecipeUpdatesOnlyProvidedFields() {
        RecipePatch request = new RecipePatch(
                null,
                null,
                null,
                JsonNodeFactory.instance.textNode(
                        "Updated instructions."
                )
        );

        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        RecipeResponse result =
                recipeService.patchRecipe(
                        10L,
                        request,
                        owner
                );

        assertEquals("Pancakes", result.name());
        assertEquals("2 cups", result.measurements());
        assertEquals(
                "Updated instructions.",
                result.instructions()
        );
    }

    @Test
    void patchRecipeClearsInstructionsWhenExplicitlyNull() {
        RecipePatch request = new RecipePatch(
                null,
                null,
                null,
                JsonNodeFactory.instance.nullNode()
        );

        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        RecipeResponse result =
                recipeService.patchRecipe(
                        10L,
                        request,
                        owner
                );

        assertNull(result.instructions());
        assertEquals("2 cups", result.measurements());
    }

    @Test
    void patchRecipeReplacesIngredientsWhenIngredientIdsProvided() {
        RecipePatch request = new RecipePatch(
                null,
                Set.of(2L),
                null,
                null
        );

        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        when(ingredientRepository.findAllById(Set.of(2L)))
                .thenReturn(List.of(sugar));

        RecipeResponse result =
                recipeService.patchRecipe(
                        10L,
                        request,
                        owner
                );

        assertEquals(1, result.ingredients().size());
        assertEquals(
                "Sugar",
                result.ingredients()
                        .iterator()
                        .next()
                        .ingredientName()
        );
    }

    @Test
    void deleteRecipeDeletesOwnedRecipe() {
        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        recipeService.deleteRecipe(10L, owner);

        verify(recipeRepository).delete(recipe);
    }

    @Test
    void anotherUserCannotUpdateRecipe() {
        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        RecipeCreate request = new RecipeCreate(
                "Unauthorized Update",
                Set.of(1L),
                "1 cup",
                "Instructions"
        );

        assertThrows(
                EntityNotFoundException.class,
                () -> recipeService.replaceRecipe(
                        10L,
                        request,
                        otherUser
                )
        );

        verify(recipeRepository, never())
                .save(any(Recipe.class));
    }

    @Test
    void anotherUserCannotDeleteRecipe() {
        when(recipeRepository.findById(10L))
                .thenReturn(Optional.of(recipe));

        assertThrows(
                EntityNotFoundException.class,
                () -> recipeService.deleteRecipe(
                        10L,
                        otherUser
                )
        );

        verify(recipeRepository, never())
                .delete(any(Recipe.class));
    }

    private static void setId(
            Object entity,
            Long id) throws Exception {

        Field idField =
                entity.getClass().getDeclaredField("id");

        idField.setAccessible(true);
        idField.set(entity, id);
    }
}