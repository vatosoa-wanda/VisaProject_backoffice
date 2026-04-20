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
public class PasseportDTO {

    @NotBlank(message = "Le numéro de passeport est obligatoire")
    private String numero;                  // (O, U)

    @NotNull(message = "La date de délivrance est obligatoire")
    private LocalDate dateDelivrance;       // (O)

    @NotNull(message = "La date d'expiration est obligatoire")
    @jakarta.validation.constraints.Future(message = "Le passeport doit être valide")
    private LocalDate dateExpiration;       // (O)
}
