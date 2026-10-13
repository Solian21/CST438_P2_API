package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.util.*;

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

    @OneToMany(
            mappedBy = "owner",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MealPlan> mealPlans = new ArrayList<>();

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

    /**
     * Adds a recipe to the collection and synchronizes the user's recipe collection
     *
     * @param recipe recipe to be added
     * @throws IllegalArgumentException if the recipe is null
     * @throws IllegalStateException if the recipe doesn't belong to this user
     */
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
     * Adds a meal plan to the collection and synchronizes the user's meal plan collection
     *
     * @param mealPlan meal plan to be added
     * @throws IllegalArgumentException if the meal plan is null
     * @throws IllegalStateException if the meal plan doesn't belong to this user
     */
    public void addMealPlan(MealPlan mealPlan){
        if(mealPlan == null){
            throw new IllegalArgumentException("Meal plan must not be null");
        }
        if(mealPlan.getOwner() != null && mealPlan.getOwner() != this){
            throw new IllegalStateException("A meal plan cannot be transferred between owners");
        }
        mealPlans.add(mealPlan);
        mealPlan.setOwnerFromUser(this);
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

    /**
     * Removes a user's meal plan by removing it from the collection.
     * Because orphanRemoval is enabled, it also removes meal plan row from the database, similar to recipes.
     *
     * @param mealPlan the meal plan to remove
     * @throws IllegalArgumentException if the meal plan is null or doesn't belong to this user
     */
    public void removeMealPlan(MealPlan mealPlan){
        if(mealPlan == null || mealPlan.getOwner() != this){
            throw new IllegalArgumentException("Meal plan does not belong to this user");
        }
        mealPlans.remove(mealPlan);
    }

    /**
     * When getMealPlans() is called, the caller should not be able to modify the list
     * @return mealPlans list for the user
     */
    public List<MealPlan> getMealPlans() {
        return mealPlans != null ? Collections.unmodifiableList(mealPlans) : Collections.emptyList();
    }
}
