package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.Recipe;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.UserResponseDTO;
import edu.csumb.cst438.api.repository.UserRepository;
import edu.csumb.cst438.api.service.UserService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests methods in UserService class
 * Disclaimer: also assisted by AI
 */
@ExtendWith(MockitoExtension.class)
public class UserServiceTest {
    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    /**
     * Tests getUserByName
     */
    @Test
    void testGetUserByUsername(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findByEmail("email@gmail.com")).thenReturn(Optional.of(mockUser));

        UserResponseDTO result = userService.getUserByEmail("email@gmail.com");
        assertNotNull(result);
        assertEquals("newUser", result.displayName());
        assertEquals("email@gmail.com", result.email());
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("email@gmail.com");
    }

    /**
     * Tests getAllUsers
     */
    @Test
    void testGetAllUsers(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        User mockUser2 = new User("gmail@gmail.com", "newerUser", "USER");
        Page<User> page = new PageImpl<>(List.of(mockUser, mockUser2));
        Mockito.when(userRepository.findAll(PageRequest.of(0, 2))).thenReturn(page);

        Page<UserResponseDTO> result = userService.getAllUsers(0, 2);

        assertEquals(2, result.getTotalElements());
        assertEquals("email@gmail.com", result.getContent().get(0).email());
        assertEquals("gmail@gmail.com", result.getContent().get(1).email());
        Mockito.verify(userRepository, Mockito.times(1)).findAll(PageRequest.of(0, 2));
    }

    /**
     * Tests getAllUsers with negative page
     */
    @Test
    void testgetAllUsersWNeg() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers(-1, 20));

        assertEquals("Page index must be 0 or greater", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    /**
     * Tests getAllUsers with negative size
     */
    @Test
    void testgetAllUsersWNegSize() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers(0, -1));

        assertEquals("Page size must be between 1 and 100", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    /**
     * Tests getAllUsers with size over 100
     */
    @Test
    void testgetAllUsersWBigSize() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers(0, 101));

        assertEquals("Page size must be between 1 and 100", ex.getMessage());
        verifyNoInteractions(userRepository);
    }

    /**
     * Tests getUserById
     */
    @Test
    void testGetUserById(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponseDTO result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals("newUser", result.displayName());
        assertEquals("email@gmail.com", result.email());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    /**
     * Tests getUserById when the Id does not exist
     */
    @Test
    void testGetUserByIdNonEx(){
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(999L));
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    /**
     * Tests getUserById when the user has a recipe
     */
    @Test
    void testGetUserByIdWithStuff(){
        User user = new User("email@gmail.com", "newUser", "USER");
        Recipe recipe = new Recipe("food", "1 food", user);
        user.getRecipes().add(recipe);
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserResponseDTO result = userService.getUserById(1L);
        assertEquals("email@gmail.com", result.email());
        assertEquals("USER", result.role());
    }

    /**
     * Tests deleteUser
     */
    @Test
    void testDeleteUserById(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));
        userService.deleteUser(1L);
        Mockito.verify(userRepository, Mockito.times(1)).deleteById(1L);
    }

    /**
     * Tests patchUser
     */
    @Test
    void testPatchUser(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        UserResponseDTO result = userService.patchUser(1L, "ADMIN");

        assertEquals("ADMIN", result.role());
        assertEquals("email@gmail.com", result.email());
        assertEquals("newUser", result.displayName());
    }

    /**
     * Tests patch user when user isn't found
     */
    @Test
    void testPatchUserNotFound(){
        Mockito.when(userRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.patchUser(999L, "ADMIN"));
        Mockito.verify(userRepository, Mockito.never()).save(any());
    }

    /**
     * Tests get user when email isn't found
     */
    @Test
    void testGetUserByEmailNotFound(){
        Mockito.when(userRepository.findByEmail("no@gmail.com"))
                .thenReturn(Optional.empty());

        User savedUser = new User("no@gmail.com", "no@gmail.com", "USER");

        Mockito.when(userRepository.save(any(User.class)))
                .thenReturn(savedUser);

        UserResponseDTO result =
                userService.getUserByEmail("no@gmail.com");

        assertNotNull(result);
        assertEquals("no@gmail.com", result.email());
        assertEquals("no@gmail.com", result.displayName());
        assertEquals("USER", result.role());

        Mockito.verify(userRepository).save(any(User.class));
    }
}
