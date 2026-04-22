package com.visa.backoffice.repository;

import com.visa.backoffice.model.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Long> {
    List<Piece> findByTypePieceCode(String code);

    List<Piece> findByTypePieceCodeIn(List<String> codes);
}
