package com.mapapplication.mapapplication.service;

import com.mapapplication.mapapplication.entity.MapEntity;
import com.mapapplication.mapapplication.repository.MapRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MapService {

    private MapRepository mapRepository;

    public MapService(MapRepository mapRepository) {
        this.mapRepository = mapRepository;
    }

    public List<MapEntity> getAllLocations() {
        return mapRepository.findAll();
    }

}
