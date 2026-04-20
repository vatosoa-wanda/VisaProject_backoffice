package com.visa.backoffice.mapper;

import com.visa.backoffice.dto.VisaTransformableDTO;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.VisaTransformable;
import org.springframework.stereotype.Component;

@Component
public class VisaTransformableMapper {

    /**
     * Convertit un DTO en entité, en attachant le passeport.
     * @param dto           données saisies dans le formulaire
     * @param passeport     entité Passeport résolue (via Dev1)
     * @return              entité VisaTransformable (non persistée)
     */
    public VisaTransformable toEntity(VisaTransformableDTO dto, Passeport passeport) {
        VisaTransformable entity = new VisaTransformable();
        entity.setReferenceVisa(dto.getReferenceVisa());
        entity.setDateEntree(dto.getDateEntree());
        entity.setLieuEntree(dto.getLieuEntree());
        entity.setDateExpiration(dto.getDateExpiration());
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
