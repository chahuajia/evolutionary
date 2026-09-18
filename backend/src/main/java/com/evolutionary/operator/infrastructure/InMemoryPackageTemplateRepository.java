package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.PackageTemplateRepository;
import com.evolutionary.operator.domain.PackageTemplate;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/** 进程内套餐模板仓储。 */
public final class InMemoryPackageTemplateRepository implements PackageTemplateRepository {

    private final Map<String, PackageTemplate> byId = new ConcurrentHashMap<>();

    @Override
    public void save(PackageTemplate template) {
        byId.put(template.id(), template);
    }

    @Override
    public Optional<PackageTemplate> findById(String id) {
        return Optional.ofNullable(byId.get(id));
    }
}
