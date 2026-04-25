package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeResumeeDTO {
    private Long id;
    private String demandeurNom;
    private String demandeurPrenom;
    private String numeroPasSeport;
    private String referenceVisa;
    private LocalDateTime dateApproval;
}
