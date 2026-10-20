package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.record.IngredientResponse;
import edu.csumb.cst438.api.entity.Ingredient;
import edu.csumb.cst438.api.repository.IngredientRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class IngredientServiceTest {

    @Mock
    private IngredientRepository ingredientRepository;

    @InjectMocks
    private IngredientService ingredientService;

    @Test
    void listIngredientsReturnsIngredientResponses() {
        Pageable pageable = PageRequest.of(0, 20);
        Ingredient ingredient = new Ingredient("Flour");

        Page<Ingredient> ingredientPage =
                new PageImpl<>(List.of(ingredient));

        when(ingredientRepository.findAll(pageable))
                .thenReturn(ingredientPage);

        Page<IngredientResponse> result =
                ingredientService.listIngredients(pageable);

        assertEquals(1, result.getContent().size());
        assertEquals(
                "Flour",
                result.getContent().get(0).getIngredientName()
        );

        verify(ingredientRepository).findAll(pageable);
    }
}