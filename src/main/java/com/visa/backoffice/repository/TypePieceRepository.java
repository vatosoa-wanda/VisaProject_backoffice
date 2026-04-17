package com.visa.backoffice.repository;

import com.visa.backoffice.model.TypePiece;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TypePieceRepository extends JpaRepository<TypePiece, Long> {
}
