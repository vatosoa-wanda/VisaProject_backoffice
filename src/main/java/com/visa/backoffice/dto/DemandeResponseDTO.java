package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeResponseDTO {

    private Long id;
    private LocalDateTime dateDemande;
    private String nomDemandeur;
    private String prenomDemandeur;
    private String numeroPasSeport;
    private String referenceVisa;
    private String typeVisa;               // libellé ex: "Travailleur"
    private String typeDemande;            // libellé → "NOUVELLE"
    private String statutDemande;          // libellé → "CREE"
    private List<DemandePieceDTO> pieces;
    private Boolean demandeurExistant;
}
