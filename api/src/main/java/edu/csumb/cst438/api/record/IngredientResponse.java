package edu.csumb.cst438.api.record;

import edu.csumb.cst438.api.entity.Ingredient;

public record IngredientResponse(
        Long id,
        String ingredientName
) {
    public static IngredientResponse from(Ingredient ingredient) {
        return new IngredientResponse(
                ingredient.getId(),
                ingredient.getIngredientName()
        );
    }

    public Long getId() {
        return id;
    }

    public String getIngredientName() {
        return ingredientName;
    }
}