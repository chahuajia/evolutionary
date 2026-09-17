package com.evolutionary.operator.domain;

/** 运营域错误码（对齐 IDL OrgDomainErrorCode）。 */
public enum OperatorErrorCode {
    ORG_NOT_OWNER,
    ORG_NOT_DESCENDANT,
    FIELD_NOT_OVERRIDABLE,
    TEMPLATE_NOT_PUBLISHED,
    TEMPLATE_IMMUTABLE,
    TEMPLATE_NOT_DRAFT,
    OVERRIDE_INVALID,
    CAPABILITY_DENIED
}
