package com.visa.backoffice.repository;

import com.visa.backoffice.model.Passeport;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PasseportRepository extends JpaRepository<Passeport, Long> {
    Optional<Passeport> findByNumero(String numero);
}
