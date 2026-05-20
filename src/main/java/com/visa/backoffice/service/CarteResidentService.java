package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.CarteResident;
import com.visa.backoffice.model.CarteResidentPasseport;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.CarteResidentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CarteResidentService {

    private final CarteResidentRepository carteResidentRepository;
    private final CarteResidentPasseportService carteResidentPasseportService;

    public CarteResidentService(CarteResidentRepository carteResidentRepository,
                               CarteResidentPasseportService carteResidentPasseportService) {
        this.carteResidentRepository = carteResidentRepository;
        this.carteResidentPasseportService = carteResidentPasseportService;
    }

    /**
     * Créer une nouvelle carte résident
     * RG-01 : appelé après approbation d'une demande NOUVELLE
     * RG-06 : appelé pour créer une nouvelle carte en DUPLICATA
     *
     * @param demande The demande to link with
     * @param passeport The passeport to link with
     * @return Created CarteResident with generated numeroCarte and dateDebut=today
     * @throws BusinessException if passeport is null
     */
    public CarteResident creer(Demande demande, Passeport passeport) {
        if (passeport == null) {
            throw new BusinessException("Passeport requis pour créer une carte résident");
        }

        String numero;
        int attempts = 0;
        do {
            long ts = System.currentTimeMillis() / 1000L;
            int rand = (int) (Math.random() * 9000) + 1000;
            numero = String.format("CRD-%d-%04d", ts, rand);
            attempts++;
            if (attempts > 5) break;
        } while (carteResidentRepository.findByNumeroCarte(numero) != null);

        CarteResident carte = new CarteResident();
        carte.setNumeroCarte(numero);
        carte.setDateDebut(java.time.LocalDate.now());
        carte.setDateFin(null);

        CarteResident savedCarte = carteResidentRepository.save(carte);
        carteResidentPasseportService.creer(savedCarte, passeport, demande);

        return savedCarte;
    }

    /**
     * Crée une nouvelle carte résident pour un duplicata.
     * La nouvelle carte est liée à la demande DUPLICATA et reprend, si possible,
     * la date de fin de la carte de la demande d'origine.
     */
    public CarteResident creerPourDuplicata(Demande demandeDuplicata, Passeport passeport) {
        if (demandeDuplicata == null) {
            throw new BusinessException("Demande duplicata requise pour créer une carte résident");
        }
        if (passeport == null) {
            throw new BusinessException("Passeport requis pour créer une carte résident");
        }

        String numero;
        int attempts = 0;
        do {
            long ts = System.currentTimeMillis() / 1000L;
            int rand = (int) (Math.random() * 9000) + 1000;
            numero = String.format("CRD-%d-%04d", ts, rand);
            attempts++;
            if (attempts > 5) {
                break;
            }
        } while (carteResidentRepository.findByNumeroCarte(numero) != null);

        CarteResident carteOrigine = null;
        if (demandeDuplicata.getIdDemandeOrigine() != null) {
            List<CarteResidentPasseport> cartesOrigine = carteResidentPasseportService.findByDemandeId(demandeDuplicata.getIdDemandeOrigine());
            if (!cartesOrigine.isEmpty()) {
                carteOrigine = cartesOrigine.get(0).getCarteResident();
            }
        }

        CarteResident carte = new CarteResident();
        carte.setNumeroCarte(numero);
        carte.setDateDebut(java.time.LocalDate.now());
        carte.setDateFin(carteOrigine != null ? carteOrigine.getDateFin() : null);

        CarteResident savedCarte = carteResidentRepository.save(carte);
        carteResidentPasseportService.creer(savedCarte, passeport, demandeDuplicata);

        return savedCarte;
    }

    /**
     * Récupérer une carte résident par id_demande
     */
    public CarteResident findByDemandeId(Long idDemande) {
        List<CarteResidentPasseport> associations = carteResidentPasseportService.findByDemandeId(idDemande);
        if (associations.isEmpty()) {
            return null;
        }
        return associations.get(0).getCarteResident();
    }

    /**
     * Récupérer une carte résident par numero_carte
     */
    public CarteResident findByNumeroCarte(String numeroCarte) {
        return carteResidentRepository.findByNumeroCarte(numeroCarte);
    }
}
