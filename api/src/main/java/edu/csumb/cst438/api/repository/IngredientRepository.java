package edu.csumb.cst438.api.repository;

import edu.csumb.cst438.api.entity.Ingredient;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IngredientRepository
        extends JpaRepository<Ingredient, Long> {

    boolean existsByIngredientNameIgnoreCase(String ingredientName);
}