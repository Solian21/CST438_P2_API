package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name="recipes")
public class Recipe {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String instructions;

    //Many recipes can belong to one user
    //the recipes table stores the user's ID in the user_id foreign key column
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name="user_id", nullable = false)
    private User owner;

    //Many recipes can contain many ingredients meaning Many to many
    //Because of manytomany, we create a separate recipe_ingredients join table
    // to connect recipes and ingredients
    @ManyToMany
    @JoinTable(
            name="recipe_ingredients",
            //point to the recipe primary ket in the join table
            joinColumns = @JoinColumn(name="recipe_id"),
            //points to the ingrdient primary key in join table
            inverseJoinColumns = @JoinColumn(name = "ingredient_id")
    )
    private Set<Ingredient> ingredients = new HashSet<>();

    protected Recipe() {}

    public Recipe(String name, String instructions, User owner){
        this.name = name;
        this.instructions = instructions;
        this.owner = owner;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getInstructions() {
        return instructions;
    }

    public void setInstructions(String instructions) {
        this.instructions = instructions;
    }

    public User getOwner() {
        return owner;
    }

    public void setOwner(User owner) {
        this.owner = owner;
    }

    public Set<Ingredient> getIngredients() {
        return ingredients;
    }

    public void setIngredients(Set<Ingredient> ingredients) {
        this.ingredients = ingredients;
    }
}
