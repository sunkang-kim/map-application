package com.mapapplication.mapapplication.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;

@Entity
@Getter @Setter
@Table(name = "location")
public class MapEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "lat")
    private Double lat;

    @Column(name = "lng")
    private Double lng;
}
