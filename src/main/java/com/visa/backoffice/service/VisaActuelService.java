package com.visa.backoffice.service;

import com.visa.backoffice.model.Personne;
import com.visa.backoffice.model.VisaActuel;
import com.visa.backoffice.repository.VisaActuelRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class VisaActuelService {

    @Autowired
    private VisaActuelRepository visaActuelRepository;

    public VisaActuel save(VisaActuel visaActuel) {
        return visaActuelRepository.save(visaActuel);
    }

    public Optional<VisaActuel> findById(Integer id) {
        return visaActuelRepository.findById(id);
    }

    public List<VisaActuel> findAll() {
        return visaActuelRepository.findAll();
    }

    public void delete(Integer id) {
        visaActuelRepository.deleteById(id);
    }

    public Optional<VisaActuel> findByNumeroVisa(String numeroVisa) {
        return visaActuelRepository.findByNumeroVisa(numeroVisa);
    }

    public List<VisaActuel> findByPersonne(Personne personne) {
        return visaActuelRepository.findByPersonne(personne);
    }

    public List<VisaActuel> findByPersonneId(Integer personneId) {
        return visaActuelRepository.findByPersonneId(personneId);
    }

    public List<VisaActuel> findByDateExpirationBefore(LocalDate date) {
        return visaActuelRepository.findByDateExpirationBefore(date);
    }

    public List<VisaActuel> findByDateExpirationAfter(LocalDate date) {
        return visaActuelRepository.findByDateExpirationAfter(date);
    }

    public List<VisaActuel> findExpiredVisas() {
        return visaActuelRepository.findByDateExpirationBefore(LocalDate.now());
    }

    public List<VisaActuel> findExpiringVisas(LocalDate date) {
        return visaActuelRepository.findByDateExpirationBefore(date);
    }

    public VisaActuel update(Integer id, VisaActuel visaActuel) {
        if (visaActuelRepository.existsById(id)) {
            visaActuel.setId(id);
            return visaActuelRepository.save(visaActuel);
        }
        return null;
    }

    public boolean isVisaExpired(Integer visaId) {
        Optional<VisaActuel> visa = visaActuelRepository.findById(visaId);
        return visa.isPresent() && visa.get().getDateExpiration().isBefore(LocalDate.now());
    }

    public boolean isVisaValid(Integer visaId) {
        return !isVisaExpired(visaId);
    }
}
