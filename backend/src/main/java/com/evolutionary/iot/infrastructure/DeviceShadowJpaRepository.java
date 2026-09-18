package com.evolutionary.iot.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;

public interface DeviceShadowJpaRepository extends JpaRepository<DeviceShadowJpaEntity, String> {}
