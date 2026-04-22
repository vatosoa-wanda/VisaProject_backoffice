package com.visa.backoffice.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO pour la modification d'une demande existante.
 * ⚠️ Champs IMMUTABLES : dateDemande, idDemandeur, typeDemande
 * ⚠️ Champs modifiables : demandeur (partiellement), passeport, visa, typeVisa, pièces
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeUpdateDTO {

    // ── Bloc 🟦 ÉTAT CIVIL (partiellement modifiable) ──────────────────
    @Valid
    @NotNull(message = "Les informations du demandeur sont obligatoires")
    private DemandeurUpdateDTO demandeurDTO;

    // ── Bloc 🟩 PASSEPORT (modifiable) ──────────────────────────────
    @Valid
    @NotNull(message = "Les informations du passeport sont obligatoires")
    private PasseportDTO passeportDTO;

    // ── Bloc 🟨 VISA TRANSFORMABLE (modifiable) ──────────────────────
    @Valid
    @NotNull(message = "Les informations du visa transformable sont obligatoires")
    private VisaTransformableDTO visaDTO;

    // ── Bloc 🟥 DEMANDE (partiellement modifiable) ──────────────────
    @NotNull(message = "Le type de visa est obligatoire")
    private Long idTypeVisa;

    // ── Bloc 🟪 PIÈCES (modifiable) ──────────────────────────────────
    @Builder.Default
    private List<Long> piecesFournies = new ArrayList<>();
}
