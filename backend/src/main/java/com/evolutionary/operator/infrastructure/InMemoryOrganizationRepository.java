package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.domain.Organization;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内组织仓储。 */
public final class InMemoryOrganizationRepository implements OrganizationRepository {

    private final Map<String, Organization> byId = new ConcurrentHashMap<>();

    @Override
    public void save(Organization org) {
        byId.put(org.id(), org);
    }

    @Override
    public Optional<Organization> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }

    @Override
    public List<Organization> findAll() {
        return List.copyOf(new ArrayList<>(byId.values()));
    }
}
