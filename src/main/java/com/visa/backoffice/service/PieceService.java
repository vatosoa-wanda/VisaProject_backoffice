package com.visa.backoffice.service;

import com.visa.backoffice.model.Piece;
import com.visa.backoffice.repository.PieceRepository;
import org.springframework.stereotype.Service;

@Service
public class PieceService {

    private final PieceRepository pieceRepository;

    public PieceService(PieceRepository pieceRepository) {
        this.pieceRepository = pieceRepository;
    }
}
