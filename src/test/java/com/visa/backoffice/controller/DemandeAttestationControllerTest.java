// package com.visa.backoffice.controller;

// import com.visa.backoffice.model.Demande;
// import com.visa.backoffice.model.Demandeur;
// import com.visa.backoffice.model.Nationalite;
// import com.visa.backoffice.model.StatutDemande;
// import com.visa.backoffice.model.TypeDemande;
// import com.visa.backoffice.repository.DemandeRepository;
// import com.visa.backoffice.repository.DemandeurRepository;
// import com.visa.backoffice.repository.NationaliteRepository;
// import com.visa.backoffice.repository.StatutDemandeRepository;
// import com.visa.backoffice.repository.TypeDemandeRepository;
// import org.junit.jupiter.api.Test;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
// import org.springframework.boot.test.context.SpringBootTest;
// import org.springframework.http.MediaType;
// import org.springframework.test.web.servlet.MockMvc;
// import org.springframework.transaction.annotation.Transactional;

// import java.time.LocalDate;
// import java.time.LocalDateTime;

// import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
// import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// @SpringBootTest
// @AutoConfigureMockMvc
// @Transactional
// class DemandeAttestationControllerTest {

//     @Autowired
//     private MockMvc mockMvc;

//     @Autowired
//     private DemandeRepository demandeRepository;

//     @Autowired
//     private DemandeurRepository demandeurRepository;

//     @Autowired
//     private NationaliteRepository nationaliteRepository;

//     @Autowired
//     private TypeDemandeRepository typeDemandeRepository;

//     @Autowired
//     private StatutDemandeRepository statutDemandeRepository;

//     @Test
//     void getAttestation_retournePdf() throws Exception {
//         StatutDemande statutCree = statutDemandeRepository.save(StatutDemande.builder().libelle("CREE").build());
//         TypeDemande typeDemande = typeDemandeRepository.save(TypeDemande.builder().libelle("NOUVELLE").build());
//         Nationalite nat = nationaliteRepository.save(Nationalite.builder().libelle("Malagasy").build());

//         Demandeur demandeur = demandeurRepository.save(Demandeur.builder()
//             .nom("Test")
//             .prenom("User")
//             .dateNaissance(LocalDate.of(1990, 1, 1))
//             .nationalite(nat)
//             .adresseMadagascar("Adresse")
//             .telephone("0340000000")
//             .build());

//         Demande demande = demandeRepository.save(Demande.builder()
//             .dateDemande(LocalDateTime.now())
//             .demandeur(demandeur)
//             .typeDemande(typeDemande)
//             .statutDemande(statutCree)
//             .build());

//         mockMvc.perform(get("/demandes/{id}/attestation", demande.getId()))
//             .andExpect(status().isOk())
//             .andExpect(content().contentType(MediaType.APPLICATION_PDF))
//             .andExpect(header().exists("Content-Disposition"));
//     }
// }
