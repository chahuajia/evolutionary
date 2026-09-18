package com.evolutionary.operator.infrastructure;

import com.evolutionary.operator.application.OrganizationRepository;
import com.evolutionary.operator.domain.Organization;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 组织 JPA 适配（表 organizations）。
 *
 * <p><b>刻意不加 {@code @Component}。</b>本仓其他 JPA 适配都是 {@code @Component}，
 * 这个是例外：它的**种子必须在 bean 构造时写入**（见 {@code OperatorConfig} 的注释），
 * 所以由 {@code OperatorConfig} 的 {@code @Bean} 方法构造并播种。
 * 若这里再加 {@code @Component}，容器里会有两个 {@code OrganizationRepository}
 * —— {@code AdminConfig} 注入时直接 `Parameter required a single bean, but 2 were found`。
 *
 * <p>{@link #save} 走 save-and-flush：{@link #findAll} 的结果会立刻喂给
 * {@link com.evolutionary.operator.domain.OrgAuthorization} 的构造时索引，
 * 不能等事务提交才可见。
 */
public final class JpaOrganizationRepository implements OrganizationRepository {

    private final OrganizationJpaRepository jpa;

    public JpaOrganizationRepository(OrganizationJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public void save(Organization org) {
        jpa.saveAndFlush(
                new OrganizationJpaEntity(
                        org.id(),
                        org.name(),
                        org.parentId(),
                        org.capabilities(),
                        org.regionScope(),
                        org.status()));
    }

    @Override
    public Optional<Organization> findById(String id) {
        return jpa.findById(id).map(JpaOrganizationRepository::toDomain);
    }

    @Override
    public List<Organization> findAll() {
        List<Organization> result = new ArrayList<>();
        for (OrganizationJpaEntity row : jpa.findAll()) {
            result.add(toDomain(row));
        }
        return result;
    }

    private static Organization toDomain(OrganizationJpaEntity row) {
        return Organization.rehydrate(
                row.getId(),
                row.getName(),
                row.getParentId(),
                row.getCapabilities(),
                row.getRegionScope(),
                row.getStatus());
    }
}
