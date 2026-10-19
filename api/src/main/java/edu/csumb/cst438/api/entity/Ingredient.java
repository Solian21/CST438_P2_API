package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

@Entity
@Table(name = "ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "ingredient_name", nullable = false)
    private String ingredientName;

    @Column(
            name = "ingredient_name_normalized",
            nullable = false,
            unique = true
    )
    private String normalizedName;

    @ManyToMany(mappedBy = "ingredients")
    private Set<Recipe> recipes = new HashSet<>();

    protected Ingredient() {
    }

    public Ingredient(String ingredientName) {
        setIngredientName(ingredientName);
    }

    public Long getId() {
        return id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
        this.normalizedName = normalize(ingredientName);
    }

    public Set<Recipe> getRecipes() {
        return recipes;
    }

    void addRecipe(Recipe recipe) {
        recipes.add(recipe);
    }

    void removeRecipe(Recipe recipe) {
        recipes.remove(recipe);
    }

    @PrePersist
    @PreUpdate
    private void updateNormalizedName() {
        this.normalizedName = normalize(this.ingredientName);
    }

    private static String normalize(String value) {
        return value == null
                ? null
                : value.trim().toLowerCase(Locale.ROOT);
    }
}