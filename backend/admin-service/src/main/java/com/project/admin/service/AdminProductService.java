package com.project.admin.service;

import com.project.admin.domain.Product;
import com.project.admin.dto.ProductRequest;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.ProductRepository;
import com.project.common.event.StockUpdatedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminProductService {

    private final ProductRepository productRepository;
    private final AdminEventProducer adminEventProducer;

    @Transactional(readOnly = true)
    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = Product.builder()
                .name(request.getName())
                .price(request.getPrice())
                .stock(request.getStock())
                .category(request.getCategory())
                .description(request.getDescription())
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다: " + id));

        int previousStock = product.getStock();

        product.setName(request.getName());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCategory(request.getCategory());
        product.setDescription(request.getDescription());

        Product saved = productRepository.save(product);

        // 재고 변경 시 이벤트 발행
        if (previousStock != request.getStock()) {
            StockUpdatedEvent event = new StockUpdatedEvent(
                    id, previousStock, request.getStock(), "ADMIN_UPDATE");
            adminEventProducer.publishEvent("product-events", event);
        }

        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "상품을 찾을 수 없습니다: " + id);
        }
        productRepository.deleteById(id);
    }
}
