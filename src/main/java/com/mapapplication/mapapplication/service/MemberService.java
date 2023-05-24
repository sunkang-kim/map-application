package com.mapapplication.mapapplication.service;


import com.mapapplication.mapapplication.dto.MemberDto;
import com.mapapplication.mapapplication.entity.MemberEntity;
import com.mapapplication.mapapplication.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberService {

    private final MemberRepository memberRepository;

    // 회원가입
    public void join(MemberDto memberDTO) {
        MemberEntity memberEntity = MemberEntity.toMemberEntity(memberDTO);
        memberRepository.save(memberEntity);
        System.out.println("join 진입");
    }


    // 회원정보 수정
    public MemberDto updateForm(String myEmail) {
        Optional<MemberEntity> optionalMemberEntity = memberRepository.findByMemberEmail(myEmail);

        if(optionalMemberEntity.isPresent()) {
            return MemberDto.toMemberDTO(optionalMemberEntity.get());
        } else {
            return null;
        }
    }


    // 회원정보 삭제
    public void deleteById(Long id) {
        memberRepository.deleteById(id);
    }

    // 로그인
    public MemberDto login(MemberDto memberDTO) {
        // 이메일로 회원정보 조회
        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberDTO.getMemberEmail());

        if(byMemberEmail.isPresent()) {
            // 이메일 일치
            MemberEntity memberEntity = byMemberEmail.get();

            if(memberEntity.getMemberPassword().equals(memberDTO.getMemberPassword())) {
                // 비밀번호 일치
                MemberDto dto = MemberDto.toMemberDTO(memberEntity);
                return dto;
            } else {
                // 비밀번호 불일치
                return null;
            }
        } else {
            // 이메일 불일치
            return null;
        }
    }

    // 이메일 중복체크
    public String emailCheck(String memberEmail) {
        Optional<MemberEntity> byMemberEmail = memberRepository.findByMemberEmail(memberEmail);

        if(byMemberEmail.isPresent()) {
            // 조회결과가 있으면 사용불가능
            return null;
        } else {
            // 조회결과가 없으면 사용가능
            return "ok";
        }
    }

}
