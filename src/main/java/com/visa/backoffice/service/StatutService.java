package com.visa.backoffice.service;

import com.visa.backoffice.model.Statut;
import com.visa.backoffice.repository.StatutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class StatutService {

    @Autowired
    private StatutRepository statutRepository;

    public Statut save(Statut statut) {
        return statutRepository.save(statut);
    }

    public Optional<Statut> findById(Integer id) {
        return statutRepository.findById(id);
    }

    public List<Statut> findAll() {
        return statutRepository.findAll();
    }

    public void delete(Integer id) {
        statutRepository.deleteById(id);
    }

    public Optional<Statut> findByCode(String code) {
        return statutRepository.findByCode(code);
    }

    public Optional<Statut> findByLibelle(String libelle) {
        return statutRepository.findByLibelle(libelle);
    }

    public Statut getEnAttente() {
        return statutRepository.findByCode("EN_ATTENTE").orElse(null);
    }

    public Statut getEnCours() {
        return statutRepository.findByCode("EN_COURS").orElse(null);
    }

    public Statut getValidee() {
        return statutRepository.findByCode("VALIDEE").orElse(null);
    }

    public Statut getRefusee() {
        return statutRepository.findByCode("REFUSEE").orElse(null);
    }

    public Statut update(Integer id, Statut statut) {
        if (statutRepository.existsById(id)) {
            statut.setId(id);
            return statutRepository.save(statut);
        }
        return null;
    }
}
