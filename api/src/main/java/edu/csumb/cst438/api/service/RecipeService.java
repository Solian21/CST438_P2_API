package edu.csumb.cst438.api.service;

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

@Service
@Transactional
public class RecipeService {

    private final RecipeRepository recipeRepository;
    private final IngredientRepository ingredientRepository;

    public RecipeService(
            RecipeRepository recipeRepository,
            IngredientRepository ingredientRepository) {
        this.recipeRepository = recipeRepository;
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public Page<RecipeResponse> listRecipes(Pageable pageable) {
        return recipeRepository
                .findAll(pageable)
                .map(RecipeResponse::from);
    }

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

        return RecipeResponse.from(recipeRepository.save(recipe));
    }

    @Transactional(readOnly = true)
    public RecipeResponse getRecipe(Long recipeId) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe " + recipeId + " not found"));

        return RecipeResponse.from(recipe);
    }

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

    public RecipeResponse patchRecipe(
            Long recipeId,
            RecipePatch request,
            User owner) {

        Recipe recipe = findOwnedRecipe(recipeId, owner);

        String name = request.name() != null
                ? request.name()
                : recipe.getName();

        String measurements = request.measurements() != null
                ? request.measurements()
                : recipe.getMeasurements();

        String instructions = request.instructions() != null
                ? request.instructions()
                : recipe.getInstructions();

        recipe.updateDetails(name, measurements, instructions);

        if (request.ingredientIds() != null) {
            replaceIngredients(recipe, request.ingredientIds());
        }

        return RecipeResponse.from(recipe);
    }

    public void deleteRecipe(Long recipeId, User owner) {
        Recipe recipe = findOwnedRecipe(recipeId, owner);
        recipeRepository.delete(recipe);
    }

    private Recipe findOwnedRecipe(Long recipeId, User owner) {
        Recipe recipe = recipeRepository.findById(recipeId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Recipe " + recipeId + " not found"));

        if (recipe.getOwner() == null
                || !recipe.getOwner().getId().equals(owner.getId())) {
            throw new EntityNotFoundException(
                    "Recipe " + recipeId + " not found");
        }

        return recipe;
    }

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

    private Set<Ingredient> findIngredients(Set<Long> ingredientIds) {
        List<Ingredient> ingredients =
                ingredientRepository.findAllById(ingredientIds);

        if (ingredients.size() != ingredientIds.size()) {
            throw new EntityNotFoundException(
                    "One or more ingredients were not found");
        }

        return new HashSet<>(ingredients);
    }
}