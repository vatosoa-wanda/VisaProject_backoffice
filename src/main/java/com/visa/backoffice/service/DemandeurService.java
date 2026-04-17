package com.visa.backoffice.service;

import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.repository.DemandeurRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class DemandeurService {

    private final DemandeurRepository demandeurRepository;

    public DemandeurService(DemandeurRepository demandeurRepository) {
        this.demandeurRepository = demandeurRepository;
    }

    public List<Demandeur> findAll() {
        return demandeurRepository.findAll();
    }

    public Optional<Demandeur> findById(Long id) {
        return demandeurRepository.findById(id);
    }

    public Demandeur save(Demandeur demandeur) {
        return demandeurRepository.save(demandeur);
    }

    public void deleteById(Long id) {
        demandeurRepository.deleteById(id);
    }
}
