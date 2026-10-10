package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

/**
 * Represents a user of the recipe application.
 */
@Entity
@Table(name = "users")
@Getter
@Setter
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
}