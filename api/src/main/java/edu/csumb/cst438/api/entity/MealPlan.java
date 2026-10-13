package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a meal plan that a user can make
 */
@Entity
@Table(name="meal_plan")
@Getter
public class MealPlan {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // I'm assuming meal plans need to be connected to a user.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User owner;

    @OneToMany(
            mappedBy = "mealPlan",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    private List<MealPlanEntry> mealPlanEntries = new ArrayList<>();

    @Column(nullable = false, name = "week_start")
    private LocalDate weekStart;

    protected MealPlan(){}

    /**
     * Creates a new meal plan connected to a user
     *
     * @param owner the owner/creator of the meal plan
     * @param week_start the week that the meal plan starts, cannot be null
     * @throws IllegalArgumentException if there is no owner or week start
     */
    public MealPlan(User owner, LocalDate week_start){
        if(owner == null || week_start == null){
            throw new IllegalArgumentException("Meal plans must have an owner and a starting weed.");
        }
        owner.addMealPlan(this);
        weekStart = week_start;
    }

    /**
     * Adds a meal plan entry to the collection and synchronizes the meal plan's entry collection
     *
     * @param mealPlanEntry Added meal plan entry
     * @throws IllegalArgumentException if the meal plan entry is null
     * @throws IllegalStateException if the meal plan entry doesn't belong to this meal plan
     */
    public void addMealPlanEntry(MealPlanEntry mealPlanEntry){
        if(mealPlanEntry == null){
            throw new IllegalArgumentException("Meal plan entry must not be null.");
        }
        if(mealPlanEntry.getMealPlan() != null && mealPlanEntry.getMealPlan() != this){
            throw new IllegalStateException("A meal plan entry cannot be transferred between meal plans.");
        }
        mealPlanEntries.add(mealPlanEntry);
        mealPlanEntry.setEntryFromMealPlan(this);
    }

    /**
     * Removes a meal plan's entry by removing it from the collection.
     * Because of orphanRemoval, this also removes the meal plan entry row on the database.
     *
     * @param mealPlanEntry meal plan entry to be removed
     * @throws IllegalArgumentException if the entry doesn't belong to this meal plan or if the entry is null
     */
    public void removeMealPlanEntry(MealPlanEntry mealPlanEntry){
        if(mealPlanEntry == null || mealPlanEntry.getMealPlan() != this){
            throw new IllegalArgumentException("Meal plan entry does not belong to meal plan.");
        }
        mealPlanEntries.remove(mealPlanEntry);
    }

    /**
     * Called by User.addMealPlan owning side cannot get out of sync similar to the recipe table.
     */
    void setOwnerFromUser(User owner) {
        this.owner = owner;
    }

    /**
     * This makes sure that if a recipe is deleted from an entry, it is also deleted here
     */
    @PreRemove
    private void detachUnsyncedEntries(){
        for(MealPlanEntry entry : mealPlanEntries){
            entry.getRecipe().getMealPlanEntries().remove(entry);
        }
    }
}
