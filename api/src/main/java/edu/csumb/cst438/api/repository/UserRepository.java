package edu.csumb.cst438.api.repository;

import edu.csumb.cst438.api.entity.User;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

/**
 * Interface class for the database stuff for User
 */
public interface UserRepository extends JpaRepository<User, Long> {
    /**
     * Interface to return a user by display name
     * @param name the display name
     * @return the user connected to the display name
     */
    Optional<User> findByDisplayName(String name);

    /**
     * Interface to return a page of users
     * @param pageable pageable with page number and size
     * @return a list of users
     */
    @Query("SELECT u FROM User u")
    List<User> findUsersInPage(Pageable pageable);
}
