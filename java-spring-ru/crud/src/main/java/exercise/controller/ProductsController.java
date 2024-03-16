package exercise.controller;

import java.util.List;

import exercise.dto.ProductCreateDTO;
import exercise.dto.ProductDTO;
import exercise.dto.ProductUpdateDTO;
import exercise.mapper.ProductMapper;
import exercise.model.Category;
import exercise.model.Product;
import exercise.repository.CategoryRepository;
import jakarta.validation.ConstraintViolationException;
import org.openapitools.jackson.nullable.JsonNullable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import exercise.exception.ResourceNotFoundException;
import exercise.repository.ProductRepository;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/products")
public class ProductsController {
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private ProductMapper productMapper;

    // BEGIN

    @Autowired
    CategoryRepository categoryRepository;

    @GetMapping()
    public List<ProductDTO> index() {
        return productRepository.findAll().stream().map(productMapper::map).toList();
    }

    @GetMapping("/{id}")
    public ProductDTO show(@PathVariable long id) {
        return productRepository.findById(id).map(productMapper::map).
                orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO create(@RequestBody ProductCreateDTO productCreateDTO) {
        Product model = productMapper.map(productCreateDTO);
        if (null == model.getCategory()) {
            throw new ConstraintViolationException("Bad request", null);
        }

        productRepository.save(model);
        Category categoryModel = categoryRepository.findById(model.getCategory().getId()).
                orElseThrow(() -> new ConstraintViolationException("Bad request", null));
        categoryModel.getProducts().add(model);
        categoryRepository.save(categoryModel);

        return productMapper.map(productRepository.findById(model.getId()).get());
    }

    @PutMapping("/{id}")
    public ProductDTO update(@PathVariable long id, @RequestBody ProductUpdateDTO productUpdateDTO) {
        Product model = productRepository.findById(id).
                orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        JsonNullable<Long> categoryId = productUpdateDTO.getCategoryId();
        if (categoryId.isPresent()) {
            categoryRepository.findById(categoryId.get()).
                    orElseThrow(() -> new ConstraintViolationException("Bad request", null));
        }

        productMapper.update(productUpdateDTO, model);
        productRepository.save(model);

//        if (categoryId.isPresent()) {
//            Category category = categoryRepository.findById(model.getCategory().getId()).get();
//            category.getProducts().remove(model);
//            category.getProducts().add()
//        }

        return productRepository.findById(model.getId()).map(productMapper::map).get();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void remove(@PathVariable long id) {
        productRepository.deleteById(id);
    }
    // END
}
