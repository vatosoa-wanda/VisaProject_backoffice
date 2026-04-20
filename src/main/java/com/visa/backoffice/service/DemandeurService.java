package com.visa.backoffice.service;

import com.visa.backoffice.dto.DemandeurDTO;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Nationalite;
import com.visa.backoffice.model.SituationFamiliale;
import com.visa.backoffice.repository.DemandeurRepository;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DemandeurService {

    private final DemandeurRepository demandeurRepository;
    private final NationaliteRepository nationaliteRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;

    public DemandeurService(DemandeurRepository demandeurRepository,
                           NationaliteRepository nationaliteRepository,
                           SituationFamilialeRepository situationFamilialeRepository) {
        this.demandeurRepository = demandeurRepository;
        this.nationaliteRepository = nationaliteRepository;
        this.situationFamilialeRepository = situationFamilialeRepository;
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

    /**
     * Crée ou récupère un demandeur existant.
     *
     * Cherche d'abord un demandeur par nom et prénom, sinon crée un nouveau.
     *
     * @param dto les données du formulaire
     * @return le demandeur créé ou existant
     */
    public Demandeur creerOuRecuperer(DemandeurDTO dto) {
        // Chercher si un demandeur existe déjà avec le même nom et date de naissance
        Optional<Demandeur> existing = demandeurRepository.findByNomAndDateNaissance(dto.getNom(), dto.getDateNaissance());

        if (existing.isPresent()) {
            return existing.get();
        }

        // Créer un nouveau demandeur
        Demandeur demandeur = new Demandeur();
        demandeur.setNom(dto.getNom());
        demandeur.setPrenom(dto.getPrenom());
        demandeur.setNomJeuneFille(dto.getNomJeuneFille());
        demandeur.setDateNaissance(dto.getDateNaissance());
        demandeur.setLieuNaissance(dto.getLieuNaissance());
        demandeur.setAdresseMadagascar(dto.getAdresseMadagascar());
        demandeur.setTelephone(dto.getTelephone());
        demandeur.setEmail(dto.getEmail());

        // Résoudre SituationFamiliale
        if (dto.getIdSituationFamiliale() != null) {
            SituationFamiliale sf = situationFamilialeRepository.findById(dto.getIdSituationFamiliale())
                    .orElse(null);
            demandeur.setSituationFamiliale(sf);
        }

        // Résoudre Nationalité (obligatoire)
        Nationalite nationalite = nationaliteRepository.findById(dto.getIdNationalite())
                .orElseThrow(() -> new RuntimeException("Nationalité introuvable"));
        demandeur.setNationalite(nationalite);

        return demandeurRepository.save(demandeur);
    }
}
