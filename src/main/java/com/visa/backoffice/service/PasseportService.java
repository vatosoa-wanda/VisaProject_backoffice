package com.visa.backoffice.service;

import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Personne;
import com.visa.backoffice.repository.PasseportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PasseportService {

    @Autowired
    private PasseportRepository passeportRepository;

    public Passeport save(Passeport passeport) {
        return passeportRepository.save(passeport);
    }

    public Optional<Passeport> findById(Integer id) {
        return passeportRepository.findById(id);
    }

    public List<Passeport> findAll() {
        return passeportRepository.findAll();
    }

    public void delete(Integer id) {
        passeportRepository.deleteById(id);
    }

    public Optional<Passeport> findByNumero(String numero) {
        return passeportRepository.findByNumero(numero);
    }

    public List<Passeport> findByPersonne(Personne personne) {
        return passeportRepository.findByPersonne(personne);
    }

    public List<Passeport> findByPersonneId(Integer personneId) {
        return passeportRepository.findByPersonneId(personneId);
    }

    public List<Passeport> findByDateExpirationBefore(LocalDate date) {
        return passeportRepository.findByDateExpirationBefore(date);
    }

    public List<Passeport> findByDateExpirationAfter(LocalDate date) {
        return passeportRepository.findByDateExpirationAfter(date);
    }

    public List<Passeport> findExpiredPassports() {
        return passeportRepository.findByDateExpirationBefore(LocalDate.now());
    }

    public Passeport update(Integer id, Passeport passeport) {
        if (passeportRepository.existsById(id)) {
            passeport.setId(id);
            return passeportRepository.save(passeport);
        }
        return null;
    }
}
