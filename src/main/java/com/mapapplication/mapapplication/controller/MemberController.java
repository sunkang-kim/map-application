package com.mapapplication.mapapplication.controller;


import com.mapapplication.mapapplication.dto.MemberDto;
import com.mapapplication.mapapplication.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;

@Controller
@RequiredArgsConstructor
@RequestMapping("/member")
public class MemberController {

    private final MemberService memberService;

    // 회원가입
    @GetMapping("/join")
    public String joinForm() {
        return "join";
    }

    @PostMapping("/join")
    public String join(@ModelAttribute MemberDto memberDTO) {
        memberService.join(memberDTO);
        return "login";
    }

    // 회원정보 삭제
    @GetMapping("/delete/{id}")
    public String deleteById(@PathVariable Long id) {
        memberService.deleteById(id);
        return "redirect:/member/";
    }

    // 로그인
    @GetMapping("/login")
    public String loginForm() {
        return "calendar";
    }

    @PostMapping("/login")
    public String login(@ModelAttribute MemberDto memberDTO, HttpSession session) {
        MemberDto loginResult = memberService.login(memberDTO);

        if(loginResult != null) {
            // 로그인 성공
            session.setAttribute("loginEmail", loginResult.getMemberEmail());
            session.setAttribute("userId", loginResult.getId());
            return "redirect:/schedules";
        } else {
            // 로그인 실패
            return "/member/login";
        }
    }

    // 로그아웃
    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "map";
    }

    // 이메일 중복체크
    @PostMapping("/email-check")
    public @ResponseBody String emailCheck(@RequestParam("memberEmail") String memberEmail) {
        System.out.println("memberEmail = " + memberEmail);
        String checkResult = memberService.emailCheck(memberEmail);
        return checkResult;
    }

}
