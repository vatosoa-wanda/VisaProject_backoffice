package com.visa.backoffice.service;

import com.visa.backoffice.model.Nationalite;
import com.visa.backoffice.repository.NationaliteRepository;
import org.springframework.stereotype.Service;

@Service
public class NationaliteService {

    private final NationaliteRepository nationaliteRepository;

    public NationaliteService(NationaliteRepository nationaliteRepository) {
        this.nationaliteRepository = nationaliteRepository;
    }
}
