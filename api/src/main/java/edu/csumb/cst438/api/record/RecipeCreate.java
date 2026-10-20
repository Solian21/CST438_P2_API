package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.Set;

public record RecipeCreate(
        @NotBlank(message = "Recipe name is required")
        String name,

        @NotNull(message = "Ingredient IDs are required")
        Set<Long> ingredientIds,

        String measurements,

        String instructions
) {
}