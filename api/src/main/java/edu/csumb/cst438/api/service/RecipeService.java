package edu.csumb.cst438.api.service;

import com.fasterxml.jackson.databind.JsonNode;
import edu.csumb.cst438.api.entity.Ingredient;
import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.RecipeCreate;
import edu.csumb.cst438.api.record.RecipePatch;
import edu.csumb.cst438.api.record.RecipeResponse;
import edu.csumb.cst438.api.repository.IngredientRepository;
import edu.csumb.cst438.api.repository.RecipeRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Provides business logic for recipe operations.
 */
@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    /**
     * Creates a recipe service.
     *
     * @param recipeRepository repository for recipes
     * @param ingredientRepository repository for ingredients
     */
    public RecipeService(
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    /**
     * Lists recipes using pagination and sorting.
     *
     * @param pageable pagination and sorting information
     * @return a page of recipes
     */
    @Transactional(readOnly = true)
    public Page<RecipeResponse> listRecipes(Pageable pageable) {
        return recipeRepository
                .findAll(pageable)
                .map(RecipeResponse::from);
    }

    /**
     * Creates a recipe for the specified user.
     *
     * @param request recipe creation data
     * @param owner user who owns the recipe
     * @return the created recipe
     */
    public RecipeResponse createRecipe(
            RecipeCreate request,
            User owner) {

        Set<Ingredient> ingredients =
                findIngredients(request.ingredientIds());

        Recipe recipe = new Recipe(
                request.name(),
                request.measurements(),
                request.instructions(),
                owner
        );

        ingredients.forEach(recipe::addIngredient);

        return RecipeResponse.from(
                recipeRepository.save(recipe)
        );
    }

    /**
     * Retrieves a recipe by ID.
     *
     * @param recipeId recipe ID
     * @return the requested recipe
     * @throws EntityNotFoundException if the recipe does not exist
     */
    @Transactional(readOnly = true)
    public RecipeResponse getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe " + recipeId + " not found"
                ));

        return RecipeResponse.from(recipe);
    }

    /**
     * Replaces all editable fields of a user's recipe.
     *
     * @param recipeId recipe ID
     * @param request replacement data
     * @param owner authenticated user
     * @return the updated recipe
     */
    public RecipeResponse replaceRecipe(
            Long recipeId,
            RecipeCreate request,
            User owner) {

        Recipe recipe = findOwnedRecipe(recipeId, owner);

        recipe.updateDetails(
                request.name(),
                request.measurements(),
                request.instructions()
        );

        replaceIngredients(recipe, request.ingredientIds());

        return RecipeResponse.from(recipe);
    }

    /**
     * Partially updates a user's recipe.
     *
     * Missing fields retain their existing values. Fields explicitly set
     * to null are cleared.
     *
     * @param recipeId recipe ID
     * @param request partial update data
     * @param owner authenticated user
     * @return the updated recipe
     */
    public RecipeResponse patchRecipe(
            Long recipeId,
            RecipePatch request,
            User owner) {

        Recipe recipe = findOwnedRecipe(recipeId, owner);

        String name = request.name() != null
                ? request.name()
                : recipe.getName();

        String measurements = patchValue(
                request.measurements(),
                recipe.getMeasurements()
        );

        String instructions = patchValue(
                request.instructions(),
                recipe.getInstructions()
        );

        recipe.updateDetails(
                name,
                measurements,
                instructions
        );

        if (request.ingredientIds() != null) {
            replaceIngredients(recipe, request.ingredientIds());
        }

        return RecipeResponse.from(recipe);
    }

    /**
     * Deletes a user's recipe.
     *
     * @param recipeId recipe ID
     * @param owner authenticated user
     */
    public void deleteRecipe(Long recipeId, User owner) {
        Recipe recipe = findOwnedRecipe(recipeId, owner);
        recipeRepository.delete(recipe);
    }

    /**
     * Finds a recipe and verifies that it belongs to the specified user.
     *
     * @param recipeId recipe ID
     * @param owner authenticated user
     * @return the owned recipe
     * @throws EntityNotFoundException if the recipe does not exist or
     *                                  belongs to another user
     */
    private Recipe findOwnedRecipe(Long recipeId, User owner) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe " + recipeId + " not found"
                ));

        if (recipe.getOwner() == null
                || !recipe.getOwner().getId().equals(owner.getId())) {
            throw new EntityNotFoundException(
                    "Recipe " + recipeId + " not found"
            );
        }

        return recipe;
    }

    /**
     * Replaces all ingredients assigned to a recipe.
     *
     * @param recipe recipe being updated
     * @param ingredientIds new ingredient IDs
     */
    private void replaceIngredients(
            Recipe recipe,
            Set<Long> ingredientIds) {

        Set<Ingredient> newIngredients =
                findIngredients(ingredientIds);

        Set<Ingredient> oldIngredients =
                new HashSet<>(recipe.getIngredients());

        oldIngredients.forEach(recipe::removeIngredient);
        newIngredients.forEach(recipe::addIngredient);
    }

    /**
     * Finds all ingredients referenced by their IDs.
     *
     * @param ingredientIds ingredient IDs
     * @return matching ingredients
     * @throws EntityNotFoundException if an ingredient is missing
     */
    private Set<Ingredient> findIngredients(Set<Long> ingredientIds) {
        List<Ingredient> ingredients =
                ingredientRepository.findAllById(ingredientIds);

        if (ingredients.size() != ingredientIds.size()) {
            throw new EntityNotFoundException(
                    "One or more ingredients were not found"
            );
        }

        return new HashSet<>(ingredients);
    }

    /**
     * Applies PATCH semantics to a nullable text field.
     *
     * @param value incoming JSON value
     * @param currentValue existing database value
     * @return the updated value
     */
    private String patchValue(
            JsonNode value,
            String currentValue) {

        // The field was omitted, so preserve the old value.
        if (value == null) {
            return currentValue;
        }

        // The field was explicitly set to null, so clear it.
        if (value.isNull()) {
            return null;
        }

        if (!value.isTextual()) {
            throw new IllegalArgumentException(
                    "Recipe text fields must be strings or null"
            );
        }

        return value.asText();
    }
}