package com.visa.backoffice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandeurDTO {

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;                     // (O)

    private String prenom;                  // (F)

    private String nomJeuneFille;           // (F)

    @NotNull(message = "La date de naissance est obligatoire")
    private LocalDate dateNaissance;        // (O)

    private String lieuNaissance;           // (F)

    @NotNull(message = "La situation familiale est obligatoire")
    private Long idSituationFamiliale;      // (O, S)

    @NotNull(message = "La nationalité est obligatoire")
    private Long idNationalite;             // (O, S)

    @NotBlank(message = "L'adresse Madagascar est obligatoire")
    private String adresseMadagascar;       // (O)

    @NotBlank(message = "Le téléphone est obligatoire")
    private String telephone;               // (O)

    private String email;                   // (F)
}
