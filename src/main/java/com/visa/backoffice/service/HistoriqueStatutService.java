package com.visa.backoffice.service;

import com.visa.backoffice.model.DemandeVisa;
import com.visa.backoffice.model.HistoriqueStatut;
import com.visa.backoffice.model.Statut;
import com.visa.backoffice.repository.HistoriqueStatutRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class HistoriqueStatutService {

    @Autowired
    private HistoriqueStatutRepository historiqueStatutRepository;

    /**
     * Enregistrer un changement de statut
     */
    public HistoriqueStatut enregistrerChangement(DemandeVisa demande, Statut statut, String commentaire) {
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatut(statut);
        historique.setCommentaire(commentaire);

        return historiqueStatutRepository.save(historique);
    }

    /**
     * Récupérer l'historique d'une demande
     */
    public List<HistoriqueStatut> findByDemande(DemandeVisa demande) {
        return historiqueStatutRepository.findByDemande(demande);
    }

    /**
     * Récupérer l'historique par ID de demande
     */
    public List<HistoriqueStatut> findByDemandeId(Integer demandeId) {
        return historiqueStatutRepository.findByDemandeId(demandeId);
    }

    /**
     * Supprimer tout l'historique d'une demande (optionnel)
     */
    public void deleteByDemande(DemandeVisa demande) {
        historiqueStatutRepository.deleteByDemande(demande);
    }

    /**
     * Récupérer tous les historiques
     */
    public List<HistoriqueStatut> findAll() {
        return historiqueStatutRepository.findAll();
    }
}