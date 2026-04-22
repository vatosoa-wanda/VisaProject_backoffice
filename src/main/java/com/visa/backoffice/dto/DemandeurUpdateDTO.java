package com.visa.backoffice.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

/**
 * DTO pour la modification d'un demandeur.
 * ⚠️ Champs IMMUTABLES (affichage seul) : nom, dateNaissance, nationalité
 * ⚠️ Champs modifiables : prenom, nomJeuneFille, lieuNaissance, situationFamiliale,
 *                          adresseMadagascar, telephone, email
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeurUpdateDTO {

    // ── Champs IMMUTABLES (affichage seul, lecture) ─────────────────────
    private String nomImmutable;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateNaissanceImmutable;
    private String nationaliteImmutable;

    // ── Champs modifiables ───────────────────────────────────────────

    private String prenom;                    // (F)

    private String nomJeuneFille;             // (F)

    private String lieuNaissance;             // (F)

    @NotNull(message = "La situation familiale est obligatoire")
    private Long idSituationFamiliale;        // (O, S)

    @NotBlank(message = "L'adresse Madagascar est obligatoire")
    private String adresseMadagascar;         // (O)

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;                 // (O)

    @Email(message = "Format email invalide")
    private String email;                     // (F)
}
