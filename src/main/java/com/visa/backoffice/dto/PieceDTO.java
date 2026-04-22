package com.visa.backoffice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PieceDTO {

    private Long id;
    private String nom;
    private Boolean obligatoire;
    private String typePiece;    // "COMMUN" / "TRAVAILLEUR" / "INVESTISSEUR"
}
