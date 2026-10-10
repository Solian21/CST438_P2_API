package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

/**
 * Represents either a prepopulated recipe or a recipe created by a user.
 *
 * A null owner means the recipe is prepopulated and shared.
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

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    /**
     * Null owner means this is a prepopulated recipe.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = true)
    @JoinColumn(name = "user_id", nullable = true)
    private User owner;

    @ManyToMany
    @JoinTable(
            name = "recipe_ingredients",
            joinColumns = @JoinColumn(name = "recipe_id"),
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients = new HashSet<>();

    protected Recipe() {
    }

    /**
     * Creates a recipe.
     * The owner may be null for prepopulated recipes.
     */
    public Recipe(String name, String instructions, User owner) {
        this.name = name;
        this.instructions = instructions;
        this.owner = owner;
    }

    /**
     * Returns true when this is a prepopulated/shared recipe.
     */
    public boolean isPrepopulated() {
        return owner == null;
    }
}