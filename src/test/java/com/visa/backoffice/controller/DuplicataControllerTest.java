package com.visa.backoffice.controller;

import com.visa.backoffice.dto.DemandeCreateDTO;
import com.visa.backoffice.dto.DemandeResponseDTO;
import com.visa.backoffice.dto.DemandeurDTO;
import com.visa.backoffice.dto.DemandeRechercheDTO;
import com.visa.backoffice.dto.DuplicataCreateDTO;
import com.visa.backoffice.dto.PasseportDTO;
import com.visa.backoffice.dto.VisaTransformableDTO;
import com.visa.backoffice.repository.NationaliteRepository;
import com.visa.backoffice.repository.SituationFamilialeRepository;
import com.visa.backoffice.repository.TypeVisaRepository;
import com.visa.backoffice.service.DemandeService;
import com.visa.backoffice.service.PieceService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ui.ExtendedModelMap;
import org.springframework.ui.Model;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class DuplicataControllerTest {

    @Mock private DemandeService demandeService;
    @Mock private TypeVisaRepository typeVisaRepository;
    @Mock private SituationFamilialeRepository situationFamilialeRepository;
    @Mock private NationaliteRepository nationaliteRepository;
    @Mock private PieceService pieceService;

    @Captor private ArgumentCaptor<DemandeCreateDTO> demandeCreateCaptor;
    @Captor private ArgumentCaptor<DuplicataCreateDTO> duplicataCaptor;

    @InjectMocks private DuplicataController controller;

    @Test
    void afficherFormulaire_withoutParameters_opensFullNoAntecedentForm() {
        when(typeVisaRepository.findAll()).thenReturn(List.of());
        when(situationFamilialeRepository.findAll()).thenReturn(List.of());
        when(nationaliteRepository.findAll()).thenReturn(List.of());
        when(pieceService.getPiecesCommunes()).thenReturn(List.of());

        Model model = new ExtendedModelMap();

        String view = controller.afficherFormulaire(null, null, null, null, new DemandeRechercheDTO(), model);

        assertEquals("demande/formulaire", view);
        assertEquals(Boolean.TRUE, model.getAttribute("modeSansAntecedent"));
        assertEquals(Boolean.FALSE, model.getAttribute("modeAvecAntecedent"));
        assertNotNull(model.getAttribute("demandeForm"));
        assertEquals(Boolean.FALSE, ((DemandeCreateDTO) model.getAttribute("demandeForm")).getAvecAntecedent());
    }

    @Test
    void soumettreFormulaire_withoutAntecedent_createsNouvelleThenDuplicata() {
        DemandeResponseDTO nouvelle = DemandeResponseDTO.builder().id(10L).build();
        DemandeResponseDTO duplicata = DemandeResponseDTO.builder().id(20L).build();

        when(demandeService.creerDemande(any(DemandeCreateDTO.class))).thenReturn(nouvelle);
        when(demandeService.creerDuplicata(any(DuplicataCreateDTO.class))).thenReturn(duplicata);

        DemandeCreateDTO dto = DemandeCreateDTO.builder()
                .avecAntecedent(false)
                .typeDemande("DUPLICATA")
                .idTypeVisa(1L)
                .demandeurDTO(DemandeurDTO.builder()
                        .nom("Dupont")
                        .prenom("Paul")
                        .dateNaissance(LocalDate.of(1990, 1, 1))
                        .idSituationFamiliale(1L)
                        .idNationalite(1L)
                        .adresseMadagascar("Adresse")
                        .telephone("0340000000")
                        .build())
                .passeportDTO(PasseportDTO.builder()
                        .numero("P123")
                        .dateDelivrance(LocalDate.of(2020, 1, 1))
                        .dateExpiration(LocalDate.of(2030, 1, 1))
                        .build())
                .visaDTO(VisaTransformableDTO.builder()
                        .referenceVisa("VISA-1")
                        .dateEntree(LocalDate.of(2024, 1, 1))
                        .lieuEntree("Antananarivo")
                        .dateExpiration(LocalDate.of(2025, 1, 1))
                        .build())
                .piecesFournies(List.of(1L, 2L))
                .build();

        BindingResult bindingResult = new BeanPropertyBindingResult(dto, "demandeForm");

        String redirect = controller.soumettreFormulaire(dto, bindingResult, new ExtendedModelMap());

        assertEquals("redirect:/duplicata/20/confirmation", redirect);
                var order = inOrder(demandeService);
                order.verify(demandeService).creerDemande(demandeCreateCaptor.capture());
                order.verify(demandeService).approuverDemandeNouvelle(10L, null, true);
                order.verify(demandeService).creerDuplicata(duplicataCaptor.capture());
        assertEquals(Boolean.FALSE, demandeCreateCaptor.getValue().getAvecAntecedent());
        assertEquals(10L, duplicataCaptor.getValue().getIdDemandeOrigine());
        assertEquals(List.of(1L, 2L), duplicataCaptor.getValue().getPiecesFournies());
    }
}
