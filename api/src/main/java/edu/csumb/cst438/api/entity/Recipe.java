package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import org.hibernate.Hibernate;
import org.hibernate.annotations.CreationTimestamp;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Represents either a prepopulated recipe or a recipe created by a user.
 *
 * A null owner means the recipe is prepopulated and shared.
 */
@Entity
@Table(name = "recipes")
@Getter
public class Recipe {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column
    private String measurements;

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

    @OneToMany(
            mappedBy = "recipe",
            cascade = CascadeType.REMOVE
    )
    private List<MealPlanEntry> mealPlanEntries = new ArrayList<>();

    protected Recipe() {
    }

    /**
     * Creates a recipe.
     * The owner may be null for prepopulated recipes.
     */
    public Recipe(String name, String measurements, String instructions, User owner) {
        this.name = name;
        this.measurements = measurements;
        this.instructions = instructions;
        if (owner != null) {
            owner.addRecipe(this);
        }
    }

    /** Creates a recipe without measurements for callers using the original constructor. */
    public Recipe(String name, String instructions, User owner) {
        this(name, null, instructions, owner);
    }

    /**
     * Updates this recipe's name, measurements, and instructions.
     *
     * @param name the new recipe name; must not be null or blank
     * @param measurements the new ingredient measurements, or null if not provided
     * @param instructions the new preparation instructions, or null if not provided
     * @throws IllegalArgumentException if name is null or blank
     */
    public void updateDetails(String name, String measurements, String instructions) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Recipe name must not be blank");
        }
        this.name = name;
        this.measurements = measurements;
        this.instructions = instructions;
    }

    /**
     * Adds an ingredient to this recipe and synchronizes the ingredient's
     * recipe collection.
     *
     * @param ingredient the ingredient to add
     * @throws IllegalArgumentException if ingredient is null
     */
    public void addIngredient(Ingredient ingredient) {
        if (ingredient == null) {
            throw new IllegalArgumentException("Ingredient must not be null");
        }
        if (ingredients.add(ingredient)) {
            ingredient.addRecipe(this);
        }
    }

    /**
     * Removes an ingredient from this recipe and synchronizes the ingredient's
     * recipe collection.
     *
     * @param ingredient the ingredient to remove
     */
    public void removeIngredient(Ingredient ingredient) {
        if (ingredients.remove(ingredient)) {
            ingredient.removeRecipe(this);
        }
    }

    /** Called by User.addRecipe so the owning side cannot get out of sync. */
    void setOwnerFromUser(User owner) {
        this.owner = owner;
    }

    /**
     * Returns true when this is a prepopulated/shared recipe.
     */
    public boolean isPrepopulated() {
        return owner == null;
    }

    /**
     * Adds a meal plan entry to the collection and synchronizes the recipe's meal plan entry collection
     *
     * @param mealPlanEntry Added meal plan entry
     * @throws IllegalArgumentException if the meal plan entry is null
     * @throws IllegalStateException if the meal plan entry has a different recipe
     */
    public void addMealPlanEntry(MealPlanEntry mealPlanEntry){
        if(mealPlanEntry == null){
            throw new IllegalArgumentException("Meal plan entry must not be null.");
        }
        // unproxy: a lazy-loaded getter returns a Hibernate proxy, which is never == this
        if(mealPlanEntry.getRecipe() != null && Hibernate.unproxy(mealPlanEntry.getRecipe()) != this){
            throw new IllegalStateException("A meal plan entry cannot be transferred between recipes.");
        }
        mealPlanEntries.add(mealPlanEntry);
        mealPlanEntry.setEntryFromRecipe(this);
    }

    /**
     * Removes a meal plan's entry by removing it from the collection.
     * Because of orphanRemoval, this also removes the meal plan entry row on the database.
     *
     * @param mealPlanEntry meal plan entry to be removed
     * @throws IllegalArgumentException if the entry doesn't belong to this recipe or if the entry is null
     */
    public void removeMealPlanEntry(MealPlanEntry mealPlanEntry){
        if(mealPlanEntry == null || Hibernate.unproxy(mealPlanEntry.getRecipe()) != this){
            throw new IllegalArgumentException("Meal plan entry does not have this recipe.");
        }
        mealPlanEntries.remove(mealPlanEntry);
    }

    /**
     * This makes sure that if an entry is deleted from mealplan, it is also deleted here
     */
    @PreRemove
    private void detachUnsyncedEntries(){
        for(MealPlanEntry entry : mealPlanEntries){
            entry.getMealPlan().getMealPlanEntries().remove(entry);
        }
    }
}
