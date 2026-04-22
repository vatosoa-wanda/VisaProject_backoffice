package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DemandePieceDTO {

    private Long idPiece;
    private String nomPiece;
    private Boolean obligatoire;
    private Boolean fourni;
}
