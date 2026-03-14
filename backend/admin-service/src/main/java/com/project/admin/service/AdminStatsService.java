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
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class AdminStatsService {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public SalesStatsResponse getSalesStats() {
        LocalDate today = LocalDate.now();
        LocalDateTime todayStart = today.atStartOfDay();
        LocalDateTime todayEnd = today.atTime(LocalTime.MAX);
        LocalDateTime monthStart = today.withDayOfMonth(1).atStartOfDay();

        BigDecimal todaySales = orderRepository.sumTotalAmountByCreatedAtBetween(todayStart, todayEnd);
        BigDecimal monthlySales = orderRepository.sumTotalAmountByCreatedAtBetween(monthStart, todayEnd);

        return new SalesStatsResponse(
                todaySales,
                orderRepository.countByCreatedAtBetween(todayStart, todayEnd),
                monthlySales,
                orderRepository.countByCreatedAtBetween(monthStart, todayEnd),
                userRepository.count(),
                productRepository.count()
        );
    }
}
