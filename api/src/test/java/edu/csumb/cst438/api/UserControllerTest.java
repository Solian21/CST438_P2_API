package edu.csumb.cst438.api;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import edu.csumb.cst438.api.controller.UserController;
import edu.csumb.cst438.api.entity.User;
import edu.csumb.cst438.api.service.UserService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

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
     * Tests if GET /api/v1/users works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGETUsersWAdmin() throws Exception{
        mockMvc.perform(get("/api/v1/users"))
                .andExpect(status().isOk());
    }

    /**
     * Tests if GET /api/v1/users/{userId} works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testGETUserIdWAdmin() throws Exception{
        User mockUser = new User("email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));
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
        User mockUser = new User("email@gmail.com", "newName", "ADMIN");
        when(userService.patchUser(1L, "ADMIN")).thenReturn(mockUser);

        mockMvc.perform(patch("/api/v1/users/1")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    /**
     * Tests if DELETE /api/v1/users/{userId} works with ADMIN role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "ADMIN")
    void testDELETEUserIdWAdmin() throws Exception{
        User mockUser = new User("email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isNoContent());
    }


    /**
     * Tests if GET /api/v1/users is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGETUsersWUser() throws Exception{
        User mockUser = new User("email@gmail.com", "newName", "USER");
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
        User mockUser = new User("email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));
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
        User mockUser = new User("email@gmail.com", "newName", "ADMIN");
        when(userService.patchUser(1L, "ADMIN")).thenReturn(mockUser);

        mockMvc.perform(patch("/api/v1/users/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"role\":\"ADMIN\"}"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests if DELETE /api/v1/users/{userId} is successfully forbidden from USER role
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testDELETEUserIdWUser() throws Exception{
        User mockUser = new User("email@gmail.com", "newName", "USER");
        when(userService.getUserById(1L)).thenReturn(Optional.of(mockUser));
        mockMvc.perform(delete("/api/v1/users/1"))
                .andExpect(status().isForbidden());
    }

    /**
     * Tests if GET api/v1/users/me is accessible from USER and returns the correct USER
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testGETLoggedInUser() throws Exception{
        User mockUser = new User("email@gmail.com", "user", "USER");
        when(userService.getUserByName("user")).thenReturn(mockUser);
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("email@gmail.com"));
    }

    /**
     * Tests if DELETE api/v1/users/me is accessible from USER and returns the correct USER
     * @throws Exception if there is an exception
     */
    @Test
    @WithMockUser(roles = "USER")
    void testDELETELoggedInUser() throws Exception{
        User mockUser = new User("email@gmail.com", "user", "USER");
        when(userService.getUserByName("user")).thenReturn(mockUser);
        mockMvc.perform(delete("/api/v1/users/me"))
                .andExpect(status().isNoContent());
    }
}
