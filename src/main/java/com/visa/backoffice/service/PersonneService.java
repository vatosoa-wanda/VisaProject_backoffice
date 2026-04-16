package com.visa.backoffice.service;

import com.visa.backoffice.model.Personne;
import com.visa.backoffice.repository.PersonneRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PersonneService {

    @Autowired
    private PersonneRepository personneRepository;

    public Personne save(Personne personne) {
        return personneRepository.save(personne);
    }

    public Optional<Personne> findById(Integer id) {
        return personneRepository.findById(id);
    }

    public List<Personne> findAll() {
        return personneRepository.findAll();
    }

    public void delete(Integer id) {
        personneRepository.deleteById(id);
    }

    public Optional<Personne> findByEmail(String email) {
        return personneRepository.findByEmail(email);
    }

    public Optional<Personne> findByTelephone(String telephone) {
        return personneRepository.findByTelephone(telephone);
    }

    public List<Personne> findByNom(String nom) {
        return personneRepository.findByNom(nom);
    }

    public List<Personne> findByPrenom(String prenom) {
        return personneRepository.findByPrenom(prenom);
    }

    public List<Personne> findByNomAndPrenom(String nom, String prenom) {
        return personneRepository.findByNomAndPrenom(nom, prenom);
    }

    public List<Personne> findByNationalite(String nationalite) {
        return personneRepository.findByNationalite(nationalite);
    }

    public Personne update(Integer id, Personne personne) {
        if (personneRepository.existsById(id)) {
            personne.setId(id);
            return personneRepository.save(personne);
        }
        return null;
    }
}
