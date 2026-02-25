package com.project.admin.service;

import com.project.admin.dto.SalesStatsResponse;
import com.project.admin.repository.OrderRepository;
import com.project.admin.repository.ProductRepository;
import com.project.admin.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminStatsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public SalesStatsResponse getSalesStats() {
        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        LocalDateTime todayEnd = LocalDate.now().atTime(LocalTime.MAX);
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        BigDecimal todaySales = orderRepository.sumTotalAmountByCreatedAtBetween(todayStart, todayEnd);
        long todayOrderCount = orderRepository.countByCreatedAtBetween(todayStart, todayEnd);

        BigDecimal monthlySales = orderRepository.sumTotalAmountByCreatedAtBetween(monthStart, todayEnd);
        long monthlyOrderCount = orderRepository.countByCreatedAtBetween(monthStart, todayEnd);

        long totalUsers = userRepository.count();
        long totalProducts = productRepository.count();

        return SalesStatsResponse.builder()
                .todaySales(todaySales)
                .todayOrderCount(todayOrderCount)
                .monthlySales(monthlySales)
                .monthlyOrderCount(monthlyOrderCount)
                .totalUsers(totalUsers)
                .totalProducts(totalProducts)
                .build();
    }
}
