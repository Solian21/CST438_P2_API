package edu.csumb.cst438.api.controller;

import edu.csumb.cst438.api.record.UserResponseDTO;
import edu.csumb.cst438.api.record.UserRoleUpdateRequest;
import edu.csumb.cst438.api.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;

/**
 * The home of the API routes and what they do
 * Specifically for /api/v1/users and routes beyond that.
 */
@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    private final UserService userService;

    /**
     * The constructor of UserController
     * @param userService UserService instance
     */
    public UserController(UserService userService){
        this.userService = userService;
    }

    /**
     * Call for multiple users on a single page
     * @param page What page number the admin goes to (starts at 0)
     * @param size What page number the admin goes to (starts at 0)
     * @return Ok with users unless the one calling it isn't an Admin
     */
    @GetMapping
    public ResponseEntity<PagedModel<UserResponseDTO>> getAllUsers(@RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "20") int size){
        Page<UserResponseDTO> users = userService.getAllUsers(page, size);
        return ResponseEntity.ok(new PagedModel<>(users));
    }

    /**
     * Call for one user via ID
     * @param userId the ID that's being called
     * @return Ok with user unless the one calling it isn't an Admin
     */
    @GetMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long userId){
        UserResponseDTO user = userService.getUserById(userId);
        return ResponseEntity.ok(user);
    }

    /**
     * Call to delete one user via ID
     * @param userId the ID that's being called
     * @return noContent unless the one calling it isn't an Admin
     */
    @DeleteMapping("/{userId}")
    public ResponseEntity<Void> deleteUser(@PathVariable Long userId){
        userService.deleteUser(userId);
        return ResponseEntity.noContent().build();
    }

    /**
     * Call to patch one user via ID and role
     * @param userId the ID of the user being called
     * @param request the role that it's being changed to
     * @return Ok with user unless the one calling it isn't an Admin
     */
    @PatchMapping("/{userId}")
    public ResponseEntity<UserResponseDTO> patchUser(@PathVariable Long userId, @Valid @RequestBody UserRoleUpdateRequest request){
        UserResponseDTO updatedUser = userService.patchUser(userId, request.role());
        return ResponseEntity.ok(updatedUser);
    }

    /**
     * Call to current logged in user
     * @param principal the current user
     * @return Ok with user unless the one calling it isn't the current user
     */
    @GetMapping("/me")
    public ResponseEntity<UserResponseDTO> getCurrentUser(Principal principal){
        UserResponseDTO user = userService.getUserByEmail(principal.getName());
        return ResponseEntity.ok(user);
    }

    /**
     * Call to delete current logged in user
     * @param principal the current user
     * @return noContent unless the one calling it isn't the current user
     */
    @DeleteMapping("/me")
    public ResponseEntity<Void> deleteCurrentUser(Principal principal){
        UserResponseDTO user = userService.getUserByEmail(principal.getName());
        userService.deleteUser(user.id());
        return ResponseEntity.noContent().build();
    }
}
