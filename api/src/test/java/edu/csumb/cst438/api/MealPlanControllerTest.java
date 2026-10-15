package edu.csumb.cst438.api;

import edu.csumb.cst438.api.controller.MealPlanController;
import edu.csumb.cst438.api.record.MealPlanResponse;
import edu.csumb.cst438.api.service.MealPlanService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.web.server.ResponseStatusException;

import java.security.Principal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

// TODO(#48): once Spring Security is on the classpath, use @WithMockUser("owner@example.com")
//  instead of .principal(...) and add a "no credentials -> 401 problem+json" test.
@WebMvcTest(MealPlanController.class)
@ActiveProfiles("test")
public class MealPlanControllerTest {
    private static final String EMAIL = "owner@example.com";
    private static final Principal USER = () -> EMAIL;
    private static final LocalDate WEEK = LocalDate.of(2026, 10, 5);

    @Autowired
    private MockMvc mvc;

    @MockitoBean
    private MealPlanService mealPlanService;

    private static MockHttpServletRequestBuilder json(MockHttpServletRequestBuilder request, String body) {
        return request.principal(USER).contentType(MediaType.APPLICATION_JSON).content(body);
    }

    @Test
    void listReturnsPageShape() throws Exception {
        when(mealPlanService.list(EMAIL, 0, 20)).thenReturn(
                new PageImpl<>(List.of(new MealPlanResponse(67L, WEEK, List.of())), PageRequest.of(0, 20), 1));

        mvc.perform(get("/api/v1/meal-plans").principal(USER))
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
    void listRejectsBadPaging() throws Exception {
        mvc.perform(get("/api/v1/meal-plans?size=101").principal(USER))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(get("/api/v1/meal-plans?page=-1").principal(USER))
                .andExpect(status().isBadRequest());
        mvc.perform(get("/api/v1/meal-plans?size=0").principal(USER))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(mealPlanService.create(EMAIL, WEEK)).thenReturn(new MealPlanResponse(67L, WEEK, List.of()));

        mvc.perform(json(post("/api/v1/meal-plans"), "{\"weekStart\":\"2026-10-05\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/meal-plans/67"))
                .andExpect(jsonPath("$.entries").isEmpty());
    }

    @Test
    void createRejectsMissingOrInvalidDate() throws Exception {
        mvc.perform(json(post("/api/v1/meal-plans"), "{}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON));
        mvc.perform(json(post("/api/v1/meal-plans"), "{\"weekStart\":\"2026-13-40\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    void updateChangesWeek() throws Exception {
        LocalDate next = LocalDate.of(2026, 10, 12);
        when(mealPlanService.update(EMAIL, 67L, next)).thenReturn(new MealPlanResponse(67L, next, List.of()));

        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{\"weekStart\":\"2026-10-12\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.weekStart").value("2026-10-12"));
    }

    @Test
    void updateRejectsEmptyOrInvalidBody() throws Exception {
        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{}"))
                .andExpect(status().isBadRequest());
        mvc.perform(json(patch("/api/v1/meal-plans/67"), "{\"weekStart\":\"not-a-date\"}"))
                .andExpect(status().isBadRequest());
        verifyNoInteractions(mealPlanService);
    }

    @Test
    void deleteReturns204() throws Exception {
        mvc.perform(delete("/api/v1/meal-plans/67").principal(USER))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));
        verify(mealPlanService).delete(EMAIL, 67L);
    }

    @Test
    void notFoundIsProblemDetails() throws Exception {
        when(mealPlanService.get(eq(EMAIL), any())).thenThrow(
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Meal plan 99 not found"));

        mvc.perform(get("/api/v1/meal-plans/99").principal(USER))
                .andExpect(status().isNotFound())
                .andExpect(content().contentType(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.title").value("Not Found"))
                .andExpect(jsonPath("$.detail").value("Meal plan 99 not found"));
    }
}
