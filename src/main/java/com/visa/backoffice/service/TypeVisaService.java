package com.visa.backoffice.service;

import com.visa.backoffice.model.TypeVisa;
import com.visa.backoffice.repository.TypeVisaRepository;
import org.springframework.stereotype.Service;

@Service
public class TypeVisaService {

    private final TypeVisaRepository typeVisaRepository;

    public TypeVisaService(TypeVisaRepository typeVisaRepository) {
        this.typeVisaRepository = typeVisaRepository;
    }
}
