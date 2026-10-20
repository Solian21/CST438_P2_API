package edu.csumb.cst438.api.service;

import edu.csumb.cst438.api.entity.Ingredient;
import edu.csumb.cst438.api.record.IngredientCreate;
import edu.csumb.cst438.api.record.IngredientResponse;
import edu.csumb.cst438.api.repository.IngredientRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional
public class IngredientService {

    private final IngredientRepository ingredientRepository;

    public IngredientService(IngredientRepository ingredientRepository) {
        this.ingredientRepository = ingredientRepository;
    }

    @Transactional(readOnly = true)
    public Page<IngredientResponse> listIngredients(Pageable pageable) {
        return ingredientRepository
                .findAll(pageable)
                .map(IngredientResponse::from);
    }

    @Transactional(readOnly = true)
    public Page<IngredientResponse> list(int page, int size) {
        return listIngredients(PageRequest.of(page, size));
    }

    public IngredientResponse create(IngredientCreate request) {
        if (ingredientRepository.existsByIngredientNameIgnoreCase(
                request.ingredientName())) {

            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ingredient already exists"
            );
        }

        Ingredient ingredient =
                new Ingredient(request.ingredientName());

        try {
            Ingredient savedIngredient =
                    ingredientRepository.saveAndFlush(ingredient);

            return IngredientResponse.from(savedIngredient);

        } catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Ingredient already exists",
                    exception
            );
        }
    }
}