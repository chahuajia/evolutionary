package com.evolutionary.commerce.infrastructure;

import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AccountJpaRepository extends JpaRepository<AccountJpaEntity, String> {

    Optional<AccountJpaEntity> findFirstByOwnerTypeAndOwnerIdAndTypeAndCurrency(
            AccountOwnerType ownerType, String ownerId, AccountType type, Currency currency);
}
