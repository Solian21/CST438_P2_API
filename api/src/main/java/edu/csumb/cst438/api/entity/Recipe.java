package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a recipe created by a user.
 *
 * A recipe belongs to one user and can contain many ingredients.
 */
@Entity
@Table(name = "recipes")
@Getter
@Setter
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    // Many recipes can belong to one user.
    // The recipes table stores the user's ID in the user_id foreign key column.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    // Many recipes can contain many ingredients.
    // A separate recipe_ingredients join table connects recipes and ingredients.
    @ManyToMany
    @JoinTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients = new HashSet<>();

    /**
     * Protected constructor required by JPA.
     */
    protected Recipe() {
    }

    /**
     * Creates a recipe.
     *
     * @param name the recipe name
     * @param instructions the recipe preparation instructions
     * @param owner the user who owns the recipe
     */
    public Recipe(String name, String instructions, User owner) {
        this.name = name;
        this.instructions = instructions;
        this.owner = owner;
    }
}