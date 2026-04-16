package com.visa.backoffice.repository;

import com.visa.backoffice.model.Piece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PieceRepository extends JpaRepository<Piece, Integer> {
    Optional<Piece> findByNom(String nom);
    List<Piece> findByType(String type);
}
