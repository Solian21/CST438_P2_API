package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.Size;

import java.util.Set;

public record RecipePatch(
        @Size(min = 1, message = "Name must not be blank")
        String name,

        Set<Long> ingredientIds,

        String measurements,

        String instructions
) {
}