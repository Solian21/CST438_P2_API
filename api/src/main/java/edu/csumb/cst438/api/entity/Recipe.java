package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a recipe created by a user.
 *
 * A recipe belongs to one user and can contain many ingredients.
 */
@Entity
@Table(name = "recipes")
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

            // Points to the recipe primary key in the join table.
            joinColumns = @JoinColumn(name = "recipe_id"),

            // Points to the ingredient primary key in the join table.
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

    /**
     * Gets the recipe ID.
     *
     * @return the recipe ID
     */
    public Long getId() {
        return id;
    }

    /**
     * Gets the recipe name.
     *
     * @return the recipe name
     */
    public String getName() {
        return name;
    }

    /**
     * Updates the recipe name.
     *
     * @param name the new recipe name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the recipe instructions.
     *
     * @return the recipe instructions
     */
    public String getInstructions() {
        return instructions;
    }

    /**
     * Updates the recipe instructions.
     *
     * @param instructions the new recipe instructions
     */
    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    /**
     * Gets the user who owns the recipe.
     *
     * @return the recipe owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Updates the recipe owner.
     *
     * @param owner the new recipe owner
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Gets the ingredients used in the recipe.
     *
     * @return the recipe ingredients
     */
    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    /**
     * Replaces the ingredients used in the recipe.
     *
     * @param ingredients the new set of ingredients
     */
    public void setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}