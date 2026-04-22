package com.visa.backoffice.mapper;

import com.visa.backoffice.dto.*;
import com.visa.backoffice.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
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

    /**
     * Convertit une Demande persistée en DTO d'édition.
     * Inclut les données actuelles pour affichage et modification.
     *
     * @param demande   entité persistée avec toutes les relations
     * @return          DemandeUpdateDTO avec données actuelles
     */
    public DemandeUpdateDTO toUpdateDTO(Demande demande) {
        DemandeUpdateDTO dto = new DemandeUpdateDTO();

        // Demandeur (partiellement)
        DemandeurUpdateDTO demandeurUpdateDTO = new DemandeurUpdateDTO();
        if (demande.getDemandeur() != null) {
            demandeurUpdateDTO.setNomImmutable(demande.getDemandeur().getNom());
            demandeurUpdateDTO.setDateNaissanceImmutable(demande.getDemandeur().getDateNaissance());
            if (demande.getDemandeur().getNationalite() != null) {
                demandeurUpdateDTO.setNationaliteImmutable(demande.getDemandeur().getNationalite().getLibelle());
            }
            demandeurUpdateDTO.setPrenom(demande.getDemandeur().getPrenom());
            demandeurUpdateDTO.setNomJeuneFille(demande.getDemandeur().getNomJeuneFille());
            demandeurUpdateDTO.setLieuNaissance(demande.getDemandeur().getLieuNaissance());
            demandeurUpdateDTO.setAdresseMadagascar(demande.getDemandeur().getAdresseMadagascar());
            demandeurUpdateDTO.setTelephone(demande.getDemandeur().getTelephone());
            demandeurUpdateDTO.setEmail(demande.getDemandeur().getEmail());
            if (demande.getDemandeur().getSituationFamiliale() != null) {
                demandeurUpdateDTO.setIdSituationFamiliale(demande.getDemandeur().getSituationFamiliale().getId());
            }
        }
        dto.setDemandeurDTO(demandeurUpdateDTO);

        // Passeport
        PasseportDTO passeportDTO = new PasseportDTO();
        if (demande.getVisaTransformable() != null && demande.getVisaTransformable().getPasseport() != null) {
            Passeport passeport = demande.getVisaTransformable().getPasseport();
            passeportDTO.setNumero(passeport.getNumero());
            passeportDTO.setDateDelivrance(passeport.getDateDelivrance());
            passeportDTO.setDateExpiration(passeport.getDateExpiration());
        }
        dto.setPasseportDTO(passeportDTO);

        // VisaTransformable
        VisaTransformableDTO visaDTO = new VisaTransformableDTO();
        if (demande.getVisaTransformable() != null) {
            VisaTransformable visa = demande.getVisaTransformable();
            visaDTO.setReferenceVisa(visa.getReferenceVisa());
            visaDTO.setDateEntree(visa.getDateEntree());
            visaDTO.setLieuEntree(visa.getLieuEntree());
            visaDTO.setDateExpiration(visa.getDateExpiration());
        }
        dto.setVisaDTO(visaDTO);

        // Type Visa
        if (demande.getTypeVisa() != null) {
            dto.setIdTypeVisa(demande.getTypeVisa().getId());
        }

        // Pièces fournies
        List<Long> piecesFournies = new ArrayList<>();
        if (demande.getDemandePieces() != null) {
            piecesFournies = demande.getDemandePieces().stream()
                .filter(DemandePiece::getFourni)
                .map(dp -> dp.getPiece().getId())
                .collect(Collectors.toList());
        }
        dto.setPiecesFournies(piecesFournies);

        return dto;
    }
}
