package com.visa.backoffice.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
}
