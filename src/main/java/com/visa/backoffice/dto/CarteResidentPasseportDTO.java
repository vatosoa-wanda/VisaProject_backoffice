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
public class CarteResidentPasseportDTO {
    private Long id;
    private Long idCarteResident;
    private Long idPasseport;
    private Long idDemande;
    private LocalDateTime dateAssociation;
}
