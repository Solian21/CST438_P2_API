package edu.csumb.cst438.api;

import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.repository.UserRepository;
import edu.csumb.cst438.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

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
        Mockito.when(userRepository.findByDisplayName("newUser")).thenReturn(Optional.of(mockUser));

        User result = userService.getUserByName("newUser");
        assertNotNull(result);
        assertEquals("newUser", result.getDisplayName());
        assertEquals("email@gmail.com", result.getEmail());
        Mockito.verify(userRepository, Mockito.times(1)).findByDisplayName("newUser");
    }

    /**
     * Tests getAllUsers
     */
    @Test
    void testGetAllUsers(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        User mockUser2 = new User("gmail@gmail.com", "newerUser", "USER");
        Mockito.when(userRepository.findUsersInPage(PageRequest.of(0, 20))).thenReturn(List.of(mockUser, mockUser2));

        List<User> result = userService.getAllUsers(0);
        assertNotNull(result);
        assertTrue(result.contains(mockUser));
        assertTrue(result.contains(mockUser2));
        Mockito.verify(userRepository, Mockito.times(1)).findUsersInPage(PageRequest.of(0, 20));
    }

    /**
     * Tests getUserById
     */
    @Test
    void testGetUserById(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findById(1L)).thenReturn(Optional.of(mockUser));

        Optional<User> result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals("newUser", result.get().getDisplayName());
        assertEquals("email@gmail.com", result.get().getEmail());
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

        mockUser = userService.patchUser(1L, "ADMIN");
        Optional<User> result = userService.getUserById(1L);
        assertNotNull(result);
        assertEquals("newUser", result.get().getDisplayName());
        assertEquals("ADMIN", result.get().getRole());
    }

    /**
     * Tests LoadUserByUsername
     */
    @Test
    void testLoadUserByUsername(){
        User mockUser = new User("email@gmail.com", "newUser", "USER");
        Mockito.when(userRepository.findByDisplayName("newUser")).thenReturn(Optional.of(mockUser));

        UserDetails details = userService.loadUserByUsername("newUser");

        assertEquals("newUser", details.getUsername());
        assertEquals("email@gmail.com", details.getPassword());
        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_USER")));
    }

    /**
     * Tests LoadUserByUsername to make sure that the ADMIN role is ADMIN authority
     */
    @Test
    void testLoadUserByUsernameMapsAdmin(){
        User mockUser = new User("email@gmail.com", "newAdmin", "ADMIN");
        Mockito.when(userRepository.findByDisplayName("newAdmin")).thenReturn(Optional.of(mockUser));

        UserDetails details = userService.loadUserByUsername("newAdmin");

        assertTrue(details.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")));
    }
}
