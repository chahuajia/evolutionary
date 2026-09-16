package com.evolutionary.swap.infrastructure;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 持久化模型 —— 只活在 infrastructure。领域 {@code Station} 不带任何 JPA 注解。
 */
@Entity
@Table(name = "stations")
public class StationJpaEntity {

    @Id
    private String id;

    @Column(nullable = false)
    private String name;

    /** JSON：[{"id":"B1","status":"AVAILABLE"}, ...] */
    @Column(nullable = false, length = 4000)
    private String batteriesJson;

    protected StationJpaEntity() {}

    public StationJpaEntity(String id, String name, String batteriesJson) {
        this.id = id;
        this.name = name;
        this.batteriesJson = batteriesJson;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getBatteriesJson() {
        return batteriesJson;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setBatteriesJson(String batteriesJson) {
        this.batteriesJson = batteriesJson;
    }
}
