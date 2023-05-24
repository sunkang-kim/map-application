package com.mapapplication.mapapplication.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@RequiredArgsConstructor
public class PlaceDto {
    private String placeId;
    private Double latitude;
    private Double longitude;
    private String name;
    private Double rating;
    private String phoneNumber;
    private String address;

    private Long parentId;

    // 생성자, getter, setter, 기타 메서드
}
