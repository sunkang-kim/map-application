package com.mapapplication.mapapplication.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.mapapplication.mapapplication.entity.TripDailySchedule;
import com.mapapplication.mapapplication.entity.TripSchedule;
import com.mapapplication.mapapplication.repository.ScheduleRepository;
import com.mapapplication.mapapplication.repository.TripDailyScheduleRepository;
import com.mapapplication.mapapplication.service.ScheduleService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import java.time.LocalDate;
import java.util.List;

@RestController
public class ScheduleController {

    private final ScheduleService scheduleService;
    private final ScheduleRepository scheduleRepository;
    private final TripDailyScheduleRepository tripDailyScheduleRepository;

    public ScheduleController(ScheduleService scheduleService, ScheduleRepository scheduleRepository,
                              TripDailyScheduleRepository tripDailyScheduleRepository) {
        this.scheduleService = scheduleService;
        this.scheduleRepository = scheduleRepository;
        this.tripDailyScheduleRepository = tripDailyScheduleRepository;
    }

    @PostMapping("/schedules")
    public RedirectView createTripSchedule(@RequestParam("title") String title,
                                           @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                           @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                           RedirectAttributes redirectAttributes) {
        // 요청 받은 데이터를 이용하여 TripSchedule 생성 로직 수행
        TripSchedule createdSchedule = scheduleService.createTripSchedule(title, startDate, endDate);

        // 생성된 TripSchedule 정보를 리다이렉트할 URL의 경로 변수로 설정
        Long parentId = createdSchedule.getId();
        redirectAttributes.addAttribute("parentId", parentId);

        // 리다이렉트할 URL을 RedirectView로 생성하여 반환
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:8080/schedules/");
        return redirectView;
    }



    @GetMapping("/schedules")
    public ModelAndView getCalendarPage() {
        List<TripSchedule> tripSchedules = scheduleRepository.findAll();
        List<TripDailySchedule> tripDailySchedules = tripDailyScheduleRepository.findAll();

        ModelAndView modelAndView = new ModelAndView("calendar");
        modelAndView.addObject("tripSchedules", tripSchedules);
        modelAndView.addObject("tripDailySchedules", tripDailySchedules);

        ObjectMapper objectMapper = new ObjectMapper();

        // LocalDate Type 처리를 위한 JavaTimeModule 등록
        objectMapper.registerModule(new JavaTimeModule());

        try {
            String json = objectMapper.writeValueAsString(tripSchedules);
            modelAndView.addObject("json", json);
            System.out.println("j"+json);
        } catch (JsonProcessingException e) {
            // JSON 변환 중 오류 발생 시 예외 처리
            e.printStackTrace();
            return new ModelAndView("error"); // 오류 페이지로 이동하거나 다른 처리를 수행할 수 있습니다.
        }
        System.out.println("t"+tripSchedules);
        System.out.println("t"+tripDailySchedules);
        return modelAndView;
    }


/*
    @GetMapping("/")
    public ResponseEntity<List<TripSchedule>> getAllTripSchedules() {
        List<TripSchedule> tripSchedules = scheduleRepository.findAll();
        return ResponseEntity.ok(tripSchedules);
    }
*/

    @PutMapping("/schedules/{tripId}")
    public String updateTrip(
            @PathVariable("tripId") Long tripId,
            @RequestParam("title") String title,
            @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
            RedirectAttributes redirectAttributes) {

        scheduleService.updateTripSchedule(tripId, title, startDate, endDate);
        redirectAttributes.addFlashAttribute("message", "Trip updated successfully");

        return "redirect:/";
    }


    @GetMapping("/schedules/{parentId}/daily")
    public ModelAndView getDailySchedulesByParentId(@PathVariable("parentId") Long parentId) {
        List<TripDailySchedule> dailySchedules = scheduleService.sortedDailyLists(parentId);

        String title = scheduleRepository.findById(parentId).get().getTitle();

        ModelAndView modelAndView = new ModelAndView("mapjab");
        modelAndView.addObject("dailySchedules", dailySchedules);
        modelAndView.addObject("title", title);

        return modelAndView;
    }


    @DeleteMapping("/schedules/{tripScheduleId}")
    public RedirectView deleteTripSchedule(@PathVariable Long tripScheduleId, RedirectAttributes redirectAttributes) {
        scheduleService.deleteTripSchedule(tripScheduleId);
        redirectAttributes.addFlashAttribute("message", "삭제 성공");
        return new RedirectView("/schedules");
    }



    @PutMapping("/schedules/update/{scheduleId}")
    public ResponseEntity<String> updateTripDailySchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("title") String title,
            @RequestParam("newTripDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newTripDate) {

        scheduleService.modifyTripDailySchedule(scheduleId, title, newTripDate);

        return ResponseEntity.ok("변경 사항 적용");
    }


}
