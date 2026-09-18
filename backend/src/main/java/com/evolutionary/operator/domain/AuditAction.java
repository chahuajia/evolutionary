package com.evolutionary.operator.domain;

/** 审计动作（对齐 IDL AuditAction）。 */
public enum AuditAction {
    TEMPLATE_PUBLISH,
    TEMPLATE_DEPRECATE,
    OVERRIDE_ACTIVATE,
    OVERRIDE_REVOKE,
    /** 平台批准商家入驻（切片30b · P0 轨迹）。 */
    ONBOARDING_APPROVE,
    /** 信用逾期标记（切片32a）。 */
    CREDIT_MARK_OVERDUE,
    /** 信用政策降额应用（切片32a）。 */
    CREDIT_POLICY_DOWNGRADE
}
