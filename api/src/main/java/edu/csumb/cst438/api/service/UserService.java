package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.jspecify.annotations.NonNull;
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
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    /**
     * Constructor for UserService
     * @param userRepository the repository class
     */
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    /**
     * Should allow spring security to authenticate users based on our database
     * @param username user display name
     * @return user details
     * @throws UsernameNotFoundException if user not found
     */
    @Override
    public UserDetails loadUserByUsername(@NonNull String username) throws UsernameNotFoundException{
        User user = userRepository.findByDisplayName(username)
                .orElseThrow(() -> new UsernameNotFoundException("No user named " + username));
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getDisplayName())
                .password(user.getEmail())
                .roles(user.getRole())
                .build();
    }

    /**
     * Gives a list of all the users by page
     * @param page an int of page (starts at 0)
     * @return a list of users
     */
    public List<User> getAllUsers(int page){
        int size = 20;
        Pageable pageable = PageRequest.of(page, size);
        return userRepository.findUsersInPage(pageable);
    }

    /**
     * Gives a user by ID
     * @param id the user ID
     * @return the user
     */
    public Optional<User> getUserById(Long id){
        return userRepository.findById(id);
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

    /**
     * Gets user by display name
     * @param name the display name
     * @return the user
     */
    public User getUserByName(String name){
        return userRepository.findByDisplayName(name)
                .orElseThrow(() -> new EntityNotFoundException("No user named " + name));
    }
}
