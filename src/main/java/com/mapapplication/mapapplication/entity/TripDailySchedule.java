package com.mapapplication.mapapplication.entity;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "tripDailySchedule")
@Getter @Setter
public class TripDailySchedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parentId")
    private TripSchedule parent;

    @Column(name = "title")
    private String title;

    @Column(name = "tripDate")
    private LocalDate date;

    public TripSchedule getParent() {
        return parent;
    }

    public void setParent(TripSchedule parent) {
        this.parent = parent;
    }

}

