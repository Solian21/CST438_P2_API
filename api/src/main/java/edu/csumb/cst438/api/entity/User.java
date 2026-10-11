package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user of the recipe application.
 */
@Entity
@Table(name = "users")
@Getter
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String role = "USER";

    @OneToMany(
            mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private Set<Recipe> recipes = new HashSet<>();

    protected User() {
    }

    /**
     * Creates a user.
     *
     * @param email the user's email address
     * @param displayName the user's display name
     * @param role the user's role
     */
    public User(String email, String displayName, String role) {
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }

    public void addRecipe(Recipe recipe) {
        if (recipe == null) {
            throw new IllegalArgumentException("Recipe must not be null");
        }
        if (recipe.getOwner() != null && recipe.getOwner() != this) {
            throw new IllegalStateException("A recipe cannot be transferred between owners");
        }
        recipes.add(recipe);
        recipe.setOwnerFromUser(this);
    }

    /**
     * Deletes one of this user's recipes by removing it from the collection.
     * With orphanRemoval enabled, this removes the recipe row from the database.
     *
     * @param recipe the recipe to remove
     * @throws IllegalArgumentException if the recipe is null or belongs to another user
     */
    public void removeRecipe(Recipe recipe) {
        if (recipe == null || recipe.getOwner() != this) {
            throw new IllegalArgumentException("Recipe does not belong to this user");
        }
        recipes.remove(recipe);
    }
}
