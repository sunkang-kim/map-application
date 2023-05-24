package com.mapapplication.mapapplication.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Table(name = "places")
@Getter @Setter
public class Place {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "place_id")
    private String placeId;

    @Column(name = "lat")
    private Double latitude;

    @Column(name = "lng")
    private Double longitude;

    private String name;

    private Double rating;

    @Column(name = "formatted_phone_number")
    private String phoneNumber;

    @Column(name = "address")
    private String address;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    private TripDailySchedule parent;

    public void setParent(TripDailySchedule parent) {
        this.parent = parent;
    }
}


