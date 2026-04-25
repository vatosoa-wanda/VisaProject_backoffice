package com.visa.backoffice.mapper;

import com.visa.backoffice.dto.VisaTransformableDTO;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.VisaTransformable;
import org.springframework.stereotype.Component;

@Component
public class VisaTransformableMapper {

    /**
     * Convertit un DTO en entité, en attachant le demandeur et le passeport.
     * @param dto           données saisies dans le formulaire
     * @param demandeur     entité Demandeur résolue
     * @param passeport     entité Passeport résolue
     * @return              entité VisaTransformable (non persistée)
     */
    public VisaTransformable toEntity(VisaTransformableDTO dto, Demandeur demandeur, Passeport passeport) {
        VisaTransformable entity = new VisaTransformable();
        entity.setReferenceVisa(dto.getReferenceVisa());
        entity.setDateEntree(dto.getDateEntree());
        entity.setLieuEntree(dto.getLieuEntree());
        entity.setDateExpiration(dto.getDateExpiration());
        entity.setDemandeur(demandeur);
        entity.setPasseport(passeport);
        return entity;
    }

    /**
     * Convertit une entité en DTO pour affichage.
     * @param entity    entité persistée
     * @return          DTO de lecture
     */
    public VisaTransformableDTO toDTO(VisaTransformable entity) {
        VisaTransformableDTO dto = new VisaTransformableDTO();
        dto.setReferenceVisa(entity.getReferenceVisa());
        dto.setDateEntree(entity.getDateEntree());
        dto.setLieuEntree(entity.getLieuEntree());
        dto.setDateExpiration(entity.getDateExpiration());
        return dto;
    }
}
