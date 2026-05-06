package com.visa.backoffice.service;

import com.visa.backoffice.dto.HistoriqueStatutDTO;
import com.visa.backoffice.model.HistoriqueStatut;
import com.visa.backoffice.repository.HistoriqueStatutRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class HistoriqueStatutService {

    private final HistoriqueStatutRepository historiqueStatutRepository;

    public HistoriqueStatutService(HistoriqueStatutRepository historiqueStatutRepository) {
        this.historiqueStatutRepository = historiqueStatutRepository;
    }

    public List<HistoriqueStatutDTO> getHistoriqueParDemande(Long demandeId) {
        return historiqueStatutRepository.findByDemandeIdOrderByDateChangementAsc(demandeId)
                .stream()
                .map(this::convertToDTO)
                .toList();
    }

    private HistoriqueStatutDTO convertToDTO(HistoriqueStatut historique) {
        return HistoriqueStatutDTO.builder()
                .id(historique.getId())
                .demandeId(historique.getDemande().getId())
                .statutDemande(historique.getStatutDemande().getLibelle())
                .dateChangement(historique.getDateChangement())
                .commentaire(historique.getCommentaire())
                .build();
    }
}

