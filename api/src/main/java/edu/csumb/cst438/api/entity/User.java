package edu.csumb.cst438.api.entity;

import jakarta.persistence.*;

@Entity
@Table(name="users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    //TODO: get clarification is display name = username so like lesly123 or is it like Lesly Villanueva
    @Column(name = "display_name")
    private String displayName;

    @Column(nullable = false)
    private String role = "USER";

    protected User() {}
    public User(String email, String displayName, String role) {
        this.email = email;
        this.displayName = displayName;
        this.role = role;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
