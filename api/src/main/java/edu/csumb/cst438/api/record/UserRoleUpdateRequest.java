package edu.csumb.cst438.api.record;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * For updating the user role in PATCH
 * @param role user role
 */
public record UserRoleUpdateRequest(
        @NotNull(message = "role is required")
        @Pattern(regexp = "USER|ADMIN", message = "role must be USER or ADMIN")
        String role) {
}
