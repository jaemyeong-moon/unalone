package com.project.admin.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@NoArgsConstructor
public class Product extends BaseEntity {

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private int stock = 0;

    @Column(length = 50)
    private String category;

    @Column(length = 1000)
    private String description;

    @Version
    private Long version;

    @Builder
    private Product(String name, BigDecimal price, int stock, String category, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.description = description;
    }

    /**
     * 상품 정보 일괄 수정 도메인 메서드
     */
    public void update(String name, BigDecimal price, int stock, String category, String description) {
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.category = category;
        this.description = description;
    }

    /**
     * 취소된 주문의 재고 복구 도메인 메서드
     */
    public void restoreStock(int quantity) {
        this.stock += quantity;
    }
}
