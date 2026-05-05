package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.CarteResident;
import com.visa.backoffice.model.CarteResidentPasseport;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.CarteResidentPasseportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
public class CarteResidentPasseportService {

    private final CarteResidentPasseportRepository carteResidentPasseportRepository;

    public CarteResidentPasseportService(CarteResidentPasseportRepository carteResidentPasseportRepository) {
        this.carteResidentPasseportRepository = carteResidentPasseportRepository;
    }

    /**
     * Crée une association carte_resident_passeport.
     * Appelée lors de la création d'une carte résident.
     *
     * @param carteResident la carte résident créée
     * @param passeport     le passeport associé
     * @param demande       la demande associée
     * @return              CarteResidentPasseport créée
     * @throws BusinessException si l'association existe déjà
     */
    public CarteResidentPasseport creer(CarteResident carteResident, Passeport passeport, Demande demande) {
        if (carteResident == null || passeport == null || demande == null) {
            throw new BusinessException("CarteResident, Passeport et Demande sont requis");
        }

        // Vérifier qu'une association identique n'existe pas déjà
        CarteResidentPasseport existant = carteResidentPasseportRepository
                .findByCarteResidentIdAndPasseportIdAndDemandeId(carteResident.getId(), passeport.getId(), demande.getId());
        if (existant != null) {
            throw new BusinessException("Une association carte_resident_passeport existe déjà pour cette carte, passeport et demande");
        }

        CarteResidentPasseport association = CarteResidentPasseport.builder()
                .carteResident(carteResident)
                .passeport(passeport)
                .demande(demande)
                .dateAssociation(LocalDateTime.now())
                .build();

        return carteResidentPasseportRepository.save(association);
    }

    /**
     * Récupère toutes les associations pour une carte résident donnée
     *
     * @param idCarteResident l'id de la carte résident
     * @return liste des CarteResidentPasseport
     */
    public List<CarteResidentPasseport> findByCarteResidentId(Long idCarteResident) {
        return carteResidentPasseportRepository.findByCarteResidentId(idCarteResident);
    }

    /**
     * Récupère toutes les associations pour un passeport donné
     *
     * @param idPasseport l'id du passeport
     * @return liste des CarteResidentPasseport
     */
    public List<CarteResidentPasseport> findByPasseportId(Long idPasseport) {
        return carteResidentPasseportRepository.findByPasseportId(idPasseport);
    }

    /**
     * Récupère toutes les associations pour une demande donnée
     *
     * @param idDemande l'id de la demande
     * @return liste des CarteResidentPasseport
     */
    public List<CarteResidentPasseport> findByDemandeId(Long idDemande) {
        return carteResidentPasseportRepository.findByDemandeId(idDemande);
    }

    /**
     * Récupère une association spécifique
     *
     * @param idCarteResident l'id de la carte résident
     * @param idPasseport     l'id du passeport
     * @param idDemande       l'id de la demande
     * @return CarteResidentPasseport ou null si non trouvée
     */
    public CarteResidentPasseport findByCarteResidentIdAndPasseportIdAndDemandeId(Long idCarteResident, Long idPasseport, Long idDemande) {
        return carteResidentPasseportRepository.findByCarteResidentIdAndPasseportIdAndDemandeId(idCarteResident, idPasseport, idDemande);
    }

    /**
     * Supprime toutes les associations pour une carte résident
     *
     * @param idCarteResident l'id de la carte résident
     */
    public void deleteByCarteResidentId(Long idCarteResident) {
        carteResidentPasseportRepository.deleteByCarteResidentId(idCarteResident);
    }

    /**
     * Supprime toutes les associations pour une demande
     *
     * @param idDemande l'id de la demande
     */
    public void deleteByDemandeId(Long idDemande) {
        carteResidentPasseportRepository.deleteByDemandeId(idDemande);
    }
}
