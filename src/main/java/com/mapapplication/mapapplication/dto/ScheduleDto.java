package com.mapapplication.mapapplication.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter @Setter
@RequiredArgsConstructor
public class ScheduleDto {
    private Long id;
    private String title;
    private LocalDate startDate;
    private LocalDate endDate;

    // Ddte대신에 LocalDate를 사용하면 불필요한 시간 및 타임존 정보를 제외하고 간결하게 날짜를 다룰 수 있습니다.

}

