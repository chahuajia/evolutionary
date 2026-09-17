package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.LedgerEntry;
import java.util.List;

public interface LedgerRepository {
    void append(LedgerEntry entry);

    List<LedgerEntry> findAll();

    /** 按订单号查找相关分录（支付/退款）；订单场景下 refId = orderId。 */
    List<LedgerEntry> findByOrderId(String orderId);

    /** 按业务引用号查找分录（与 findByOrderId 同语义，便于通用调用）。 */
    default List<LedgerEntry> findByRefId(String refId) {
        return findByOrderId(refId);
    }
}
