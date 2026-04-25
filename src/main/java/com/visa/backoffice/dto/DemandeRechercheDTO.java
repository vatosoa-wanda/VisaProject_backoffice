package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeRechercheDTO {
    private String nom;              // Optionnel - search by demandeur.nom
    private String numeroPasSeport;  // Optionnel - search by passeport.numero
    private String referenceVisa;    // Optionnel - search by visa_transformable.reference_visa
}
