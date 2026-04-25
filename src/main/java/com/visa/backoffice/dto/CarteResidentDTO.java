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
public class CarteResidentDTO {
    private Long id;
    private String numeroCarte;
    private LocalDate dateDebut;
    private LocalDate dateFin;
    private Long idPasseport;
    private Long idDemande;
}
