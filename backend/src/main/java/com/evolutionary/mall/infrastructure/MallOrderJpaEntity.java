package com.evolutionary.mall.infrastructure;



import com.evolutionary.commerce.domain.Order;
import com.evolutionary.mall.domain.MallOrder;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mall_orders")
public class MallOrderJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String merchantOrgId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MallOrder.Status status;

    private Long usePointsCents;

    private Long useBalanceCents;

    private Long discountCents;

    private String discountCurrency;

    @Column(nullable = false)
    private long paidCents;

    @Column(nullable = false)
    private String paidCurrency;

    @Column(nullable = false)
    private Instant createdAt;

    private Instant paidAt;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "mall_order_lines", joinColumns = @JoinColumn(name = "order_id"))
    private List<MallOrderLineJpaEmbeddable> lines = new ArrayList<>();

    protected MallOrderJpaEntity() {}

    public MallOrderJpaEntity(
            String id,
            String userId,
            String merchantOrgId,
            MallOrder.Status status,
            Long usePointsCents,
            Long useBalanceCents,
            Long discountCents,
            String discountCurrency,
            long paidCents,
            String paidCurrency,
            Instant createdAt,
            Instant paidAt,
            List<MallOrderLineJpaEmbeddable> lines) {
        this.id = id;
        this.userId = userId;
        this.merchantOrgId = merchantOrgId;
        this.status = status;
        this.usePointsCents = usePointsCents;
        this.useBalanceCents = useBalanceCents;
        this.discountCents = discountCents;
        this.discountCurrency = discountCurrency;
        this.paidCents = paidCents;
        this.paidCurrency = paidCurrency;
        this.createdAt = createdAt;
        this.paidAt = paidAt;
        this.lines = new ArrayList<>(lines);
    }

    public String getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public String getMerchantOrgId() {
        return merchantOrgId;
    }

    public MallOrder.Status getStatus() {
        return status;
    }

    public Long getUsePointsCents() {
        return usePointsCents;
    }

    public Long getUseBalanceCents() {
        return useBalanceCents;
    }

    public Long getDiscountCents() {
        return discountCents;
    }

    public String getDiscountCurrency() {
        return discountCurrency;
    }

    public long getPaidCents() {
        return paidCents;
    }

    public String getPaidCurrency() {
        return paidCurrency;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public Instant getPaidAt() {
        return paidAt;
    }

    public List<MallOrderLineJpaEmbeddable> getLines() {
        return lines;
    }
}
