package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.repository.VisaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class VisaServiceTest {

    @Mock
    private VisaRepository visaRepository;

    private VisaService visaService;

    @BeforeEach
    void setUp() {
        visaService = new VisaService(visaRepository);
    }

    @Test
    void creer_createsVisaWithDates() {
        Demande demande = Demande.builder().id(1L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        when(visaRepository.save(any(Visa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visa visa = visaService.creer(demande, passeport);

        assertNotNull(visa.getReferenceVisa());
        assertEquals(LocalDate.now(), visa.getDateDebut());
        assertNull(visa.getDateFin());
        assertEquals(passeport, visa.getPasseport());
    }

    @Test
    void desactiver_updatesDateFin() {
        Demande demande = Demande.builder().id(1L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        Visa visa = Visa.builder().id(3L).demande(demande).passeport(passeport).dateDebut(LocalDate.now().minusDays(5)).dateFin(null).build();
        when(visaRepository.findByDemandeId(1L)).thenReturn(visa);
        when(visaRepository.save(any(Visa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        visaService.desactiver(1L);

        assertEquals(LocalDate.now(), visa.getDateFin());
    }

    @Test
    void creer_throwsWhenPasseportMissing() {
        assertThrows(BusinessException.class, () -> visaService.creer(Demande.builder().id(1L).build(), null));
    }
}
