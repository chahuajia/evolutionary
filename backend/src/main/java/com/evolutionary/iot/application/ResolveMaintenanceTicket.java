package com.evolutionary.iot.application;

import com.evolutionary.iot.domain.MaintenanceTicket;
import java.util.Objects;

/** 解决运维工单（对齐 MaintenanceTicket.resolve · 已 RESOLVED 幂等）。 */
public final class ResolveMaintenanceTicket {

    private final MaintenanceTicketRepository tickets;

    public ResolveMaintenanceTicket(MaintenanceTicketRepository tickets) {
        this.tickets = Objects.requireNonNull(tickets, "tickets");
    }

    public MaintenanceTicket execute(String ticketId) {
        Objects.requireNonNull(ticketId, "ticketId");
        MaintenanceTicket ticket =
                tickets
                        .findById(ticketId)
                        .orElseThrow(() -> new IllegalArgumentException("未知工单: " + ticketId));
        MaintenanceTicket resolved = ticket.resolve();
        tickets.save(resolved);
        return resolved;
    }
}
