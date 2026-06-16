package com.asociaciondomitila.projects.dto;

import com.asociaciondomitila.projects.entity.BeneficiaryUser;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryUserResponseDTO {
    private Long id;
    private String name;
    private String surname1;
    private String surname2;
    private String dni;
    private String address;
    private Integer postalCode;
    private Long phone;
    private String email;
    private Long projectId;
    private List<Long> projectIds;

    public static BeneficiaryUserResponseDTO fromEntity(BeneficiaryUser beneficiary, Long currentProjectId) {
        return new BeneficiaryUserResponseDTO(
                beneficiary.getId(),
                beneficiary.getName(),
                beneficiary.getSurname1(),
                beneficiary.getSurname2(),
                beneficiary.getDni(),
                beneficiary.getAddress(),
                beneficiary.getPostalCode(),
                beneficiary.getPhone(),
                beneficiary.getEmail(),
                currentProjectId,
                beneficiary.getProjects() == null
                        ? List.of()
                        : beneficiary.getProjects().stream()
                                .map(project -> project.getId())
                                .filter(id -> id != null)
                                .distinct()
                                .toList()
        );
    }
}
