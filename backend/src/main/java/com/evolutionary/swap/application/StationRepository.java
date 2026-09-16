package com.evolutionary.swap.application;

import com.evolutionary.station.domain.Station;

/**
 * 站点读写端口 —— 应用层依赖，领域层不看见。
 *
 * <p>本轮不接 JPA/Spring：测试用内存实现。位置判据见
 * {@code patterns/dependency-decision} 与 {@code patterns/layer-vs-context}。
 */
public interface StationRepository {

    Station get(String stationId);

    void save(Station station);

    /** 全量读（本轮多站概览；调用方负责映射为视图 DTO）。 */
    java.util.List<Station> findAll();
}
