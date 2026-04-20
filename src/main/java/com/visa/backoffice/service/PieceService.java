package com.visa.backoffice.service;

import com.visa.backoffice.dto.PieceDTO;
import com.visa.backoffice.mapper.DemandeMapper;
import com.visa.backoffice.model.Piece;
import com.visa.backoffice.repository.PieceRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PieceService {

    private final PieceRepository pieceRepository;
    private final DemandeMapper demandeMapper;

    public PieceService(PieceRepository pieceRepository, DemandeMapper demandeMapper) {
        this.pieceRepository = pieceRepository;
        this.demandeMapper = demandeMapper;
    }

    /**
     * Retourne toutes les pièces communes (type COMMUN).
     *
     * @return liste de PieceDTO avec obligatoire/facultatif
     */
    public List<PieceDTO> getPiecesCommunes() {
        return pieceRepository.findByTypePieceCode("COMMUN")
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne les pièces spécifiques selon le libellé du type visa.
     *
     * @param libelleTypeVisa   "Travailleur" ou "Investisseur"
     * @return                  liste de PieceDTO spécifiques
     */
    public List<PieceDTO> getPiecesParTypeVisa(String libelleTypeVisa) {
        String code = libelleTypeVisa.toUpperCase();  // "TRAVAILLEUR" ou "INVESTISSEUR"
        return pieceRepository.findByTypePieceCode(code)
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les pièces : communes + spécifiques selon type visa.
     * Utilisé pour initialiser la liste complète lors du submit.
     *
     * @param libelleTypeVisa   "Travailleur" ou "Investisseur"
     * @return                  liste combinée de PieceDTO
     */
    public List<PieceDTO> getToutesPiecesPourTypeVisa(String libelleTypeVisa) {
        List<String> codes = List.of("COMMUN", libelleTypeVisa.toUpperCase());
        return pieceRepository.findByTypePieceCodeIn(codes)
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }
}
