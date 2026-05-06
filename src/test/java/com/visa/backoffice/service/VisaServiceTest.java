package com.visa.backoffice.service;

import com.visa.backoffice.exception.BusinessException;
import com.visa.backoffice.model.Demande;
import com.visa.backoffice.model.Passeport;
import com.visa.backoffice.model.Visa;
import com.visa.backoffice.model.VisaPasseport;
import com.visa.backoffice.repository.VisaPasseportRepository;
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

    @Mock
    private VisaPasseportRepository visaPasseportRepository;

    private VisaPasseportService visaPasseportService;
    private VisaService visaService;

    @BeforeEach
    void setUp() {
        visaPasseportService = new VisaPasseportService(visaPasseportRepository);
        visaService = new VisaService(visaRepository, visaPasseportService);
    }

    @Test
    void creer_createsVisaAndAssociation() {
        Demande demande = Demande.builder().id(1L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        when(visaRepository.save(any(Visa.class))).thenAnswer(invocation -> {
            Visa v = invocation.getArgument(0);
            v.setId(3L);
            return v;
        });
        when(visaPasseportRepository.save(any(VisaPasseport.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Visa visa = visaService.creer(demande, passeport);

        assertNotNull(visa.getReferenceVisa());
        assertEquals(LocalDate.now(), visa.getDateDebut());
        assertNull(visa.getDateFin());
    }

    @Test
    void desactiver_updatesDateFin() {
        Demande demande = Demande.builder().id(1L).build();
        Passeport passeport = Passeport.builder().id(2L).numero("P123").build();
        Visa visa = Visa.builder().id(3L).dateDebut(LocalDate.now().minusDays(5)).dateFin(null).build();
        VisaPasseport association = VisaPasseport.builder()
                .id(1L)
                .visa(visa)
                .passeport(passeport)
                .demande(demande)
                .build();
        
        when(visaPasseportRepository.findByDemandeId(1L))
                .thenReturn(java.util.List.of(association));
        when(visaRepository.save(any(Visa.class))).thenAnswer(invocation -> invocation.getArgument(0));

        visaService.desactiver(1L);

        assertEquals(LocalDate.now(), visa.getDateFin());
    }

    @Test
    void creer_throwsWhenPasseportMissing() {
        assertThrows(BusinessException.class, () -> visaService.creer(Demande.builder().id(1L).build(), null));
    }
}
