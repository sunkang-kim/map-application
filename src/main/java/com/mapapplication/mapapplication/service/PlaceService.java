package com.mapapplication.mapapplication.service;

import com.mapapplication.mapapplication.dto.PlaceDto;
import com.mapapplication.mapapplication.entity.Place;
import com.mapapplication.mapapplication.entity.TripDailySchedule;
import com.mapapplication.mapapplication.repository.PlaceRepository;
import com.mapapplication.mapapplication.repository.TripDailyScheduleRepository;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.List;

@Service
public class PlaceService {
    private final PlaceRepository placeRepository;
    private final TripDailyScheduleRepository tripDailyScheduleRepository;

    public PlaceService(PlaceRepository placeRepository, TripDailyScheduleRepository tripDailyScheduleRepository) {
        this.placeRepository = placeRepository;
        this.tripDailyScheduleRepository = tripDailyScheduleRepository;
    }

    public void savePlaces(Long parentId, List<PlaceDto> placeDTOs) {
        List<Place> existingPlaces = placeRepository.findByParentId(parentId);
        TripDailySchedule parent = tripDailyScheduleRepository.findById(parentId)
                .orElseThrow(() -> new EntityNotFoundException("Parent not found"));

        // 입력받은 places를 처리
        for (PlaceDto placeDTO : placeDTOs) {
            String placeId = placeDTO.getPlaceId();
            boolean isExistingPlace = false;

            // DB의 existingPlaces와 비교하여 처리
            for (Place existingPlace : existingPlaces) {
                if (existingPlace.getPlaceId().equals(placeId)) {
                    isExistingPlace = true;
                    // placeId가 일치하는 경우, 변경 없이 유지
                    break;
                }
            }

            if (!isExistingPlace) {
                // 입력받은 값에는 있는데 DB에는 없는 경우, 추가
                Place newPlace = new Place();
                newPlace.setParent(parent);
                newPlace.setName(placeDTO.getName());
                newPlace.setPhoneNumber(placeDTO.getPhoneNumber());
                newPlace.setRating(placeDTO.getRating());
                newPlace.setPlaceId(placeDTO.getPlaceId());
                newPlace.setAddress(placeDTO.getAddress());
                newPlace.setLatitude(placeDTO.getLatitude());
                newPlace.setLongitude(placeDTO.getLongitude());
                placeRepository.save(newPlace);
            }
        }

        // 입력받은 값에는 없는데 DB에는 있는 경우, 삭제
        for (Place existingPlace : existingPlaces) {
            boolean isExistingInInput = false;

            for (PlaceDto placeDTO : placeDTOs) {
                if (existingPlace.getPlaceId().equals(placeDTO.getPlaceId())) {
                    isExistingInInput = true;
                    break;
                }
            }

            if (!isExistingInInput) {
                placeRepository.delete(existingPlace);
            }
        }
    }
}
