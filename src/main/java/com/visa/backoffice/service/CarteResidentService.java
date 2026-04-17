package com.visa.backoffice.service;

import com.visa.backoffice.model.CarteResident;
import com.visa.backoffice.repository.CarteResidentRepository;
import org.springframework.stereotype.Service;

@Service
public class CarteResidentService {

    private final CarteResidentRepository carteResidentRepository;

    public CarteResidentService(CarteResidentRepository carteResidentRepository) {
        this.carteResidentRepository = carteResidentRepository;
    }
}
