package com.mapapplication.mapapplication.repository;

import com.mapapplication.mapapplication.entity.TripDailySchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripDailyScheduleRepository extends JpaRepository<TripDailySchedule, Long> {
    List<TripDailySchedule> findByParentIdOrderByDateAsc(Long parentId);
    List<TripDailySchedule> findByParentId(Long parentId);
}

