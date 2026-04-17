package com.visa.backoffice.service;

import com.visa.backoffice.model.Visa;
import com.visa.backoffice.repository.VisaRepository;
import org.springframework.stereotype.Service;

@Service
public class VisaService {

    private final VisaRepository visaRepository;

    public VisaService(VisaRepository visaRepository) {
        this.visaRepository = visaRepository;
    }
}
