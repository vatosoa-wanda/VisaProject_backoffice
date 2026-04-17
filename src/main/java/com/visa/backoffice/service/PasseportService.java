package com.visa.backoffice.service;

import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.PasseportRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
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
}
