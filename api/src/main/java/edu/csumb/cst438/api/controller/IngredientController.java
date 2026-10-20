package edu.csumb.cst438.api.controller;

import edu.csumb.cst438.api.record.IngredientCreate;
import edu.csumb.cst438.api.record.IngredientResponse;
import edu.csumb.cst438.api.service.IngredientService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/ingredients")
public class IngredientController {

    private final IngredientService ingredientService;

    public IngredientController(IngredientService ingredientService) {
        this.ingredientService = ingredientService;
    }

    @GetMapping
    public PagedModel<IngredientResponse> listIngredients(
            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100) int size) {

        return new PagedModel<>(
                ingredientService.list(page, size)
        );
    }

    @PostMapping
    public ResponseEntity<IngredientResponse> createIngredient(
            @Valid @RequestBody IngredientCreate request) {

        IngredientResponse created =
                ingredientService.create(request);

        return ResponseEntity
                .created(URI.create(
                        "/api/v1/ingredients/" + created.getId()
                ))
                .body(created);
    }
}