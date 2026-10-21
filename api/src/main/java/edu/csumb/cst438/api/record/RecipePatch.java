package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.Pattern;
import tools.jackson.databind.JsonNode;

import java.util.Set;

public record RecipePatch(
        @Pattern(
                regexp = ".*\\S.*",
                message = "Name must not be blank"
        )
        String name,

        Set<Long> ingredientIds,

        JsonNode measurements,

        JsonNode instructions
) {
}