package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.NotBlank;

public record IngredientCreate(
        @NotBlank String ingredientName
) {
}