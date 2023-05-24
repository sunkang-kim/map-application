package com.mapapplication.mapapplication.repository;

import com.mapapplication.mapapplication.entity.MapEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MapRepository extends JpaRepository<MapEntity, Long> {
}
