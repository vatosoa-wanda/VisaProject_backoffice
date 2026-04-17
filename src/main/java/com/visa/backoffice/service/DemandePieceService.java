package com.visa.backoffice.service;

import com.visa.backoffice.model.DemandePiece;
import com.visa.backoffice.repository.DemandePieceRepository;
import org.springframework.stereotype.Service;

@Service
public class DemandePieceService {

    private final DemandePieceRepository demandePieceRepository;

    public DemandePieceService(DemandePieceRepository demandePieceRepository) {
        this.demandePieceRepository = demandePieceRepository;
    }
}
