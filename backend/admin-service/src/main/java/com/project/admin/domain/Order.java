package com.project.admin.domain;

import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "orders")
@Getter
@NoArgsConstructor
public class Order extends BaseEntity {

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OrderStatus status = OrderStatus.PENDING;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();

    @Builder
    private Order(Long userId, OrderStatus status, BigDecimal totalAmount) {
        this.userId = userId;
        this.status = status != null ? status : OrderStatus.PENDING;
        this.totalAmount = totalAmount;
    }

    public boolean isCancellable() {
        return this.status == OrderStatus.PENDING || this.status == OrderStatus.ACCEPTED;
    }

    /**
     * 주문 상태 변경 도메인 메서드
     */
    public void changeStatus(OrderStatus newStatus) {
        this.status = newStatus;
    }

    public enum OrderStatus {
        PENDING, ACCEPTED, PROCESSING, SHIPPING, COMPLETED, CANCELLED
    }
}
