package edu.csumb.cst438.api.repository;

import edu.csumb.cst438.api.entity.MealPlan;
import edu.csumb.cst438.api.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

/**
 * Database access for meal plans. Every lookup is scoped to an owner so one user
 * can never read another user's meal plans.
 */
public interface MealPlanRepository extends JpaRepository<MealPlan, Long> {

    /**
     * Returns one page of the owner's meal plans.
     *
     * @param owner the user who owns the meal plans
     * @param pageable page number, size, and sort
     * @return a page of meal plans
     */
    Page<MealPlan> findByOwner(User owner, Pageable pageable);

    /**
     * Finds a meal plan only if it belongs to the owner.
     *
     * @param id the meal plan id
     * @param owner the user who must own the meal plan
     * @return the meal plan, or empty if it doesn't exist or belongs to someone else
     */
    Optional<MealPlan> findByIdAndOwner(Long id, User owner);

    /**
     * Finds the signed-in user by email.
     *
     * TODO(#48): after PR #48 merges, delete this method and resolve the user through
     *  UserRepository instead:
     *  1. add {@code Optional<User> findByEmail(String email)} to UserRepository
     *  2. add {@code User getUserByEmail(String email)} to UserService and use it in place of
     *     UserService.getUserByName (also in UserController's /me handlers)
     *  3. change UserService.loadUserByUsername to look up by email and build the UserDetails
     *     with {@code .withUsername(user.getEmail())}, so Principal.getName() returns the email
     *  4. inject UserService into MealPlanService and replace currentUser() with getUserByEmail
     *  5. after #20 (OAuth2 JWT): set JwtAuthenticationConverter.setPrincipalClaimName("email")
     *     so Principal.getName() keeps returning the email
     *
     * @param email the user's email
     * @return the user, or empty if no user has that email
     */
    @Query("SELECT u FROM User u WHERE u.email = :email")
    Optional<User> findUserByEmail(String email);
}
