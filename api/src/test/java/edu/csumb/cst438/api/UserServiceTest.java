package edu.csumb.cst438.api;

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
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verifyNoInteractions;

/**
 * Tests methods in UserService class
 *
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

        User result = userService.getUserByEmail("email@gmail.com");
        assertNotNull(result);
        assertEquals("newUser", result.getDisplayName());
        assertEquals("email@gmail.com", result.getEmail());
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
        Mockito.when(userRepository.findAll(PageRequest.of(0, 20))).thenReturn(page);

        Page<User> result = userService.getAllUsers(0);
        assertNotNull(result);
        assertTrue(result.getContent().contains(mockUser));
        assertTrue(result.getContent().contains(mockUser2));
        Mockito.verify(userRepository, Mockito.times(1)).findAll(PageRequest.of(0, 20));
    }

    /**
     * Tests getAllUsers
     */
    @Test
    void testgetAllUsersWNeg() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.getAllUsers(-1));

        assertEquals("Page index must be 0 or greater", ex.getMessage());
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
//        assertEquals("newUser", result.get().getDisplayName());
//        assertEquals("email@gmail.com", result.get().getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findById(1L);
    }

    /**
     * Tests deleteUser
     */
    @Test
    void testDeleteUserById(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");

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
        Mockito.when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        mockUser = userService.patchUser(1L, "ADMIN");
        UserResponseDTO result = userService.getUserById(1L);
        assertEquals("ADMIN", result.role());
        Mockito.verify(userRepository).save(mockUser);
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
     * Tests LoadUserByUsername
     */
    @Test
    void testLoadUserByUsername(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findByEmail("email@gmail.com")).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByEmail("email@gmail.com");

        assertEquals("newUser", result.getDisplayName());
        assertEquals("email@gmail.com", result.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findByEmail("email@gmail.com");
    }

    /**
     * Tests get user when email isn't found
     */
    @Test
    void testGetUserByEmailNotFound(){
        Mockito.when(userRepository.findByEmail("no@gmail.com")).thenReturn(Optional.empty());
        assertThrows(EntityNotFoundException.class, () -> userService.getUserByEmail("no@gmail.com"));
    }
}
