package com.visa.backoffice.controller;

import com.visa.backoffice.service.DemandeVisaService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demandes-visa")
@CrossOrigin(origins = "*", maxAge = 3600)
public class DemandeVisaController {

    @Autowired
    private DemandeVisaService demandeVisaService;

}
