package com.project.admin.service;

import com.project.admin.domain.Product;
import com.project.admin.dto.ProductRequest;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.ProductRepository;
import com.project.common.event.StockUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final AdminEventProducer adminEventProducer;

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.name())
                .price(request.price())
                .stock(request.stock())
                .category(request.category())
                .description(request.description())
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id));

        int previousStock = product.getStock();
        product.update(request.name(), request.price(), request.stock(), request.category(), request.description());

        // 재고 변경 시 이벤트 발행
        if (previousStock != request.stock()) {
            adminEventProducer.publishEvent(
                    "product-events",
                    new StockUpdatedEvent(id, previousStock, request.stock(), "ADMIN_UPDATE")
            );
        }

        return product;
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("상품을 찾을 수 없습니다: " + id);
        }
        productRepository.deleteById(id);
    }
}
