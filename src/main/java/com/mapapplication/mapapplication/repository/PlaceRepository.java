package com.mapapplication.mapapplication.repository;

import com.mapapplication.mapapplication.entity.Place;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PlaceRepository extends JpaRepository<Place, Long> {
    List<Place> findByParentId(Long parentId);
    void deleteByParentId(Long parentId);
    void deleteByParentIdIn(List<Long> parentIds);
}
