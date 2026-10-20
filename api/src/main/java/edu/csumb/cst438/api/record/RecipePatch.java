package edu.csumb.cst438.api.record;

import com.fasterxml.jackson.databind.JsonNode;
import jakarta.validation.constraints.Pattern;

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