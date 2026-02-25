package com.project.admin.controller;

import com.project.admin.domain.Order;
import com.project.admin.dto.OrderStatusRequest;
import com.project.admin.service.AdminOrderService;
import com.project.common.dto.ApiResponse;
import com.project.common.dto.PageResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin/orders")
@RequiredArgsConstructor
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<Order>>> getAllOrders(
            @RequestParam(required = false) String status,
            @PageableDefault(size = 20) Pageable pageable) {
        Page<Order> orders;
        if (status != null && !status.isBlank()) {
            orders = adminOrderService.getOrdersByStatus(Order.OrderStatus.valueOf(status), pageable);
        } else {
            orders = adminOrderService.getAllOrders(pageable);
        }
        return ResponseEntity.ok(ApiResponse.ok(PageResponse.from(orders)));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<Order>> updateOrderStatus(
            @PathVariable Long id,
            @Valid @RequestBody OrderStatusRequest request) {
        Order order = adminOrderService.updateOrderStatus(id, request.getStatus(), "admin");
        return ResponseEntity.ok(ApiResponse.ok(order, "주문 상태가 변경되었습니다"));
    }
}
