package com.visa.backoffice.dto;

import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VisaTransformableDTO {

    @NotBlank(message = "La référence visa est obligatoire")
    private String referenceVisa;          // (O, U)

    @NotNull(message = "La date d'entrée est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate dateEntree;          // (O)

    @NotBlank(message = "Le lieu d'entrée est obligatoire")
    private String lieuEntree;             // (O)

    @NotNull(message = "La date d'expiration est obligatoire")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDate dateExpiration;      // (O)
}
