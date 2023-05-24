package com.mapapplication.mapapplication.controller;

import com.mapapplication.mapapplication.entity.MapEntity;
import com.mapapplication.mapapplication.service.MapService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api")
public class MapController {

    @Autowired
    private MapService mapService;

    @GetMapping("/locations")
    public List<MapEntity> getAllLocations() {
        return mapService.getAllLocations();
    }
}
