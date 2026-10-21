package edu.csumb.cst438.api;

import edu.csumb.cst438.api.controller.MealPlanController;
import edu.csumb.cst438.api.entity.mealType;
import edu.csumb.cst438.api.record.MealPlanEntryPatch;
import edu.csumb.cst438.api.record.MealPlanEntryResponse;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.service.MealPlanService;
import edu.csumb.cst438.api.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


// TODO(#48): once Spring Security is on the classpath, use @WithMockUser("owner@example.com")
//  instead of .principal(...) and add a "no credentials -> 401 problem+json" test.
@WebMvcTest(MealPlanController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
public class MealPlanControllerTest {
    private static final String EMAIL = "owner@example.com";
    private static final Principal USER = () -> EMAIL;
    private static final LocalDate WEEK = LocalDate.of(2026, 10, 5);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    /**
     * Mock repository used by SecurityConfig to load the authenticated user's database role
     */
    @MockitoBean
    private UserRepository userRepository;

    /**
     * Mock Jwtdecoder used to simulate valid and invalid JWT tokens without contacting Google's athentication servers
     */
    @MockitoBean
    private JwtDecoder jwtDecoder;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    @WithMockUser("owner@example.com")
    void listReturnsPageShape() throws Exception {
        when(mealPlanService.list(EMAIL, 0, 20)).thenReturn(
                new PageImpl<>(List.of(new MealPlanResponse(67L, WEEK, List.of())), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/meal-plans"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(67))
                .andExpect(jsonPath("$.content[0].weekStart").value("2026-10-05"))
                .andExpect(jsonPath("$.content[0].entries").isArray())
                .andExpect(jsonPath("$.page.size").value(20))
                .andExpect(jsonPath("$.page.number").value(0))
                .andExpect(jsonPath("$.page.totalElements").value(1))
                .andExpect(jsonPath("$.page.totalPages").value(1));
    }

    @Test
    @WithMockUser("owner@example.com")
    void listRejectsBadPaging() throws Exception {
        mvc.perform(get("/api/v1/meal-plans?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/v1/meal-plans?page=-1"))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/meal-plans?size=0"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    @WithMockUser("owner@example.com")
    void createReturns201WithLocation() throws Exception {
        when(mealPlanService.create(EMAIL, WEEK)).thenReturn(new MealPlanResponse(67L, WEEK, List.of()));

        mvc.perform(json(post("/api/v1/meal-plans"), "{\"weekStart\":\"2026-10-05\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/meal-plans/67"))
                .andExpect(jsonPath("$.entries").isEmpty());
    }

    @Test
    @WithMockUser("owner@example.com")
    void createRejectsMissingOrInvalidDate() throws Exception {
        mvc.perform(json(post("/api/v1/meal-plans"), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(json(post("/api/v1/meal-plans"), "{\"weekStart\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    @WithMockUser("owner@example.com")
    void updateChangesWeek() throws Exception {
        LocalDate next = LocalDate.of(2026, 10, 12);
        when(mealPlanService.update(EMAIL, 67L, next)).thenReturn(new MealPlanResponse(67L, next, List.of()));

        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{\"weekStart\":\"2026-10-12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-10-12"));
    }

    @Test
    @WithMockUser("owner@example.com")
    void updateRejectsEmptyOrInvalidBody() throws Exception {
        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{\"weekStart\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    @WithMockUser("owner@example.com")
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/v1/meal-plans/67"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(mealPlanService).delete(EMAIL, 67L);
    }

    @Test
    @WithMockUser("owner@example.com")
    void notFoundIsProblemDetails() throws Exception {
        when(mealPlanService.get(eq(EMAIL), any())).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan 99 not found"));

        mvc.perform(get("/api/v1/meal-plans/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Meal plan 99 not found"));
    }

    @Test
    @WithMockUser("owner@example.com")
    void updateEntrySendsOnlyChangedFields() throws Exception {
        when(mealPlanService.updateEntry(EMAIL, 67L, 5L, new MealPlanEntryPatch(null, null, mealType.LUNCH)))
                .thenReturn(new MealPlanEntryResponse(5L, 67L, 42L, WEEK, mealType.LUNCH));

        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/5"), "{\"mealType\":\"LUNCH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5))
                .andExpect(jsonPath("$.mealPlanId").value(67))
                .andExpect(jsonPath("$.recipeId").value(42))
                .andExpect(jsonPath("$.mealType").value("LUNCH"));
    }

    @Test
    @WithMockUser("owner@example.com")
    void updateEntryRejectsEmptyOrInvalidBody() throws Exception {
        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/5"), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/5"), "{\"mealType\":\"BRUNCH\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/5"), "{\"plannedDate\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    @WithMockUser("owner@example.com")
    void deleteEntryReturns204() throws Exception {
        mvc.perform(delete("/api/v1/meal-plans/67/entries/5"))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(mealPlanService).deleteEntry(EMAIL, 67L, 5L);
    }

    @Test
    @WithMockUser("owner@example.com")
    void missingEntryIsNotFound() throws Exception {
        when(mealPlanService.updateEntry(eq(EMAIL), eq(67L), eq(99L), any())).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan entry 99 not found"));
        doThrow(new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan entry 99 not found"))
                .when(mealPlanService).deleteEntry(EMAIL, 67L, 99L);

        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/99"), "{\"mealType\":\"LUNCH\"}"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(delete("/api/v1/meal-plans/67/entries/99"))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
    }

    @Test
    void entryRoutesRequireToken() throws Exception {
        mvc.perform(json(patch("/api/v1/meal-plans/67/entries/5"), "{\"mealType\":\"LUNCH\"}"))
                .andExpect(status().isUnauthorized());
        mvc.perform(delete("/api/v1/meal-plans/67/entries/5"))
                .andExpect(status().isUnauthorized());
        verifyNoInteractions(mealPlanService);
    }
}
