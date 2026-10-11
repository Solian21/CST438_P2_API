package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ingredient_name", nullable = false, unique = true)
    private String ingredientName;

    @ManyToMany(mappedBy = "ingredients")
    private Set<Recipe> recipes = new HashSet<>();

    protected Ingredient() {}

    public Ingredient(String ingredientName) {
        this.ingredientName = ingredientName;
    }
    public Long getId() {
        return id;
    }

    public String getIngredientName() {
        return ingredientName;
    }

    public void setIngredientName(String ingredientName) {
        this.ingredientName = ingredientName;
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
}
