package edu.csumb.cst438.api.repository;

import edu.csumb.cst438.api.entity.Recipe;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Repository for accessing recipes in the database.
 */
public interface RecipeRepository extends JpaRepository<Recipe, Long> {
}