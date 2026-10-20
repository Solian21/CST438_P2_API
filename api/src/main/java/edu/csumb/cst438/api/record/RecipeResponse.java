package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.Recipe;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

public record RecipeResponse(
        Long id,
        String name,
        Instant createdAt,
        Set<IngredientResponse> ingredients,
        String measurements,
        String instructions
) {
    public static RecipeResponse from(Recipe recipe) {
        return new RecipeResponse(
                recipe.getId(),
                recipe.getName(),
                recipe.getCreatedAt(),
                recipe.getIngredients()
                        .stream()
                        .map(IngredientResponse::from)
                        .collect(Collectors.toSet()),
                recipe.getMeasurements(),
                recipe.getInstructions()
        );
    }
}