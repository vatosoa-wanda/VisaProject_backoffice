package com.visa.backoffice.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentUploadDTO {

    @NotNull(message = "L'identifiant de la demande est obligatoire")
    private Long demandeId;

    @NotNull(message = "La pièce est obligatoire")
    private Long pieceId;

    private String nomPiece;
}
