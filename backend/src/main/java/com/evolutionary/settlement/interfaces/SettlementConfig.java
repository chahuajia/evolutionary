package com.evolutionary.settlement.interfaces;

import com.evolutionary.commerce.application.AccountRepository;
import com.evolutionary.commerce.application.LedgerRepository;
import com.evolutionary.commerce.domain.Account;
import com.evolutionary.commerce.domain.AccountOwnerType;
import com.evolutionary.commerce.domain.AccountType;
import com.evolutionary.commerce.domain.Currency;
import com.evolutionary.settlement.application.AccrueOnOrderCompleted;
import com.evolutionary.settlement.application.ProfitShareAccrualRepository;
import com.evolutionary.settlement.application.ProfitSharingRuleRepository;
import com.evolutionary.settlement.application.ReferralBindingRepository;
import com.evolutionary.settlement.application.ReverseAccrualsOnRefund;
import com.evolutionary.settlement.application.RunSettlementBatch;
import com.evolutionary.settlement.application.SettlementBatchRepository;
import com.evolutionary.settlement.domain.ProfitSharingRule;
import com.evolutionary.settlement.domain.ProfitSplit;
import com.evolutionary.settlement.infrastructure.InMemoryProfitShareAccrualRepository;
import com.evolutionary.settlement.infrastructure.InMemoryReferralBindingRepository;
import com.evolutionary.settlement.infrastructure.InMemorySettlementBatchRepository;
import java.time.Instant;
import java.util.List;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 结算应用层装配（切片21a / phase-4 Accrue · Reverse · Batch）。
 *
 * <p>清算户优先复用种子 {@code ACC-CLR}；本配置另开 {@code ACC-SETTLE-CLR} 并注入足额余额，避免与信用还款抢余额。
 */
@Configuration
public class SettlementConfig {

    /** ProfitSharingRuleRepository → {@code JpaProfitSharingRuleRepository}（表 profit_sharing_rules）。 */

    /** 分润清算户（SETTLEMENT）；RunSettlementBatch 借方。 */
    public static final String CLEARING_ACCOUNT_ID = "ACC-SETTLE-CLR";

    public static final String SELLER_ORG_ID = "ORG-L2";
    public static final String PARENT_ORG_ID = "ORG-L1";
    /** 信用购种子商品 P-CREDIT-1 的售卖方（与 Commerce/Credit 种子对齐）。 */
    public static final String CREDIT_SELLER_ORG_ID = "ORG-1";

    @Bean
    ProfitShareAccrualRepository profitShareAccrualRepository() {
        return new InMemoryProfitShareAccrualRepository();
    }

    @Bean
    ReferralBindingRepository referralBindingRepository() {
        return new InMemoryReferralBindingRepository();
    }

    @Bean
    SettlementBatchRepository settlementBatchRepository() {
        return new InMemorySettlementBatchRepository();
    }

    @Bean
    AccrueOnOrderCompleted accrueOnOrderCompleted(
            ProfitSharingRuleRepository rules,
            ReferralBindingRepository bindings,
            ProfitShareAccrualRepository accruals) {
        return new AccrueOnOrderCompleted(rules, bindings, accruals);
    }

    @Bean
    ReverseAccrualsOnRefund reverseAccrualsOnRefund(ProfitShareAccrualRepository accruals) {
        return new ReverseAccrualsOnRefund(accruals);
    }

    @Bean
    RunSettlementBatch runSettlementBatch(
            ProfitShareAccrualRepository accruals,
            SettlementBatchRepository batches,
            LedgerRepository ledger,
            AccountRepository accounts) {
        return new RunSettlementBatch(accruals, batches, ledger, accounts, CLEARING_ACCOUNT_ID);
    }

    /**
     * 正式本地种子：ORG-L2 分润规则（L2 10% / L1 5% / PLATFORM 余量）+ ORG-1（10% + PLATFORM 余量）+
     * 结算户 + 清算户。
     *
     * <p>也可使用 Credit 种子 {@code ACC-CLR}；本切片用独立 {@link #CLEARING_ACCOUNT_ID} 以免余额冲突。
     *
     * <p>ORG-1 结算户由 CommerceConfig {@code ACC-ORG1-SETTLE} 提供，此处不重复开户。
     */
    @Bean
    ApplicationRunner seedSettlement(
            ProfitSharingRuleRepository rules, AccountRepository accounts) {
        return args -> {
            Instant effectiveFrom = Instant.parse("2026-01-01T00:00:00Z");
            rules.save(
                    ProfitSharingRule.create(
                            "R-L2",
                            SELLER_ORG_ID,
                            List.of(
                                    ProfitSplit.of(SELLER_ORG_ID, 10),
                                    ProfitSplit.of(PARENT_ORG_ID, 5)),
                            effectiveFrom,
                            1));
            // 信用购 P-CREDIT-1 → ORG-1；与 ORG-L2 规则并存
            rules.save(
                    ProfitSharingRule.create(
                            "R-ORG-1",
                            CREDIT_SELLER_ORG_ID,
                            List.of(ProfitSplit.of(CREDIT_SELLER_ORG_ID, 10)),
                            effectiveFrom,
                            1));

            accounts.save(
                    Account.open(
                            CLEARING_ACCOUNT_ID,
                            AccountOwnerType.PLATFORM,
                            "SETTLEMENT-CLEARING",
                            AccountType.SETTLEMENT,
                            Currency.CNY,
                            1_000_000));
            for (String org :
                    List.of(SELLER_ORG_ID, PARENT_ORG_ID, ProfitSharingRule.PLATFORM_ORG_ID)) {
                accounts.save(
                        Account.open(
                                "ACC-STL-" + org,
                                AccountOwnerType.ORG,
                                org,
                                AccountType.SETTLEMENT,
                                Currency.CNY,
                                0));
            }
        };
    }
}
