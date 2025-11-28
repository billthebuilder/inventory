package com.example.inventory.repository;

import com.example.inventory.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByDeletedFalse(Pageable pageable);

    @Query("select p from Product p where p.deleted=false and (:q is null or lower(p.name) like lower(concat('%', :q, '%')) or lower(p.category) like lower(concat('%', :q, '%'))) ")
    Page<Product> search(String q, Pageable pageable);
}
