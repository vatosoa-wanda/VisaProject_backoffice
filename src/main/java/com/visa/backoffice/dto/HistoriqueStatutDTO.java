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
public class HistoriqueStatutDTO {

    private Long id;
    private Long demandeId;
    private String statutDemande;
    private LocalDateTime dateChangement;
    private String commentaire;
}
