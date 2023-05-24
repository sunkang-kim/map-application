package com.mapapplication.mapapplication.dto;

import com.mapapplication.mapapplication.entity.MemberEntity;
import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class MemberDto {

    private Long id;
    private String memberEmail;
    private String memberName;
    private String memberPassword;

    public static MemberDto toMemberDTO(MemberEntity memberEntity) {
        MemberDto memberDTO = new MemberDto();
        memberDTO.setId(memberEntity.getId());
        memberDTO.setMemberEmail(memberEntity.getMemberEmail());
        memberDTO.setMemberName(memberEntity.getMemberName());
        memberDTO.setMemberPassword(memberEntity.getMemberPassword());
        return memberDTO;
    }

}
