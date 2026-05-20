package com.visa.backoffice.repository;

import com.visa.backoffice.model.Visa;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisaRepository extends JpaRepository<Visa, Long> {
    // Les méthodes findById() et findAll() sont héritées de JpaRepository
    // Les associations visa_passeport sont gérées par VisaPasseportRepository
}

