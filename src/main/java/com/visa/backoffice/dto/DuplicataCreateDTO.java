package com.visa.backoffice.dto;

import jakarta.validation.constraints.NotNull;
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
public class DuplicataCreateDTO {

    @NotNull(message = "L'identifiant de la demande d'origine est obligatoire")
    private Long idDemandeOrigine;  // REQUIRED - FK vers une demande NOUVELLE APPROUVEE

    // Pièces fournies
    @Builder.Default
    private List<Long> piecesFournies = new ArrayList<>();
}
