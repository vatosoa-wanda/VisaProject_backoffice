package com.visa.backoffice.service;

import com.visa.backoffice.model.VisaTransformable;
import com.visa.backoffice.repository.VisaTransformableRepository;
import org.springframework.stereotype.Service;

@Service
public class VisaTransformableService {

    private final VisaTransformableRepository visaTransformableRepository;

    public VisaTransformableService(VisaTransformableRepository visaTransformableRepository) {
        this.visaTransformableRepository = visaTransformableRepository;
    }
}
