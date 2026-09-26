package com.evolutionary.mall.infrastructure;

import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.commerce.domain.Money;
import com.evolutionary.commerce.domain.PaymentIntent;
import com.evolutionary.mall.application.MallOrderRepository;
import com.evolutionary.mall.domain.MallOrder;
import com.evolutionary.mall.domain.MallOrderLine;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Component;

/** 商城订单 JPA 适配。 */
@Component
public final class JpaMallOrderRepository implements MallOrderRepository {

    private final MallOrderJpaRepository jpa;

    public JpaMallOrderRepository(MallOrderJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Optional<MallOrder> findById(String orderId) {
        return jpa.findById(orderId).map(JpaMallOrderRepository::toDomain);
    }

    @Override
    public void save(MallOrder order) {
        jpa.save(toEntity(order));
    }

    private static MallOrderJpaEntity toEntity(MallOrder order) {
        PaymentIntent intent = order.paymentIntent();
        Money discount = order.discountTotal();
        List<MallOrderLineJpaEmbeddable> lineRows =
                order.lines().stream()
                        .map(
                                line ->
                                        new MallOrderLineJpaEmbeddable(
                                                line.skuId(),
                                                line.qty(),
                                                line.unitPrice().cents(),
                                                line.unitPrice().currency().name()))
                        .toList();
        return new MallOrderJpaEntity(
                order.id(),
                order.userId(),
                order.merchantOrgId(),
                order.status(),
                intent == null ? null : intent.usePointsCents(),
                intent == null ? null : intent.useBalanceCents(),
                discount == null ? null : discount.cents(),
                discount == null ? null : discount.currency().name(),
                order.paidAmount().cents(),
                order.paidAmount().currency().name(),
                order.createdAt(),
                order.paidAt(),
                lineRows);
    }

    private static MallOrder toDomain(MallOrderJpaEntity row) {
        List<MallOrderLine> lines =
                row.getLines().stream()
                        .map(
                                line ->
                                        new MallOrderLine(
                                                line.getSkuId(),
                                                line.getQty(),
                                                new Money(
                                                        line.getUnitPriceCents(),
                                                        Currency.valueOf(
                                                                line.getUnitPriceCurrency()))))
                        .toList();
        PaymentIntent intent = null;
        if (row.getUsePointsCents() != null && row.getUseBalanceCents() != null) {
            intent = new PaymentIntent(row.getUsePointsCents(), row.getUseBalanceCents());
        }
        Money discount = null;
        if (row.getDiscountCents() != null && row.getDiscountCurrency() != null) {
            discount =
                    new Money(row.getDiscountCents(), Currency.valueOf(row.getDiscountCurrency()));
        }
        Money paid = new Money(row.getPaidCents(), Currency.valueOf(row.getPaidCurrency()));
        return MallOrder.rehydrate(
                row.getId(),
                row.getUserId(),
                row.getMerchantOrgId(),
                lines,
                row.getStatus(),
                intent,
                discount,
                paid,
                row.getCreatedAt(),
                row.getPaidAt());
    }
}
