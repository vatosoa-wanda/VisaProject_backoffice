package com.visa.backoffice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeCreateDTO {

    // ── Bloc 🟦 ÉTAT CIVIL (Dev1) ──────────────────────────────
    @Valid
    @NotNull(message = "Les informations du demandeur sont obligatoires")
    private DemandeurDTO demandeurDTO;

    // ── Bloc 🟩 PASSEPORT (Dev1) ───────────────────────────────
    @Valid
    @NotNull(message = "Les informations du passeport sont obligatoires")
    private PasseportDTO passeportDTO;

    // ── Bloc 🟨 VISA TRANSFORMABLE ──────────────────────────────
    @Valid
    @NotNull(message = "Les informations du visa transformable sont obligatoires")
    private VisaTransformableDTO visaDTO;

    // ── Bloc 🟥 DEMANDE ─────────────────────────────────────────
    @NotNull(message = "Le type de visa est obligatoire")
    private Long idTypeVisa;                    // (O, S)

    // dateDemande     → forcé à now() en service  (H)
    // idTypeDemande   → forcé à NOUVELLE en service (H)
    // idStatutDemande → forcé à CREE en service (H)

    // ── Bloc 🟪 PIÈCES ───────────────────────────────────────────
    @Builder.Default
    private List<Long> piecesFournies = new ArrayList<>();  // ids des pièces cochées

    // ── Contexte Sprint 2 : DUPLICATA / TRANSFERT ────────────────
    private Long idDemandeOrigine;              // Pour DUPLICATA/TRANSFERT avec antécédent
    private String typeDemande;                 // "NOUVELLE" | "DUPLICATA" | "TRANSFERT"
    private Boolean avecAntecedent;             // true = avec, false = sans

    // ── Nouveau passeport pour TRANSFERT ───────────────────────
    @Valid
    private PasseportDTO passeportNouveauDTO;   // Pour TRANSFERT
}
