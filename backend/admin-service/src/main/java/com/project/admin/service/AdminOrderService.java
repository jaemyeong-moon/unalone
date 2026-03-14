package com.project.admin.service;

import com.project.admin.domain.Order;
import com.project.admin.exception.InvalidRequestException;
import com.project.admin.exception.ResourceNotFoundException;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.OrderRepository;
import com.project.admin.repository.ProductRepository;
import com.project.common.event.OrderStatusChangedEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Slf4j
@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AdminEventProducer adminEventProducer;

    public Page<Order> getOrders(String status, Pageable pageable) {
        if (StringUtils.hasText(status)) {
            Order.OrderStatus orderStatus = parseOrderStatus(status);
            return orderRepository.findByStatus(orderStatus, pageable);
        }
        return orderRepository.findAll(pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatusStr, String changedBy) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("주문을 찾을 수 없습니다: " + orderId));

        Order.OrderStatus newStatus = parseOrderStatus(newStatusStr);
        String previousStatus = order.getStatus().name();

        // 취소 시 배송중 이전까지만 가능
        if (newStatus == Order.OrderStatus.CANCELLED && !order.isCancellable()) {
            throw new InvalidRequestException("배송중 이후에는 주문을 취소할 수 없습니다");
        }

        // 취소 시 재고 복구
        if (newStatus == Order.OrderStatus.CANCELLED) {
            order.getOrderItems().forEach(item ->
                    productRepository.findById(item.getProductId()).ifPresent(product ->
                            product.restoreStock(item.getQuantity())
                    )
            );
        }

        order.changeStatus(newStatus);

        adminEventProducer.publishEvent(
                "order-events",
                new OrderStatusChangedEvent(orderId, previousStatus, newStatus.name(), changedBy)
        );

        log.info("Order {} status changed: {} -> {}", orderId, previousStatus, newStatus);
        return order;
    }

    private Order.OrderStatus parseOrderStatus(String status) {
        try {
            return Order.OrderStatus.valueOf(status);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException("유효하지 않은 주문 상태: " + status);
        }
    }
}
