package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaDTO {
    private Long id;
    private String referenceVisa;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long idPasseport;
    private Long idDemande;
}
