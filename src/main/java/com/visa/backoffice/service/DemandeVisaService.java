package com.visa.backoffice.service;

import com.visa.backoffice.model.DemandeVisa;
import com.visa.backoffice.model.Personne;
import com.visa.backoffice.model.Statut;
import com.visa.backoffice.model.TypeVisa;
import com.visa.backoffice.repository.DemandeVisaRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class DemandeVisaService {

    @Autowired
    private DemandeVisaRepository demandeVisaRepository;

    @Autowired
    private HistoriqueStatutService historiqueStatutService;

    public DemandeVisa save(DemandeVisa demandeVisa) {
        return demandeVisaRepository.save(demandeVisa);
    }

    public Optional<DemandeVisa> findById(Integer id) {
        return demandeVisaRepository.findById(id);
    }

    public List<DemandeVisa> findAll() {
        return demandeVisaRepository.findAll();
    }

    public void delete(Integer id) {
        demandeVisaRepository.deleteById(id);
    }

    public List<DemandeVisa> findByPersonne(Personne personne) {
        return demandeVisaRepository.findByPersonne(personne);
    }

    public List<DemandeVisa> findByPersonneId(Integer personneId) {
        return demandeVisaRepository.findByPersonneId(personneId);
    }

    public List<DemandeVisa> findByStatut(String statut) {
        return demandeVisaRepository.findByStatut(statut);
    }

    public List<DemandeVisa> findByStatutObj(Statut statut) {
        return demandeVisaRepository.findByStatutObj(statut);
    }

    public List<DemandeVisa> findByTypeVisa(TypeVisa typeVisa) {
        return demandeVisaRepository.findByTypeVisa(typeVisa);
    }

    public List<DemandeVisa> findByTypeDemande(String typeDemande) {
        return demandeVisaRepository.findByTypeDemande(typeDemande);
    }

    public List<DemandeVisa> findByDateDemandeAfter(LocalDateTime date) {
        return demandeVisaRepository.findByDateDemandeAfter(date);
    }

    public List<DemandeVisa> findByDateDemandeBefore(LocalDateTime date) {
        return demandeVisaRepository.findByDateDemandeBefore(date);
    }

    public List<DemandeVisa> findByDemandeOriginale(DemandeVisa demandeOriginale) {
        return demandeVisaRepository.findByDemandeOriginale(demandeOriginale);
    }

    public List<DemandeVisa> findByStatutAndPersonneId(String statut, Integer personneId) {
        return demandeVisaRepository.findByStatutAndPersonneId(statut, personneId);
    }

    public void updateStatut(Integer demandeId, Statut nouveauStatut, String commentaire) {
        Optional<DemandeVisa> demandeOpt = demandeVisaRepository.findById(demandeId);
        if (demandeOpt.isPresent()) {
            DemandeVisa demande = demandeOpt.get();
            demande.setStatutObj(nouveauStatut);
            demandeVisaRepository.save(demande);

            historiqueStatutService.enregistrerChangement(demande, nouveauStatut, commentaire);
        }
    }

    public DemandeVisa update(Integer id, DemandeVisa demandeVisa) {
        if (demandeVisaRepository.existsById(id)) {
            demandeVisa.setId(id);
            return demandeVisaRepository.save(demandeVisa);
        }
        return null;
    }

    public List<DemandeVisa> findPendingRequests() {
        return demandeVisaRepository.findByStatut("EN_ATTENTE");
    }

    public List<DemandeVisa> findProcessingRequests() {
        return demandeVisaRepository.findByStatut("EN_COURS");
    }

    public List<DemandeVisa> findNewRequests() {
        return demandeVisaRepository.findByTypeDemande("NOUVELLE");
    }

    public List<DemandeVisa> findRenewalRequests() {
        return demandeVisaRepository.findByTypeDemande("RENOUVELLEMENT");
    }
}
