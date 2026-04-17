package com.visa.backoffice.service;

import com.visa.backoffice.model.TypePiece;
import com.visa.backoffice.repository.TypePieceRepository;
import org.springframework.stereotype.Service;

@Service
public class TypePieceService {

    private final TypePieceRepository typePieceRepository;

    public TypePieceService(TypePieceRepository typePieceRepository) {
        this.typePieceRepository = typePieceRepository;
    }
}
