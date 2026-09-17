package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;

public interface AccountRepository {
    Account get(String accountId);

    Account findUserBalance(String userId, Currency currency);

    Account findOrgSettlement(String orgId, Currency currency);

    void save(Account account);
}
