package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.LedgerEntry;
import java.util.List;

public interface LedgerRepository {
    void append(LedgerEntry entry);

    List<LedgerEntry> findAll();

    /** 按订单号查找相关分录（支付/退款）。 */
    List<LedgerEntry> findByOrderId(String orderId);
}
