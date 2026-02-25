package com.project.common.event;

import lombok.Getter;
import lombok.ToString;

@Getter
@ToString(callSuper = true)
public class StockUpdatedEvent extends DomainEvent {

    private final Long productId;
    private final int previousStock;
    private final int newStock;
    private final String reason;

    public StockUpdatedEvent(Long productId, int previousStock, int newStock, String reason) {
        super("STOCK_UPDATED", String.valueOf(productId), "admin-service");
        this.productId = productId;
        this.previousStock = previousStock;
        this.newStock = newStock;
        this.reason = reason;
    }
}
