package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Entitlement;
import com.evolutionary.commerce.domain.Order;

public record PurchaseResult(Order order, Entitlement entitlement) {}
