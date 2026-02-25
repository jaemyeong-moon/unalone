package com.project.admin.service;

import com.project.admin.domain.Order;
import com.project.admin.domain.Product;
import com.project.admin.kafka.producer.AdminEventProducer;
import com.project.admin.repository.OrderRepository;
import com.project.admin.repository.ProductRepository;
import com.project.common.event.OrderStatusChangedEvent;
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
public class AdminOrderService {

    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final AdminEventProducer adminEventProducer;

    @Transactional(readOnly = true)
    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Order> getOrdersByStatus(Order.OrderStatus status, Pageable pageable) {
        return orderRepository.findByStatus(status, pageable);
    }

    @Transactional
    public Order updateOrderStatus(Long orderId, String newStatusStr, String changedBy) {
        Order order = orderRepository.findByIdWithItems(orderId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "주문을 찾을 수 없습니다: " + orderId));

        Order.OrderStatus newStatus;
        try {
            newStatus = Order.OrderStatus.valueOf(newStatusStr);
        } catch (IllegalArgumentException e) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "유효하지 않은 주문 상태: " + newStatusStr);
        }

        String previousStatus = order.getStatus().name();

        // 취소 시 배송중 이전까지만 가능
        if (newStatus == Order.OrderStatus.CANCELLED && !order.isCancellable()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "배송중 이후에는 주문을 취소할 수 없습니다");
        }

        // 취소 시 재고 복구
        if (newStatus == Order.OrderStatus.CANCELLED) {
            order.getOrderItems().forEach(item -> {
                productRepository.findById(item.getProductId()).ifPresent(product -> {
                    product.setStock(product.getStock() + item.getQuantity());
                    productRepository.save(product);
                });
            });
        }

        order.setStatus(newStatus);
        orderRepository.save(order);

        // 이벤트 발행
        OrderStatusChangedEvent event = new OrderStatusChangedEvent(orderId, previousStatus, newStatus.name(), changedBy);
        adminEventProducer.publishEvent("order-events", event);

        log.info("Order {} status changed: {} -> {}", orderId, previousStatus, newStatus);
        return order;
    }
}
