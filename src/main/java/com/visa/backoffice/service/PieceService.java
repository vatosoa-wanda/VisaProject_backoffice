package com.visa.backoffice.service;

import com.visa.backoffice.model.Piece;
import com.visa.backoffice.repository.PieceRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PieceService {

    @Autowired
    private PieceRepository pieceRepository;

    public Piece save(Piece piece) {
        return pieceRepository.save(piece);
    }

    public Optional<Piece> findById(Integer id) {
        return pieceRepository.findById(id);
    }

    public List<Piece> findAll() {
        return pieceRepository.findAll();
    }

    public void delete(Integer id) {
        pieceRepository.deleteById(id);
    }

    public Optional<Piece> findByNom(String nom) {
        return pieceRepository.findByNom(nom);
    }

    public List<Piece> findByType(String type) {
        return pieceRepository.findByType(type);
    }

    public List<Piece> findCommonPieces() {
        return pieceRepository.findByType("COMMUN");
    }

    public List<Piece> findInvestorPieces() {
        return pieceRepository.findByType("INVESTISSEUR");
    }

    public List<Piece> findWorkerPieces() {
        return pieceRepository.findByType("TRAVAILLEUR");
    }

    public Piece update(Integer id, Piece piece) {
        if (pieceRepository.existsById(id)) {
            piece.setId(id);
            return pieceRepository.save(piece);
        }
        return null;
    }
}
