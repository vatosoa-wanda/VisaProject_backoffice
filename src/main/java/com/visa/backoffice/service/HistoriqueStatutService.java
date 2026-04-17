package com.visa.backoffice.service;

import com.visa.backoffice.model.HistoriqueStatut;
import com.visa.backoffice.repository.HistoriqueStatutRepository;
import org.springframework.stereotype.Service;

@Service
public class HistoriqueStatutService {

    private final HistoriqueStatutRepository historiqueStatutRepository;

    public HistoriqueStatutService(HistoriqueStatutRepository historiqueStatutRepository) {
        this.historiqueStatutRepository = historiqueStatutRepository;
    }
}
