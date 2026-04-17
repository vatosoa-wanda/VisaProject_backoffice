package com.visa.backoffice.service;

import com.visa.backoffice.model.StatutDemande;
import com.visa.backoffice.repository.StatutDemandeRepository;
import org.springframework.stereotype.Service;

@Service
public class StatutDemandeService {

    private final StatutDemandeRepository statutDemandeRepository;

    public StatutDemandeService(StatutDemandeRepository statutDemandeRepository) {
        this.statutDemandeRepository = statutDemandeRepository;
    }
}
