package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.UserResponseDTO;
import edu.csumb.cst438.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

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
     * This also creates the user on the first sign in
     * @param email user email
     * @return the user
     */
    public UserResponseDTO getUserByEmail(String email){
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> userRepository.save(
                        new User(email, email, "USER")
                ));
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    /**
     * Gives a list of all the users by page
     * @param page an int of page (starts at 0)
     * @param size an int of size (default is 20)
     * @return a list of users
     * @throws IllegalArgumentException if page index is < 0
     */
    public Page<UserResponseDTO> getAllUsers(int page, int size){
        if (page < 0) {
            throw new IllegalArgumentException("Page index must be 0 or greater");
        }
        if (size < 1 || size > 100){
            throw new IllegalArgumentException("Page size must be between 1 and 100");
        }
        return userRepository.findAll(PageRequest.of(page, size))
                .map(user -> new UserResponseDTO(
                        user.getId(),
                        user.getEmail(),
                        user.getDisplayName(),
                        user.getRole()
                ));
    }

    /**
     * Gives a user by ID
     * @param id the user ID
     * @return the user
     */
    public UserResponseDTO getUserById(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("No user found with id " + id));
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }

    /**
     * Deletes a user by ID
     * @param id the user ID
     */
    public void deleteUser(Long id){
        userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user found with id " + id));
        userRepository.deleteById(id);
    }

    /**
     * Grants or revokes admin
     * @param id the user ID
     * @param role the role (USER/ADMIN)
     * @return the updated user
     */
    @Transactional
    public UserResponseDTO patchUser(Long id, String role){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("No user found with id " + id));
        user.changeRole(role);
        return new UserResponseDTO(
                user.getId(),
                user.getEmail(),
                user.getDisplayName(),
                user.getRole()
        );
    }
}
