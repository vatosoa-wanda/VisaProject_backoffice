package com.visa.backoffice.service;

import com.visa.backoffice.model.SituationFamiliale;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import org.springframework.stereotype.Service;

@Service
public class SituationFamilialeService {

    private final SituationFamilialeRepository situationFamilialeRepository;

    public SituationFamilialeService(SituationFamilialeRepository situationFamilialeRepository) {
        this.situationFamilialeRepository = situationFamilialeRepository;
    }
}
