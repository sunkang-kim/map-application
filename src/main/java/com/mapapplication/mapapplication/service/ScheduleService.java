package com.mapapplication.mapapplication.service;

import com.mapapplication.mapapplication.entity.TripDailySchedule;
import com.mapapplication.mapapplication.entity.TripSchedule;
import com.mapapplication.mapapplication.repository.PlaceRepository;
import com.mapapplication.mapapplication.repository.ScheduleRepository;
import com.mapapplication.mapapplication.repository.TripDailyScheduleRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final TripDailyScheduleRepository tripDailyScheduleRepository;
    private final PlaceRepository placeRepository;

    public ScheduleService(ScheduleRepository scheduleRepository, TripDailyScheduleRepository tripDailyScheduleRepository, PlaceRepository placeRepository) {
        this.scheduleRepository = scheduleRepository;
        this.tripDailyScheduleRepository = tripDailyScheduleRepository;
        this.placeRepository = placeRepository;
    }

    public List<TripDailySchedule> sortedDailyLists(Long parentId) {
        List<TripDailySchedule> dailySchedules = tripDailyScheduleRepository.findByParentIdOrderByDateAsc(parentId);
        return dailySchedules;
    }

    // TripSchedule 생성
    public TripSchedule createTripSchedule(String title, LocalDate startDate, LocalDate endDate, Long userId) {
        TripSchedule tripSchedule = new TripSchedule();
        tripSchedule.setTitle(title);
        tripSchedule.setStartDate(startDate);
        tripSchedule.setEndDate(endDate);
        tripSchedule.setUserId(userId);

        //하위 테이블인 TripDailySchedule은 트리거에서 자동생성

        // TripSchedule 저장
        return scheduleRepository.save(tripSchedule);
    }

    // TripSchedule 삭제
    public void deleteTripSchedule(Long tripScheduleId) {

        // 연관된 TripDailySchedule 삭제
        deleteTripDailySchedule(tripScheduleId);

        // TripSchedule 삭제
        TripSchedule tripSchedule = scheduleRepository.findById(tripScheduleId)
                .orElseThrow(() -> new EntityNotFoundException("아이디를 찾을 수 없음: " + tripScheduleId));
        scheduleRepository.delete(tripSchedule);

    }

//
//    public void updateTripSchedule(Long tripScheduleId) {
//
//        // 연관된 TripDailySchedule 삭제
//        deleteTripDailySchedule(tripScheduleId);
//
//        // TripSchedule 삭제
//        TripSchedule tripSchedule = scheduleRepository.findById(tripScheduleId)
//                .orElseThrow(() -> new EntityNotFoundException("아이디를 찾을 수 없음: " + tripScheduleId));
//        scheduleRepository.delete(tripSchedule);
//
//    }

    // TripSchedule 수정
    public ResponseEntity<String> updateTripSchedule(Long scheduleId, String title, LocalDate startDate, LocalDate endDate) {

        // id를 이용해 수정할 객체 가져오기
        TripSchedule tripSchedule = scheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("TripSchedule을 찾을 수 없음: " + scheduleId));

        // title이 변경되었을때 수행
        if (!tripSchedule.getTitle().equals(title)) {
            tripSchedule.setTitle(title);
            scheduleRepository.save(tripSchedule);
        }

        // 일정이 변경되었을때 수행
        if (!tripSchedule.getStartDate().equals(startDate) || !tripSchedule.getEndDate().equals(endDate)) {
            tripSchedule.setStartDate(startDate);
            tripSchedule.setEndDate(endDate);
            deleteTripDailySchedule(tripSchedule.getId());

            LocalDate currentDate = tripSchedule.getStartDate();

            int nDays = 1;
            while (currentDate.isBefore(tripSchedule.getEndDate()) || currentDate.isEqual(tripSchedule.getEndDate())) {
                TripDailySchedule tripDailySchedule = new TripDailySchedule();
                tripDailySchedule.setParent(tripSchedule);
                tripDailySchedule.setTitle(nDays+"일 일정");
                tripDailySchedule.setDate(currentDate);
                nDays++;
                tripDailyScheduleRepository.save(tripDailySchedule);

                currentDate = currentDate.plusDays(1);
            }
            nDays = 1;

            tripSchedule = scheduleRepository.save(tripSchedule);
        }

        return ResponseEntity.ok("TripSchedule 업데이트 완료");

    }

    // TripDailySchedule 삭제
    public void deleteTripDailySchedule(Long tripScheduleId) {

        // 연관된 TripDailySchedule 삭제
        List<TripDailySchedule> tripDailySchedules = tripDailyScheduleRepository.findByParentId(tripScheduleId);

        tripDailyScheduleRepository.deleteAll(tripDailySchedules);

        // 연관된 Place 삭제
        List<Long> tripDailyScheduleIds = tripDailySchedules.stream()
                .map(TripDailySchedule::getId)
                .collect(Collectors.toList());

        placeRepository.deleteByParentIdIn(tripDailyScheduleIds);

    }

    // TripDailySchedule 수정
    public void modifyTripDailySchedule(Long scheduleId, String title, LocalDate newTripDate){
        // 업데이트 대상인 TripDailySchedule 가져오기
        TripDailySchedule schedule = tripDailyScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new EntityNotFoundException("TripDailySchedule을 찾을 수 없음: " + scheduleId));

        // title이 변경되었거나 date가 변경되었을때 수행
        if (!schedule.getTitle().equals(title) || !schedule.getDate().equals(newTripDate)) {
            // 같은 parentId를 가진 다른 TripDailySchedule들 가져오기
            List<TripDailySchedule> otherSchedules = tripDailyScheduleRepository.findByParentId(schedule.getParent().getId());

            // title 업데이트
            schedule.setTitle(title);

            // tripDate 맞바꾸기
            for (TripDailySchedule otherSchedule : otherSchedules) {
                if (otherSchedule.getId().equals(scheduleId)) {
                    otherSchedule.setDate(newTripDate);
                } else if (otherSchedule.getDate().equals(newTripDate)) {
                    otherSchedule.setDate(schedule.getDate());
                }
            }

            // 변경된 TripDailySchedule들 저장
            tripDailyScheduleRepository.saveAll(otherSchedules);
            tripDailyScheduleRepository.save(schedule); // 변경된 schedule 엔티티 저장
            System.out.println("수정 완료");
        }
    }

}

