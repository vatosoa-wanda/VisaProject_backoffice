package com.visa.backoffice.service;

import com.visa.backoffice.model.Demande;
import com.visa.backoffice.repository.DemandeRepository;
import org.springframework.stereotype.Service;

@Service
public class DemandeService {

    private final DemandeRepository demandeRepository;

    public DemandeService(DemandeRepository demandeRepository) {
        this.demandeRepository = demandeRepository;
    }
}
