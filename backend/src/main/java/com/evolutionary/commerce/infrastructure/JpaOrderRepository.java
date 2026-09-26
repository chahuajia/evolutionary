package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.application.OrderRepository;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.Order;
import org.springframework.stereotype.Component;

/** 订单 JPA 适配。 */
@Component
public final class JpaOrderRepository implements OrderRepository {

    private final OrderJpaRepository jpa;

    public JpaOrderRepository(OrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Order get(String orderId) {
        return jpa.findById(orderId)
                .map(JpaOrderRepository::toDomain)
                .orElseThrow(() -> new IllegalArgumentException("order not found: " + orderId));
    }

    @Override
    public void save(Order order) {
        jpa.save(
                new OrderJpaEntity(
                        order.id(),
                        order.userId(),
                        order.productId(),
                        order.orgId(),
                        order.status(),
                        order.paidAmount().cents(),
                        order.paidAmount().currency().name(),
                        order.createdAt(),
                        order.paidAt(),
                        order.refundedAt()));
    }

    private static Order toDomain(OrderJpaEntity row) {
        Money paid =
                new Money(row.getPaidCents(), Currency.valueOf(row.getPaidCurrency()));
        return Order.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getProductId(),
                row.getOrgId(),
                row.getStatus(),
                paid,
                row.getCreatedAt(),
                row.getPaidAt(),
                row.getRefundedAt());
    }
}
