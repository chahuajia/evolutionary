package com.evolutionary.commerce.application;

import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;

public interface AccountRepository {
    Account get(String accountId);

    Account findUserBalance(String userId, Currency currency);

    /** 用户积分账户（按币种单位对齐，1 积分 = 1 分）。 */
    Account findUserPoints(String userId, Currency currency);

    Account findOrgSettlement(String orgId, Currency currency);

    void save(Account account);
}
