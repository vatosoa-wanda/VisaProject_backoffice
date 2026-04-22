package com.visa.backoffice.service;

import com.visa.backoffice.dto.VisaTransformableDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.mapper.VisaTransformableMapper;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.VisaTransformable;
import com.visa.backoffice.repository.VisaTransformableRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class VisaTransformableService {

    private final VisaTransformableRepository visaTransformableRepository;
    private final VisaTransformableMapper visaTransformableMapper;

    public VisaTransformableService(VisaTransformableRepository visaTransformableRepository,
                                   VisaTransformableMapper visaTransformableMapper) {
        this.visaTransformableRepository = visaTransformableRepository;
        this.visaTransformableMapper = visaTransformableMapper;
    }

    /**
     * Crée et persiste un nouveau VisaTransformable.
     *
     * Règles appliquées :
     *   RG-01 : referenceVisa doit être unique
     *   RG-02 : dateExpiration > dateEntree
     *   RG-03 : passeport non null
     *
     * @param dto       données du formulaire
     * @param passeport entité Passeport résolue (non null)
     * @return          entité persistée
     * @throws BusinessException si référence déjà utilisée ou dates incohérentes
     */
    public VisaTransformable creer(VisaTransformableDTO dto, Passeport passeport) {
        // 1. Vérifier unicité referenceVisa
        if (visaTransformableRepository.findByReferenceVisa(dto.getReferenceVisa()).isPresent()) {
            throw new BusinessException("La référence visa '" + dto.getReferenceVisa() + "' est déjà utilisée.");
        }

        // 2. Vérifier cohérence des dates
        if (!dto.getDateExpiration().isAfter(dto.getDateEntree())) {
            throw new BusinessException("La date d'expiration doit être postérieure à la date d'entrée.");
        }

        // 3. Mapper et sauvegarder
        VisaTransformable entity = visaTransformableMapper.toEntity(dto, passeport);
        return visaTransformableRepository.save(entity);
    }

    /**
     * Vérifie si une référence visa est déjà en base.
     *
     * @param referenceVisa     référence à tester
     * @return                  true si elle existe déjà
     */
    public boolean existeParReference(String referenceVisa) {
        return visaTransformableRepository.findByReferenceVisa(referenceVisa).isPresent();
    }
}
