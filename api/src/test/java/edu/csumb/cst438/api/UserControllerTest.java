package edu.csumb.cst438.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import edu.csumb.cst438.api.controller.UserController;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.record.UserResponseDTO;
import edu.csumb.cst438.api.service.UserService;
import edu.csumb.cst438.api.repository.UserRepository;


import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.jwt.BadJwtException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.util.List;

/**
 * Tests the UserController.java API routes
 * As well as tests what ADMIN/USER can and can't access
 * Disclaimer: some of these tests were assisted with AI
 */
@WebMvcTest(UserController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class UserControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserService userService;

    /**
     * Mock repository used by SecurityConfig to load the authenticated user's database role
     */
    @MockitoBean
    private UserRepository userRepository;

    /**
     * Mock decoder used to simulate invalid JWT tokens
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    /**
     * Tests if GET /api/v1/users works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGETUsersWAdmin() throws Exception{
        UserResponseDTO mockUser = new UserResponseDTO(1L, "email@gmail.com", "newName", "USER");
        when(userService.getAllUsers(0, 20)).thenReturn(new PageImpl<>(List.of(mockUser)));
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].email").value("email@gmail.com"))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.number").value(0));
    }

    /**
     * Tests if GET /api/v1/users/{userId} works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGETUserIdWAdmin() throws Exception{
        UserResponseDTO mockUser = new UserResponseDTO(1L, "email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(mockUser);
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isOk());
    }

    /**
     * Tests if PATCH /api/v1/users/{userId} works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPATCHUserWAdmin() throws Exception{
        when(userService.patchUser(1L, "ADMIN")).thenReturn(new UserResponseDTO(1L, "email@gmail.com", "newName", "ADMIN"));

        mockMvc.perform(patch("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Tests PATCH if the user don't exist
     * @throws Exception if there's an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPATCHNonExistentUser() throws Exception{
        when(userService.patchUser(999L, "ADMIN"))
                .thenThrow(new EntityNotFoundException("No user found with id 999"));
        mockMvc.perform(patch("/api/v1/users/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.detail").value("No user found with id 999"))
                .andExpect(jsonPath("$.instance").value("/api/v1/users/999"));
    }

    /**
     * Tests PATCH user with an invalid role input
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPATCHUserInvalidRole() throws Exception{
        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMN\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Bad Request"))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.detail").value(org.hamcrest.Matchers.containsString("role")));

        verify(userService, never()).patchUser(any(), any());
    }

    /**
     * Tests if PATCh works with a missing role input
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testPATCHUserMissingRole() throws Exception{
        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400));

        verify(userService, never()).patchUser(any(), any());
    }

    /**
     * Tests if GET users works when you're not authenticated
     * @throws Exception if there is an exception
     */
    @Test
    void testGETUsersUnauthenticated() throws Exception{
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().exists("WWW-Authenticate"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/api/v1/users"));
    }

    /**
     * Verifies that an invalid bearer token returns a 401 response with a problem details JSON bosy and the required WWW-Authenticate header
     * @throws Exception if the MockMvc request fails
     */
    @Test
    void testInvalidBearerTokenReturnsProblemDetails() throws Exception {
        when(jwtDecoder.decode("invalid-token"))
                .thenThrow(new BadJwtException("Invalid bearer token"));

        mockMvc.perform(get("/api/v1/users")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer invalid-token"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(
                        MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(header().string(
                        HttpHeaders.WWW_AUTHENTICATE, "Bearer"))
                .andExpect(jsonPath("$.title").value("Unauthorized"))
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.instance").value("/api/v1/users"));
    }

    /**
     * Tests if GET users works if a user does it
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGETUsersWUserProblemDetails() throws Exception{
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.title").value("Forbidden"))
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.instance").value("/api/v1/users"));
    }

    /**
     * Tests if DELETE /api/v1/users/{userId} works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDELETEUserIdWAdmin() throws Exception{
        UserResponseDTO mockUser = new UserResponseDTO(1L, "email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(mockUser);
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(1L);
    }


    /**
     * Tests if GET /api/v1/users is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGETUsersWUser() throws Exception{
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests if GET /api/v1/users/{userId} is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGETUserIdWUser() throws Exception{
        UserResponseDTO mockUser = new UserResponseDTO(1L, "email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(mockUser);
        mockMvc.perform(get("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests if PATCH /api/v1/users/{userId} is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testPATCHUserWUser() throws Exception{
        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
        verifyNoInteractions(userService);
    }

    /**
     * Tests if DELETE /api/v1/users/{userId} is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testDELETEUserIdWUser() throws Exception{
        UserResponseDTO mockUser = new UserResponseDTO(1L, "email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(mockUser);
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests if GET api/v1/users/me is accessible from USER and returns the correct USER
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(username = "email@gmail.com", roles = "USER")
    void testGETLoggedInUser() throws Exception{
        when(userService.getUserByEmail("email@gmail.com")).thenReturn(new UserResponseDTO(1L, "email@gmail.com", "user", "USER"));
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@gmail.com"));
    }

    /**
     * Tests if DELETE api/v1/users/me is accessible from USER and returns the correct USER
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(username = "email@gmail.com", roles = "USER")
    void testDELETELoggedInUser() throws Exception{
        User mockUser = mock(User.class);
        when(mockUser.getId()).thenReturn(1L);
        when(userService.getUserByEmail("email@gmail.com")).thenReturn(new UserResponseDTO(1L, "email@gmail.com", "user", "USER"));
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());
        verify(userService).deleteUser(1L);
    }
}
