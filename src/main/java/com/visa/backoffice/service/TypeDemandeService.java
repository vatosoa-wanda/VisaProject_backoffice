package com.visa.backoffice.service;

import com.visa.backoffice.model.TypeDemande;
import com.visa.backoffice.repository.TypeDemandeRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeDemandeService {

    private final TypeDemandeRepository typeDemandeRepository;

    public TypeDemandeService(TypeDemandeRepository typeDemandeRepository) {
        this.typeDemandeRepository = typeDemandeRepository;
    }
}
