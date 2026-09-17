package com.evolutionary.operator.application;

import com.evolutionary.operator.domain.AuditLog;
import java.util.List;

/** 审计日志仓储端口。 */
public interface AuditLogRepository {

    void append(AuditLog log);

    List<AuditLog> findByResourceId(String resourceId);
}
