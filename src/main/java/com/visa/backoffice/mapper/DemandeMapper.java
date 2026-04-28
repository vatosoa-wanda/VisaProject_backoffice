package com.visa.backoffice.mapper;

import com.visa.backoffice.dto.DemandePieceDTO;
import com.visa.backoffice.dto.DemandeResumeeDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.dto.PieceDTO;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Piece;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;



@Component

public class DemandeMapper {



    /**

     * Convertit une Demande persistée en DTO de réponse complet.

     * @param demande   entité avec toutes ses relations chargées

     * @return          DemandeResponseDTO prêt pour la vue

     */

    public DemandeResponseDTO toResponseDTO(Demande demande) {

        DemandeResponseDTO dto = new DemandeResponseDTO();

        dto.setId(demande.getId());

        dto.setDateDemande(demande.getDateDemande());



        if (demande.getDemandeur() != null) {

            dto.setNomDemandeur(demande.getDemandeur().getNom());

            dto.setPrenomDemandeur(demande.getDemandeur().getPrenom());

        }



        if (demande.getVisaTransformable() != null && demande.getVisaTransformable().getPasseport() != null) {

            dto.setNumeroPasSeport(demande.getVisaTransformable().getPasseport().getNumero());

            dto.setReferenceVisa(demande.getVisaTransformable().getReferenceVisa());

        }



        if (demande.getTypeVisa() != null) {

            dto.setTypeVisa(demande.getTypeVisa().getLibelle());

        }



        if (demande.getTypeDemande() != null) {

            dto.setTypeDemande(demande.getTypeDemande().getLibelle());

        }



        if (demande.getStatutDemande() != null) {

            dto.setStatutDemande(demande.getStatutDemande().getLibelle());

        }



        // mapping des pièces

        if (demande.getDemandePieces() != null) {

            dto.setPieces(demande.getDemandePieces().stream()

                .map(dp -> {

                    DemandePieceDTO dpDto = new DemandePieceDTO();

                    if (dp.getPiece() != null) {

                        dpDto.setIdPiece(dp.getPiece().getId());

                        dpDto.setNomPiece(dp.getPiece().getNom());

                        dpDto.setObligatoire(dp.getPiece().getObligatoire());

                    }

                    dpDto.setFourni(dp.getFourni());

                    return dpDto;

                })

                .collect(Collectors.toList()));

        }



        return dto;

    }



    /**

     * Convertit une Demande en DemandeResumeeDTO pour la recherche.

     * @param demande   entité Demande

     * @return          DemandeResumeeDTO avec informations résumées

     */

    public DemandeResumeeDTO toResumeeDTO(Demande demande) {

        DemandeResumeeDTO dto = new DemandeResumeeDTO();

        if (demande == null) {
            return dto;
        }

        dto.setId(demande.getId());

        // Gestion sécurisée du demandeur
        if (demande.getDemandeur() != null) {
            dto.setDemandeurNom(demande.getDemandeur().getNom() != null ? demande.getDemandeur().getNom() : "");
            dto.setDemandeurPrenom(demande.getDemandeur().getPrenom() != null ? demande.getDemandeur().getPrenom() : "");
        } else {
            dto.setDemandeurNom("");
            dto.setDemandeurPrenom("");
        }

        // Gestion sécurisée du passeport
        if (demande.getVisaTransformable() != null && demande.getVisaTransformable().getPasseport() != null) {
            dto.setNumeroPasSeport(demande.getVisaTransformable().getPasseport().getNumero() != null ? 
                demande.getVisaTransformable().getPasseport().getNumero() : "");
        } else {
            dto.setNumeroPasSeport("");
        }

        // Gestion sécurisée de la référence visa
        if (demande.getVisaTransformable() != null) {
            dto.setReferenceVisa(demande.getVisaTransformable().getReferenceVisa() != null ? 
                demande.getVisaTransformable().getReferenceVisa() : "");
        } else {
            dto.setReferenceVisa("");
        }

        // Gestion sécurisée de la date
        dto.setDateApproval(demande.getDateDemande());

        return dto;

    }



    /**

     * Convertit une Piece en PieceDTO.

     * @param piece     entité Piece

     * @return          PieceDTO

     */

    public PieceDTO toPieceDTO(Piece piece) {

        PieceDTO dto = new PieceDTO();

        dto.setId(piece.getId());

        dto.setNom(piece.getNom());

        dto.setObligatoire(piece.getObligatoire());

        if (piece.getTypePiece() != null) {

            dto.setTypePiece(piece.getTypePiece().getCode());

        }

        return dto;

    }

}
