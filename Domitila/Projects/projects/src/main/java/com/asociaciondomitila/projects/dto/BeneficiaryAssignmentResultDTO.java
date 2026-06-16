package com.asociaciondomitila.projects.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BeneficiaryAssignmentResultDTO {
    private String action;
    private String message;
    private boolean existsInDatabase;
    private boolean alreadyInProject;
    private boolean requiresConfirmation;
    private BeneficiaryUserResponseDTO beneficiary;
}
