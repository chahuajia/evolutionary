package com.evolutionary.credit.application;

import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.Order;
import com.evolutionary.credit.domain.CreditLedgerDebt;
import com.evolutionary.credit.domain.CreditProfile;

/** 信用购结果。 */
public record CreditPurchaseResult(
        Order order, Entitlement entitlement, CreditLedgerDebt debt, CreditProfile profile) {}
