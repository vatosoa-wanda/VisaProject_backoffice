package com.visa.backoffice.service;

import com.visa.backoffice.dto.PasseportDTO;
import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demandeur;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.PasseportRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class PasseportService {

    private final PasseportRepository passeportRepository;

    public PasseportService(PasseportRepository passeportRepository) {
        this.passeportRepository = passeportRepository;
    }

    public List<Passeport> findAll() {
        return passeportRepository.findAll();
    }

    public Optional<Passeport> findById(Long id) {
        return passeportRepository.findById(id);
    }

    public Passeport save(Passeport passeport) {
        return passeportRepository.save(passeport);
    }

    public void deleteById(Long id) {
        passeportRepository.deleteById(id);
    }

    /**
     * Crée un nouveau passeport lié à un demandeur.
     *
     * @param dto       données du formulaire
     * @param demandeur le demandeur propriétaire du passeport
     * @return          le passeport créé et persiste
     * @throws BusinessException si le numéro de passeport existe déjà
     */
    public Passeport creer(PasseportDTO dto, Demandeur demandeur) {
        // Vérifier l'unicité du numéro de passeport
        if (passeportRepository.findByNumero(dto.getNumero()).isPresent()) {
            throw new BusinessException("Le numéro de passeport '" + dto.getNumero() + "' est déjà utilisé.");
        }

        Passeport passeport = new Passeport();
        passeport.setNumero(dto.getNumero());
        passeport.setDateDelivrance(dto.getDateDelivrance());
        passeport.setDateExpiration(dto.getDateExpiration());
        passeport.setDemandeur(demandeur);

        return passeportRepository.save(passeport);
    }
}
