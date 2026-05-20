package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.CarteResident;
import com.visa.backoffice.model.CarteResidentPasseport;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.repository.CarteResidentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarteResidentServiceTest {

    @Mock
    private CarteResidentRepository carteResidentRepository;

    @Mock
    private CarteResidentPasseportService carteResidentPasseportService;

    private CarteResidentService carteResidentService;

    @BeforeEach
    void setUp() {
        carteResidentService = new CarteResidentService(carteResidentRepository, carteResidentPasseportService);
    }

    @Test
    void creer_createsCardWithDates() {
        Demande demande = Demande.builder().id(1L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        when(carteResidentRepository.findByNumeroCarte(any())).thenReturn(null);
        when(carteResidentRepository.save(any(CarteResident.class))).thenAnswer(invocation -> {
            CarteResident carte = invocation.getArgument(0);
            carte.setId(1L);
            return carte;
        });
        when(carteResidentPasseportService.creer(any(), any(), any())).thenReturn(null);

        CarteResident carte = carteResidentService.creer(demande, passeport);

        assertNotNull(carte.getNumeroCarte());
        assertEquals(LocalDate.now(), carte.getDateDebut());
        assertNull(carte.getDateFin());
    }

    @Test
    void creer_throwsWhenPasseportMissing() {
        assertThrows(BusinessException.class, () -> carteResidentService.creer(Demande.builder().id(1L).build(), null));
    }

    @Test
    void creerPourDuplicata_reusesOriginCardEndDate() {
        Demande origine = Demande.builder().id(10L).build();
        Demande duplicata = Demande.builder().id(20L).idDemandeOrigine(10L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        CarteResident carteOrigine = CarteResident.builder()
                .id(1L)
                .numeroCarte("CRD-ORIG-001")
                .dateDebut(LocalDate.now().minusYears(1))
                .dateFin(LocalDate.now().plusYears(1))
                .build();

        when(carteResidentRepository.findByNumeroCarte(any())).thenReturn(null);
        List<CarteResidentPasseport> cartesOrigine = new ArrayList<>();
        CarteResidentPasseport crp = CarteResidentPasseport.builder()
                .id(1L)
                .carteResident(carteOrigine)
                .passeport(passeport)
                .demande(origine)
                .build();
        cartesOrigine.add(crp);
        when(carteResidentPasseportService.findByDemandeId(10L)).thenReturn(cartesOrigine);
        when(carteResidentRepository.save(any(CarteResident.class))).thenAnswer(invocation -> {
            CarteResident carte = invocation.getArgument(0);
            carte.setId(2L);
            return carte;
        });
        when(carteResidentPasseportService.creer(any(), any(), any())).thenReturn(null);

        CarteResident carte = carteResidentService.creerPourDuplicata(duplicata, passeport);

        assertEquals(LocalDate.now(), carte.getDateDebut());
        assertEquals(carteOrigine.getDateFin(), carte.getDateFin());
    }
}

