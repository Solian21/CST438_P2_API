package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name="ingredients")
public class Ingredient {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="ingredient_name", nullable = false, unique = true)
    private String ingredientName;

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

    //TODO: add recipe relationships
}
