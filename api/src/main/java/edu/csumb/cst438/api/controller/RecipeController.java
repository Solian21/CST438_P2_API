package edu.csumb.cst438.api.controller;

import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.RecipeCreate;
import edu.csumb.cst438.api.record.RecipePatch;
import edu.csumb.cst438.api.record.RecipeResponse;
import edu.csumb.cst438.api.repository.UserRepository;
import edu.csumb.cst438.api.service.RecipeService;
import jakarta.validation.Valid;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/recipes")
public class RecipeController {

    private final RecipeService recipeService;
    private final UserRepository userRepository;

    public RecipeController(
            RecipeService recipeService,
            UserRepository userRepository) {
        this.recipeService = recipeService;
        this.userRepository = userRepository;
    }

    @GetMapping
    public PagedModel<RecipeResponse> listRecipes(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt,desc") String sort) {

        if (page < 0) {
            throw new IllegalArgumentException(
                    "Page index must be 0 or greater");
        }

        if (size < 1 || size > 100) {
            throw new IllegalArgumentException(
                    "Page size must be between 1 and 100");
        }

        Pageable pageable = createPageable(page, size, sort);

        return new PagedModel<>(
                recipeService.listRecipes(pageable)
        );
    }

    @PostMapping
    public ResponseEntity<RecipeResponse> createRecipe(
            @Valid @RequestBody RecipeCreate request,
            Authentication authentication) {

        User owner = getCurrentUser(authentication);

        RecipeResponse created =
                recipeService.createRecipe(request, owner);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/recipes/" + created.id()
                ))
                .body(created);
    }

    @GetMapping("/{recipeId}")
    public RecipeResponse getRecipe(@PathVariable Long recipeId) {
        return recipeService.getRecipe(recipeId);
    }

    @PutMapping("/{recipeId}")
    public RecipeResponse replaceRecipe(
            @PathVariable Long recipeId,
            @Valid @RequestBody RecipeCreate request,
            Authentication authentication) {

        User owner = getCurrentUser(authentication);

        return recipeService.replaceRecipe(
                recipeId,
                request,
                owner
        );
    }

    @PatchMapping("/{recipeId}")
    public RecipeResponse patchRecipe(
            @PathVariable Long recipeId,
            @Valid @RequestBody RecipePatch request,
            Authentication authentication) {

        User owner = getCurrentUser(authentication);

        return recipeService.patchRecipe(
                recipeId,
                request,
                owner
        );
    }

    @DeleteMapping("/{recipeId}")
    public ResponseEntity<Void> deleteRecipe(
            @PathVariable Long recipeId,
            Authentication authentication) {

        User owner = getCurrentUser(authentication);

        recipeService.deleteRecipe(recipeId, owner);

        return ResponseEntity.noContent().build();
    }

    private User getCurrentUser(Authentication authentication) {
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new IllegalArgumentException(
                        "Authenticated user was not found"));
    }

    private Pageable createPageable(
            int page,
            int size,
            String sort) {

        String[] sortParts = sort.split(",");

        if (sortParts.length != 2) {
            throw new IllegalArgumentException(
                    "Sort must use the format field,direction");
        }

        String field = sortParts[0];
        String direction = sortParts[1];

        if (!field.equals("name") && !field.equals("createdAt")) {
            throw new IllegalArgumentException(
                    "Recipes can only be sorted by name or createdAt");
        }

        if (!direction.equals("asc") && !direction.equals("desc")) {
            throw new IllegalArgumentException(
                    "Sort direction must be asc or desc");
        }

        Sort.Direction sortDirection =
                direction.equals("asc")
                        ? Sort.Direction.ASC
                        : Sort.Direction.DESC;

        return PageRequest.of(
                page,
                size,
                Sort.by(sortDirection, field)
        );
    }
}