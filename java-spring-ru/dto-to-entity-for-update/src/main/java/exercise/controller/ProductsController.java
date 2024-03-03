package exercise.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.Optional;

import exercise.model.Product;
import exercise.repository.ProductRepository;
import exercise.dto.ProductDTO;
import exercise.dto.ProductCreateDTO;
import exercise.dto.ProductUpdateDTO;
import exercise.exception.ResourceNotFoundException;

@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductRepository productRepository;

    @GetMapping(path = "")
    public List<ProductDTO> index() {
        var products = productRepository.findAll();
        return products.stream()
                .map(this::toDTO)
                .toList();
    }

    @GetMapping(path = "/{id}")
    public ProductDTO show(@PathVariable long id) {

        var product =  productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Product with id " + id + " not found"));
        var productDTO = toDTO(product);
        return productDTO;
    }

    @PostMapping(path = "")
    @ResponseStatus(HttpStatus.CREATED)
    public ProductDTO create(@RequestBody ProductCreateDTO productData) {
        var product = toEntity(productData);
        productRepository.save(product);
        var productDto = toDTO(product);
        return productDto;
    }

    // BEGIN
    @PutMapping(path = "/{id}")
    public ResponseEntity<ProductDTO> update(@RequestBody ProductUpdateDTO productUpdateDTO, @PathVariable Long id) {
        return productRepository.findById(id).map(p -> {
            updateToEntity(productUpdateDTO, p);
            p = productRepository.save(p);
            return ResponseEntity.status(HttpStatus.OK).body(toDTO(p));
        }).orElseGet( () -> ResponseEntity.status(HttpStatus.NOT_FOUND).body(new ProductDTO()));
    }

    private void updateToEntity(ProductUpdateDTO productUpdateDTO, Product product) {
        product.setPrice(productUpdateDTO.getPrice());
        product.setTitle(productUpdateDTO.getTitle());
    }
    // END

    private Product toEntity(ProductCreateDTO productDto) {
        var product = new Product();
        product.setTitle(productDto.getTitle());
        product.setPrice(productDto.getPrice());
        product.setVendorCode(productDto.getVendorCode());
        return product;
    }

    private ProductDTO toDTO(Product product) {
        var dto = new ProductDTO();
        dto.setId(product.getId());
        dto.setTitle(product.getTitle());
        dto.setPrice(product.getPrice());
        dto.setVendorCode(product.getVendorCode());
        dto.setCreatedAt(product.getCreatedAt());
        dto.setUpdatedAt(product.getUpdatedAt());
        return dto;
    }
}
