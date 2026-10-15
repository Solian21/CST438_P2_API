package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.UserResponseDTO;
import edu.csumb.cst438.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Methods that are ran in the controller, does a lot of the work.
 */
@Service
public class UserService {
    private final UserRepository userRepository;

    /**
     * Constructor for UserService
     * @param userRepository the repository class
     */
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Gets user by email for future oauth stuff
     * @param email user email
     * @return the user
     */
    public User getUserByEmail(String email){
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new EntityNotFoundException("No user found for the authenticated account"));
    }

    /**
     * Gives a list of all the users by page
     * @param page an int of page (starts at 0)
     * @return a list of users
     * @throws IllegalArgumentException if page index isn't <= 0
     */
    public Page<User> getAllUsers(int page){
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be 0 or greater");
        }
        return userRepository.findAll(PageRequest.of(page, 20));
    }

    /**
     * Gives a user by ID
     * @param id the user ID
     * @return the user
     */
    public UserResponseDTO getUserById(Long id){
        Optional<User> user = userRepository.findById(id);
        return new UserResponseDTO(
                user.get().getId(),
                user.get().getEmail(),
                user.get().getDisplayName(),
                user.get().getRole()
        );
    }

    /**
     * Deletes a user by ID
     * @param id the user ID
     */
    public void deleteUser(Long id){
        userRepository.deleteById(id);
    }

    /**
     * Grants or revokes admin
     * @param id the user ID
     * @param role the role (USER/ADMIN)
     * @return the updated user
     */
    @Transactional
    public User patchUser(Long id, String role){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user found with id " + id));
        user.changeRole(role);
        return userRepository.save(user);
    }
}
