package com.asociaciondomitila.projects.dto;

import com.asociaciondomitila.projects.entity.Staff;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StaffDto {

    private Long id;
    private String nick;
    private String name;
    private String surname1;
    private String surname2;
    private String phone;
    private String gender;
    private LocalDate bday;
    private String role;
    private String email;
    private String profileImg;
    private Boolean active;
    private Boolean emailVerified;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static StaffDto fromEntity(Staff staff) {
        return StaffDto.builder()
                .id(staff.getId())
                .nick(staff.getNick())
                .name(staff.getName())
                .surname1(staff.getSurname1())
                .surname2(staff.getSurname2())
                .phone(staff.getPhone())
                .gender(staff.getGender().getDisplayName())
                .bday(staff.getBday())
                .role(staff.getRole().getDisplayName())
                .email(staff.getEmail())
                .profileImg(staff.getProfileImg())
                .active(staff.getActive())
                .emailVerified(staff.getEmailVerified())
                .createdAt(staff.getCreatedAt())
                .updatedAt(staff.getUpdatedAt())
                .build();
    }
}