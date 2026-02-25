package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class OrderStatusChangedEvent extends DomainEvent {

    private final Long orderId;
    private final String previousStatus;
    private final String newStatus;
    private final String changedBy;

    public OrderStatusChangedEvent(Long orderId, String previousStatus, String newStatus, String changedBy) {
        super("ORDER_STATUS_CHANGED", String.valueOf(orderId), "admin-service");
        this.orderId = orderId;
        this.previousStatus = previousStatus;
        this.newStatus = newStatus;
        this.changedBy = changedBy;
    }
}
