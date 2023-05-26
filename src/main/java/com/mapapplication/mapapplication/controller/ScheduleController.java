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
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.view.RedirectView;

import javax.servlet.http.HttpSession;
import java.time.LocalDate;
import java.util.List;


@RestController
@RequestMapping("/schedules")
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

    @PostMapping("")
    public RedirectView createTripSchedule(@RequestParam("title") String title,
                                           @RequestParam("startDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                           @RequestParam("endDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                           RedirectAttributes redirectAttributes, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        // 요청 받은 데이터를 이용하여 TripSchedule 생성 로직 수행
        TripSchedule createdSchedule = scheduleService.createTripSchedule(title, startDate, endDate, userId);

        // 생성된 TripSchedule 정보를 리다이렉트할 URL의 경로 변수로 설정
        Long parentId = createdSchedule.getId();
        redirectAttributes.addAttribute("parentId", parentId);

        // 리다이렉트할 URL을 RedirectView로 생성하여 반환
        RedirectView redirectView = new RedirectView();
        redirectView.setUrl("http://localhost:8080/schedules/");
        return redirectView;
    }


    @GetMapping("")
    public ModelAndView getCalendarPage(HttpSession session) {
        ModelAndView modelAndView;
        // loginEmail이 null이 아닐 때에만 특정 동작 수행
        if (session.getAttribute("loginEmail") != null) {
            Long userId = (Long) session.getAttribute("userId");

            List<TripSchedule> tripSchedules = scheduleRepository.findByUserId(userId);

            modelAndView = new ModelAndView("calendar");
            modelAndView.addObject("tripSchedules", tripSchedules);

            ObjectMapper objectMapper = new ObjectMapper();

            // LocalDate Type 처리를 위한 JavaTimeModule 등록
            objectMapper.registerModule(new JavaTimeModule());

            try {
                String json = objectMapper.writeValueAsString(tripSchedules);
                modelAndView.addObject("json", json);
                System.out.println("j" + json);
            } catch (JsonProcessingException e) {
                // JSON 변환 중 오류 발생 시 예외 처리
                e.printStackTrace();
                return new ModelAndView("error"); // 오류 페이지로 이동하거나 다른 처리를 수행할 수 있습니다.
            }
        } else {
            modelAndView = new ModelAndView("error");
        }
        return modelAndView;
    }


/*
    @GetMapping("/")
    public ResponseEntity<List<TripSchedule>> getAllTripSchedules() {
        List<TripSchedule> tripSchedules = scheduleRepository.findAll();
        return ResponseEntity.ok(tripSchedules);
    }
*/

    @PutMapping("/{tripId}")
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


    @GetMapping("/{parentId}/daily")
    public ModelAndView getDailySchedulesByParentId(@PathVariable("parentId") Long parentId
            , HttpSession session) {
        ModelAndView modelAndView;
        Long userId = (Long) session.getAttribute("userId");
        boolean isMatching = isUserIdMatching(userId, parentId);

        if (isMatching){
            List<TripDailySchedule> dailySchedules = scheduleService.sortedDailyLists(parentId);

            String title = scheduleRepository.findById(parentId).get().getTitle();

            modelAndView = new ModelAndView("mapjab");
            modelAndView.addObject("dailySchedules", dailySchedules);
            modelAndView.addObject("title", title);
        }else {
            modelAndView = new ModelAndView("error");
        }
        return modelAndView;
    }


    @DeleteMapping("/{tripScheduleId}")
    public RedirectView deleteTripSchedule(@PathVariable Long tripScheduleId, RedirectAttributes redirectAttributes
            , HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean isMatching = isUserIdMatching(userId, tripScheduleId);

        if (isMatching) {
            scheduleService.deleteTripSchedule(tripScheduleId);
            redirectAttributes.addFlashAttribute("message", "삭제 성공");
        } else {
            redirectAttributes.addFlashAttribute("error", "삭제 권한이 없습니다.");
        }

        return new RedirectView("/schedules");
    }


    @PutMapping("/update/{scheduleId}")
    public ResponseEntity<String> updateTripDailySchedule(
            @PathVariable("scheduleId") Long scheduleId,
            @RequestParam("title") String title,
            @RequestParam("newTripDate") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate newTripDate,
            HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");
        boolean isMatching = isUserIdMatching(userId, scheduleId);

        if (isMatching) {
            scheduleService.modifyTripDailySchedule(scheduleId, title, newTripDate);
        } else {
            // 수정 권한이 없을 때의 처리
            RedirectView redirectView = new RedirectView("/schedules");
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(redirectView.getUrl());
        }

        return ResponseEntity.ok("변경 사항 적용");
    }



    // 세션의 id와 테이블의 userId비교 하는 메소드
    public boolean isUserIdMatching(Long userId, Long tripScheduleId) {
        TripSchedule tripSchedule = scheduleRepository.findById(tripScheduleId).orElse(null);
        return tripSchedule != null && tripSchedule.getUserId().equals(userId);
    }


}