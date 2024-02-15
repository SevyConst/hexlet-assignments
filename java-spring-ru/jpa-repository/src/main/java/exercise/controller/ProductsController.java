package exercise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.data.domain.Sort;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import exercise.model.Product;
import exercise.repository.ProductRepository;
import exercise.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductRepository productRepository;

    // BEGIN
    @GetMapping()
    public List<Product> index(@RequestParam(defaultValue = "0") Integer min,
                               @RequestParam(defaultValue = "0") Integer max) {
        if (max == 0) {
            return productRepository.findByPriceGreaterThanEqual(min).stream().
                    sorted(Comparator.comparingInt(Product::getPrice)).toList();
        }

        return productRepository.findByPriceBetween(min, max).stream().
                sorted(Comparator.comparingInt(Product::getPrice)).toList();
    }
    // END

    @GetMapping(path = "/{id}")
    public Product show(@PathVariable long id) {

        var product =  productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));

        return product;
    }
}
