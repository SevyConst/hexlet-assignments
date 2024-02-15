package exercise.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.util.Streamable;
import org.springframework.stereotype.Repository;
import java.util.List;
import exercise.model.Product;

import org.springframework.data.domain.Sort;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    // BEGIN
    Streamable<Product> findByPriceBetween(Integer min, Integer max);
    Streamable<Product> findByPriceGreaterThanEqual(Integer min);

    // END
}
