package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.AuditLogRepository;
import com.evolutionary.operator.domain.AuditLog;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** 进程内审计日志仓储（单测用；生产由 {@link JpaAuditLogRepository} 接管）。 */
public final class InMemoryAuditLogRepository implements AuditLogRepository {

    private final List<AuditLog> logs = new CopyOnWriteArrayList<>();

    @Override
    public void append(AuditLog log) {
        logs.add(log);
    }

    @Override
    public List<AuditLog> findByResourceId(String resourceId) {
        List<AuditLog> matched = new ArrayList<>();
        for (AuditLog log : logs) {
            if (log.resourceId().equals(resourceId)) {
                matched.add(log);
            }
        }
        return List.copyOf(matched);
    }
}
