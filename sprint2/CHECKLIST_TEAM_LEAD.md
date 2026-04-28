════════════════════════════════════════════════════════════════════════════════
  CHECKLIST TEAM LEAD : Préparation du Sprint 2 (Avant que Dev1 & Dev2 démarrent)
════════════════════════════════════════════════════════════════════════════════

OBJECTIF
────────────────────────────────────────────────────────────────────────────────
Préparer une base de code STABLE et ISOLÉE par domaine, pour que :
✅ Dev1 ET Dev2 travaillent en parallèle sans conflits de merge
✅ Les dépendances entre Dev1 et Dev2 sont claires et définies
✅ Chaque dev a ses routes/packages dédiés
✅ Le code partagé est préparé d'avance (DTOs, interfaces, formulaire.html)

════════════════════════════════════════════════════════════════════════════════
PHASE 1 : PRÉPARATION DES ENTITÉS & INFRASTRUCTURE (TL seul, 2-3h)
════════════════════════════════════════════════════════════════════════════════

✅ TÂCHE 1.1 : Exécuter le script SQL 240426.sql
   Commande :
   mysql -u [user] -p [password] [database] < script/240426.sql
   
   Vérifier :
   ├─ Table `visa` créée (id, reference_visa, date_debut, date_fin, id_passeport, id_demande)
   ├─ Table `carte_resident` créée (id, numero_carte UNIQUE, date_debut, date_fin, id_passeport, id_demande)
   └─ Colonne `demande.id_demande_origine` ADDED (nullable, FK vers demande.id)


✅ TÂCHE 1.2 : Créer les Entités JPA (Modèles)
   Fichiers à créer :
   ├─ src/main/java/com/visa/backoffice/model/Visa.java
   └─ src/main/java/com/visa/backoffice/model/CarteResident.java
   
   ⚠️  NE PAS laisser Dev1 créer ces entités
        → Ils partagent des propriétés (id_passeport, id_demande)
        → Risque de duplication / incohérence
   
   Code template minimal :
   ```java
   // Visa.java
   @Entity
   @Table(name = "visa")
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class Visa {
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(name = "reference_visa", unique = true, nullable = false)
       private String referenceVisa;
       
       @Column(name = "date_debut", nullable = false)
       private LocalDate dateDebut;
       
       @Column(name = "date_fin")
       private LocalDate dateFin;
       
       @ManyToOne(optional = false)
       @JoinColumn(name = "id_passeport", nullable = false)
       private Passeport passeport;
       
       @ManyToOne(optional = false)
       @JoinColumn(name = "id_demande", nullable = false)
       private Demande demande;
   }
   
   // CarteResident.java
   @Entity
   @Table(name = "carte_resident")
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class CarteResident {
       @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
       private Long id;
       
       @Column(name = "numero_carte", unique = true, nullable = false)
       private String numeroCarte;
       
       @Column(name = "date_debut", nullable = false)
       private LocalDate dateDebut;
       
       @Column(name = "date_fin")
       private LocalDate dateFin;
       
       @ManyToOne(optional = false)
       @JoinColumn(name = "id_passeport", nullable = false)
       private Passeport passeport;
       
       @ManyToOne(optional = false)
       @JoinColumn(name = "id_demande", nullable = false)
       private Demande demande;
   }
   ```


✅ TÂCHE 1.3 : Enrichir la classe Demande
   Fichier : src/main/java/com/visa/backoffice/model/Demande.java
   
   Ajouter dans Demande :
   ```java
   @Column(name = "id_demande_origine")
   private Long idDemandeOrigine;  // FK vers une autre demande NOUVELLE APPROUVEE
   
   // Optionnel mais utile : Helper lazy
   @ManyToOne
   @JoinColumn(name = "id_demande_origine", insertable = false, updatable = false)
   private Demande demandeOrigine;
   ```


✅ TÂCHE 1.4 : Créer les DTOs & Mappers (interfaces)
   Créer les DTOs sans implémentation complète (juste les champs) :
   
   Fichiers à créer :
   ├─ src/main/java/com/visa/backoffice/dto/VisaDTO.java
   ├─ src/main/java/com/visa/backoffice/dto/CarteResidentDTO.java
   ├─ src/main/java/com/visa/backoffice/dto/DemandeRechercheDTO.java
   ├─ src/main/java/com/visa/backoffice/dto/DemandeResumeeDTO.java
   └─ src/main/java/com/visa/backoffice/dto/DuplicataCreateDTO.java
   
   Code template :
   ```java
   // VisaDTO.java
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class VisaDTO {
       private Long id;
       private String referenceVisa;
       private LocalDate dateDebut;
       private LocalDate dateFin;
       private Long idPasseport;
       private Long idDemande;
   }
   
   // CarteResidentDTO.java
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class CarteResidentDTO {
       private Long id;
       private String numeroCarte;
       private LocalDate dateDebut;
       private LocalDate dateFin;
       private Long idPasseport;
       private Long idDemande;
   }
   
   // DemandeRechercheDTO.java (pour Dev2 : recherche de dossiers approuvés)
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class DemandeRechercheDTO {
       private String nom;              // Optionnel
       private String numeroPasSeport;  // Optionnel
       private String referenceVisa;    // Optionnel
   }
   
   // DemandeResumeeDTO.java (résultat de recherche)
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class DemandeResumeeDTO {
       private Long id;
       private String demandeurNom;
       private String demandeurPrenom;
       private String numeroPasSeport;
       private String referenceVisa;
       private LocalDateTime dateApproval;
   }
   
   // DuplicataCreateDTO.java (Dev2)
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class DuplicataCreateDTO {
       private Long idDemandeOrigine;  // REQUIRED
       // Pièces :
       private List<Long> piecesFournies;
   }
   
   // TransfertCreateDTO.java (Dev1)
   @Data @NoArgsConstructor @AllArgsConstructor @Builder
   public class TransfertCreateDTO {
       private Long idDemandeOrigine;  // REQUIRED
       @Valid
       private PasseportDTO passeportNouveau;  // REQUIRED
       // Pièces :
       private List<Long> piecesFournies;
   }
   ```


✅ TÂCHE 1.5 : Enrichir DemandeCreateDTO
   Fichier : src/main/java/com/visa/backoffice/dto/DemandeCreateDTO.java
   
   Ajouter ces champs :
   ```java
   // Contexte de la demande (Sprint 2)
   private Long idDemandeOrigine;              // Pour DUPLICATA/TRANSFERT avec antécédent
   private String typeDemande;                 // "NOUVELLE" | "DUPLICATA" | "TRANSFERT"
   private Boolean avecAntecedent;             // true = avec, false = sans
   
   // Nouveau passeport pour TRANSFERT
   @Valid
   private PasseportDTO passeportNouveauDTO;   // Pour TRANSFERT
   ```
   
   Utilité : Dev1 ET Dev2 utiliseront le même DTO enrichi pour formulaire.html
            → Pas de duplication, pas de confusion


════════════════════════════════════════════════════════════════════════════════
PHASE 2 : PRÉPARATION DU FORMULAIRE PARTAGÉ (TL seul, 2-3h)
════════════════════════════════════════════════════════════════════════════════

✅ TÂCHE 2.1 : Adapter formulaire.html (voir GUIDE_ADAPTATION_FORMULAIRE.md)
   
   Modifications à faire en une seule fois par TL :
   ├─ Ajouter titre dynamique avec bannière (th:if sur typeDemande)
   ├─ Ajouter sections read-only pour infos héritées (th:if sur avecAntecedent=true)
   │  ├─ État civil (lecture seule)
   │  ├─ Ancien passeport (lecture seule)
   │  └─ Ancien visa (lecture seule, si transfert)
   ├─ Ajouter bloc "NOUVEAU PASSEPORT" (th:if sur typeDemande=TRANSFERT)
   ├─ Entourer blocs existants avec th:if avecAntecedent != true
   └─ Ajouter validation JS optionnelle pour unicité numéro passeport
   
   Important : COMMIT SEUL ce fichier
             ├─ Commentez clairement les sections th:if/th:unless
             ├─ Expliquez la logique en haut du fichier
             ├─ Message de commit : "feat: adapt formulaire.html for Sprint2 (NOUVELLE/DUPLICATA/TRANSFERT)"
             └─ Aucune autre modification dans ce commit


✅ TÂCHE 2.2 : Créer les Repository interfaces
   Fichiers à créer (vides, juste les contrats) :
   ├─ src/main/java/com/visa/backoffice/repository/VisaRepository.java
   └─ src/main/java/com/visa/backoffice/repository/CarteResidentRepository.java
   
   Code :
   ```java
   // VisaRepository.java
   public interface VisaRepository extends JpaRepository<Visa, Long> {
       Visa findByDemandeId(Long idDemande);
       Visa findByPasseportId(Long idPasseport);
       List<Visa> findByDemandeIdAndDateFinAfter(Long idDemande, LocalDate date);
   }
   
   // CarteResidentRepository.java
   public interface CarteResidentRepository extends JpaRepository<CarteResident, Long> {
       CarteResident findByDemandeId(Long idDemande);
       CarteResident findByNumeroCarte(String numeroCarte);
   }
   ```


════════════════════════════════════════════════════════════════════════════════
PHASE 3 : PRÉPARATION DES ROUTES & CONTRÔLEURS (TL seul, 2-3h)
════════════════════════════════════════════════════════════════════════════════

✅ TÂCHE 3.1 : Créer les packages isolés par domaine
   ```
   src/main/java/com/visa/backoffice/
   ├── controller/
   │   ├── DemandeController.java        (Sprint 1 + existant)
   │   ├── DuplicataController.java      ← Dev2 travaillera ICI
   │   └── TransfertController.java      ← Dev1 travaillera ICI
   │
   ├── service/
   │   ├── DemandeService.java           (Sprint 1, Dev1 l'étendra)
   │   ├── VisaService.java              ← Dev1 crée ici
   │   ├── CarteResidentService.java     ← Dev1 crée ici
   │   └── PieceService.java             (Sprint 1, existant)
   ```
   
   Avantage : Dev1 n'y touche PAS à DuplicataController
            Dev2 n'y touche PAS à TransfertController
            → Zéro conflit de merge


✅ TÂCHE 3.2 : Créer DuplicataController vide (pour Dev2)
   Fichier : src/main/java/com/visa/backoffice/controller/DuplicataController.java
   
   Template minimal :
   ```java
   @Controller
   @RequestMapping("/duplicata")
   public class DuplicataController {
       // Dev2 implémentera :
       // GET /duplicata/formulaire
       // POST /duplicata/formulaire
       // GET /duplicata/{id}/confirmation
       
       private final DemandeService demandeService;
       private final TypeVisaRepository typeVisaRepository;
       // ... autres dépendances
       
       public DuplicataController(...) { }
       
       // Stubs que Dev2 complétera
       @GetMapping("/formulaire")
       public String afficherFormulaire(...) {
           throw new UnsupportedOperationException("À implémenter par Dev2");
       }
       
       @PostMapping("/formulaire")
       public String soumettreFormulaire(...) {
           throw new UnsupportedOperationException("À implémenter par Dev2");
       }
       
       @GetMapping("/{id}/confirmation")
       public String afficherConfirmation(...) {
           throw new UnsupportedOperationException("À implémenter par Dev2");
       }
   }
   ```


✅ TÂCHE 3.3 : Créer TransfertController vide (pour Dev1)
   Fichier : src/main/java/com/visa/backoffice/controller/TransfertController.java
   
   Même structure que DuplicataController, pour :
   // GET /transfert/formulaire
   // POST /transfert/formulaire
   // GET /transfert/{id}/confirmation


✅ TÂCHE 3.4 : Préparer DemandeController pour les modifications communes
   Fichier existant : src/main/java/com/visa/backoffice/controller/DemandeController.java
   
   Laisser tel quel (ne pas y ajouter les routes /duplicata ou /transfert)
   Mais noter dans les commentaires les méthodes qui vont être appelées par Dev1 :
   - approuverDemandeNouvelle() → sera appelée par DuplicataController et TransfertController


════════════════════════════════════════════════════════════════════════════════
PHASE 4 : INTERFACE DES SERVICES (TL seul, 2-3h)
════════════════════════════════════════════════════════════════════════════════

✅ TÂCHE 4.1 : Définir l'interface VisaService (contrats que Dev1 doit implémenter)
   Fichier : src/main/java/com/visa/backoffice/service/VisaService.java
   
   ```java
   @Service
   public class VisaService {
       private final VisaRepository visaRepository;
       private final PasseportRepository passeportRepository;
       
       public VisaService(VisaRepository visaRepository, PasseportRepository passeportRepository) {
           this.visaRepository = visaRepository;
           this.passeportRepository = passeportRepository;
       }
       
       /**
        * Créer un nouveau visa
        * RG-01 : appelé après approbation d'une demande NOUVELLE
        * RG-07 : appelé pour créer un nouveau visa en TRANSFERT
        * 
        * @param demande The demande to link with
        * @param passeport The passeport to link with
        * @return Created Visa with generated referenceVisa and dateDebut=today
        * @throws BusinessException if passeport is null
        */
       public Visa creer(Demande demande, Passeport passeport) {
           // Dev1 implémente ici
           // ✅ Valider passeport != null
           // ✅ Générer referenceVisa (ex: "VIS-" + timestamp)
           // ✅ Set dateDebut = LocalDate.now()
           // ✅ Set dateFin = null
           // ✅ Link à demande et passeport
           // ✅ Save et return
       }
       
       /**
        * Désactiver un visa
        * RG-03 : appelé en TRANSFERT pour marquer l'ancien visa comme inactif
        * 
        * @param idDemande The demande whose active visa should be deactivated
        * @throws BusinessException if no active visa found
        */
       public void desactiver(Long idDemande) {
           // Dev1 implémente ici
           // ✅ Charger visa actif de la demande (dateFin = null)
           // ✅ Set dateFin = LocalDate.now()
           // ✅ Save
       }
       
       // Autres méthodes que Dev1 peut ajouter au besoin
       public Visa findByDemandeId(Long idDemande) { /* ... */ }
       public Visa findByPasseportId(Long idPasseport) { /* ... */ }
   }
   ```


✅ TÂCHE 4.2 : Définir l'interface CarteResidentService (contrats que Dev1 doit implémenter)
   Fichier : src/main/java/com/visa/backoffice/service/CarteResidentService.java
   
   ```java
   @Service
   public class CarteResidentService {
       private final CarteResidentRepository carteResidentRepository;
       private final PasseportRepository passeportRepository;
       
       /**
        * Créer une nouvelle carte résident
        * RG-01 : appelé après approbation d'une demande NOUVELLE
        * RG-06 : appelé pour créer une nouvelle carte en DUPLICATA
        * 
        * @param demande The demande to link with
        * @param passeport The passeport to link with
        * @return Created CarteResident with generated numeroCarte and dateDebut=today
        * @throws BusinessException if passeport is null
        */
       public CarteResident creer(Demande demande, Passeport passeport) {
           // Dev1 implémente ici
           // ✅ Valider passeport != null → BusinessException sinon
           // ✅ Générer numeroCarte UNIQUE (ex: "RES-" + timestamp + random)
           // ✅ Vérifier numeroCarte n'existe pas déjà
           // ✅ Set dateDebut = LocalDate.now()
           // ✅ Set dateFin = null
           // ✅ Link à demande et passeport
           // ✅ Save et return
       }
   }
   ```


✅ TÂCHE 4.3 : Ajouter les contrats dans DemandeService (pour que Dev1 & Dev2 sachent quoi implémenter)
   Fichier existant : src/main/java/com/visa/backoffice/service/DemandeService.java
   
   Ajouter signatures (dev1 implémente) :
   ```java
   /**
    * Approuver une demande NOUVELLE
    * RG-01 : crée automatiquement visa + carte_resident après approbation
    * 
    * @param idDemande The id of the demande to approve
    * @param commentaire Optional approval comment
    * @param automatique If true, historique will say "Approbation automatique"
    * @throws BusinessException if statut != CREE or typeDemande != NOUVELLE
    */
   public void approuverDemandeNouvelle(Long idDemande, String commentaire, boolean automatique) {
       // Dev1 implémente ici
   }
   
   /**
    * Rechercher les demandes APPROUVEE type NOUVELLE
    * Utilisé par Dev2 pour DUPLICATA avec antécédent
    * 
    * @param criteresRecherche DemandeRechercheDTO (min 1 critère requis)
    * @return List<DemandeResumeeDTO>
    * @throws BusinessException if no criteria provided
    */
   public List<DemandeResumeeDTO> rechercherDemandesApprouvees(DemandeRechercheDTO criteresRecherche) {
       // Dev2 implémente ici (Dev1 peut aussi)
   }
   
   /**
    * Créer une demande DUPLICATA
    * RG-02, RG-06 : crée une demande DUPLICATA avec nouvelle carte_resident
    * 
    * @param dto DuplicataCreateDTO contenant idDemandeOrigine + pièces
    * @return Created demande DUPLICATA (statut=CREE)
    * @throws BusinessException if origine invalid or doublon exists
    */
   public DemandeResponseDTO creerDuplicata(DuplicataCreateDTO dto) {
       // Dev2 implémente ici
   }
   
   /**
    * Créer une demande TRANSFERT
    * RG-03, RG-07 : crée une demande TRANSFERT avec nouveau visa, ancien visa désactivé
    * 
    * @param dto TransfertCreateDTO contenant idDemandeOrigine + nouveauPasseport + pièces
    * @return Created demande TRANSFERT (statut=CREE)
    * @throws BusinessException if origine invalid, doublon, or same passeport number
    */
   public DemandeResponseDTO creerTransfert(TransfertCreateDTO dto) {
       // Dev1 implémente ici
   }
   ```


════════════════════════════════════════════════════════════════════════════════
PHASE 5 : DOCUMENTATION & CLARIFICATIONS (TL seul, 1h)
════════════════════════════════════════════════════════════════════════════════

✅ TÂCHE 5.1 : Créer un fichier SPRINT2_SYNC.md à la racine du projet
   
   Contenu :
   ```markdown
   # Sprint 2 : Synchronisation entre Dev1 et Dev2
   
   ## 📋 Tâches par responsable
   
   ### Dev1 (Priorité 1-5)
   1. Visa Entity + VisaService (creer, desactiver)
   2. CarteResident Entity + CarteResidentService (creer)
   3. DemandeService.approuverDemandeNouvelle()
   4. TransfertController.GET/POST (dépend de 1-3)
   5. DemandeService.creerTransfert() (dépend de 1-2)
   
   ### Dev2 (Après Dev1 >= priorité 2)
   1. DemandeService.rechercherDemandesApprouvees()
   2. DuplicataController.GET/POST
   3. DemandeService.creerDuplicata()
   4. Templates : confirmation-duplicata.html
   
   ## 🔗 Dépendances critiques
   
   Dev2 **ATTEND** Dev1 :
   ├─ Visa Entity (pour vérifier que visa n'est pas créé en DUPLICATA)
   ├─ CarteResident Entity (pour crierDuplicata)
   └─ approuverDemandeNouvelle() (appelée dans crierDuplicata sans antécédent)
   
   Dev1 **ATTEND** Dev2 :
   └─ (Aucune dépendance ! Dev1 est indépendant)
   
   ## ✅ Code partagé (stable, version de base)
   
   Ne pas modifier sans accord :
   ├─ DemandeCreateDTO (champs déjà enrichis par TL)
   ├─ formulaire.html (sections déjà préparées)
   ├─ DemandeController.afficherFormulaire() (Sprint 1 inchangé)
   └─ Repositories interfaces (VisaRepository, CarteResidentRepository)
   
   ## 🚀 Checklist avant de démarrer
   
   - [ ] Script SQL 240426.sql exécuté
   - [ ] Entités Visa + CarteResident + Demande enrichie créées
   - [ ] DTOs (VisaDTO, CarteResidentDTO, DuplicataCreateDTO, etc.) créés
   - [ ] DemandeCreateDTO enrichi
   - [ ] formulaire.html adapté
   - [ ] Repositories interfaces créés
   - [ ] DuplicataController + TransfertController créés (stubs)
   - [ ] DemandeService signatures ajoutées
   - [ ] Ce fichier SPRINT2_SYNC.md lu par Dev1 ET Dev2
   ```


✅ TÂCHE 5.2 : Créer un fichier de CONTRATS par Dev (qui liste ce qu'il doit implémenter)
   
   Fichier : /docs/SPRINT2_DEV1_TASKS.md
   ```markdown
   # Dev1 : Tâches Sprint 2
   
   ## 🔨 À implémenter (dans l'ordre)
   
   ### 1. VisaService (service/VisaService.java)
   - [ ] creer(Demande, Passeport) → Visa
      - Valider passeport != null
      - Générer referenceVisa
      - Set dateDebut = today, dateFin = null
      - Save et return
   - [ ] desactiver(idDemande) → void
      - Charger visa actif (dateFin == null)
      - Set dateFin = today
      - Save
   
   ### 2. CarteResidentService (service/CarteResidentService.java)
   - [ ] creer(Demande, Passeport) → CarteResident
      - Valider passeport != null
      - Générer numeroCarte UNIQUE
      - Set dateDebut = today, dateFin = null
      - Save et return
   
   ### 3. DemandeService.approuverDemandeNouvelle() (service/DemandeService.java)
   - [ ] Load demande
   - [ ] Vérify statut=CREE, type=NOUVELLE
   - [ ] Change statut to APPROUVEE
   - [ ] Create historique
   - [ ] Call visaService.creer()
   - [ ] Call carteResidentService.creer()
   
   ### 4. TransfertController (controller/TransfertController.java)
   - [ ] GET /transfert/formulaire (afficher formulaire)
   - [ ] POST /transfert/formulaire (traiter soumission)
   
   ### 5. DemandeService.creerTransfert() (service/DemandeService.java)
   - [ ] Verify origine (APPROUVEE + NOUVELLE)
   - [ ] Check no doublon TRANSFERT
   - [ ] Create new Passeport
   - [ ] Create demande TRANSFERT
   - [ ] visaService.desactiver()
   - [ ] visaService.creer() avec nouveau passeport
   
   ## 📝 Tests attendus
   - [ ] VisaService.creer() → referenceVisa generated, dateDebut=today
   - [ ] VisaService.desactiver() → dateFin set
   - [ ] CarteResidentService.creer() → numeroCarte generated, unique
   - [ ] approuverDemandeNouvelle() → statut changed, visa+carte created
   - [ ] TransfertController routes work
   - [ ] creerTransfert() complete flow
   ```
   
   Fichier : /docs/SPRINT2_DEV2_TASKS.md
   ```markdown
   # Dev2 : Tâches Sprint 2
   
   ## 🔨 À implémenter (dans l'ordre, après Dev1 >= tâche 2)
   
   ### 1. DemandeService.rechercherDemandesApprouvees() (service/DemandeService.java)
   - [ ] Validate at least 1 criteria provided (nom, numeroPasSeport, referenceVisa)
   - [ ] Query demandes where statut=APPROUVEE AND type=NOUVELLE
   - [ ] Filter by criteria provided
   - [ ] Return List<DemandeResumeeDTO>
   
   ### 2. DuplicataController (controller/DuplicataController.java)
   - [ ] GET /duplicata/formulaire (afficher formulaire avec option antécédent)
   - [ ] POST /duplicata/formulaire (traiter soumission)
   
   ### 3. DemandeService.creerDuplicata() (service/DemandeService.java)
   - [ ] Verify origine (APPROUVEE + NOUVELLE)
   - [ ] Check no doublon DUPLICATA
   - [ ] Create demande DUPLICATA (hérite demandeur, visa, typeVisa)
   - [ ] Create historique
   - [ ] Link pièces
   - [ ] carteResidentService.creer() ← nouvelle carte uniquement
   
   ### 4. Templates
   - [ ] confirmation-duplicata.html
   
   ## 📝 Tests attendus
   - [ ] rechercherDemandesApprouvees() filters correctly
   - [ ] DuplicataController routes work
   - [ ] creerDuplicata() avec antécédent → carte created, visa NOT created
   - [ ] creerDuplicata() sans antécédent → NOUVELLE approved auto, then DUPLICATA
   ```


✅ TÂCHE 5.3 : Envoyer un message de kickoff aux devs
   
   Contenu (Slack ou email) :
   ```
   🚀 Sprint 2 - Kickoff
   
   Avant de démarrer, préparez-vous :
   
   📖 À LIRE EN PRIORITÉ :
   - /sprint2/todo_sprint2_ADAPT.txt (vue d'ensemble)
   - /docs/SPRINT2_SYNC.md (synchronisation)
   - /docs/SPRINT2_DEV1_TASKS.md (pour Dev1)
   - /docs/SPRINT2_DEV2_TASKS.md (pour Dev2)
   - /sprint2/GUIDE_ADAPTATION_FORMULAIRE.md (formulaire.html)
   
   ✅ INFRA PRÊTE :
   - Script SQL 240426.sql exécuté
   - Entités Visa + CarteResident créées
   - formulaire.html adapté
   - DemandeCreateDTO enrichi
   - Repositories interfaces prêts
   - Controllers stubs créés
   
   ⚠️  ATTENTION CONFLITS :
   - Dev1 : travaillez UNIQUEMENT dans /controller/TransfertController.java
   - Dev2 : travaillez UNIQUEMENT dans /controller/DuplicataController.java
   - Dev1 & Dev2 : ÉVITEZ de modifier DemandeService en même temps
         → Délimitez bien qui implémente quelle méthode
   
   🔗 DÉPENDANCES :
   - Dev1 = prioritaire (Dev2 attend les entités Visa + CarteResident)
   - Dev1 tâche 1-3 AVANT Dev2 commence
   - Dev1 termine tâche 3 AVANT Dev2 se lance sur creerDuplicata
   
   Questions ? /thread avec le TL.
   ```


════════════════════════════════════════════════════════════════════════════════
SUMMARY : CHECKLIST TL AVANT SPRINT 2
════════════════════════════════════════════════════════════════════════════════

**PHASE 1 : ENTITÉS & INFRASTRUCTURE**
- [ ] Script SQL 240426.sql exécuté
- [ ] Visa.java créée
- [ ] CarteResident.java créée
- [ ] Demande.java enrichie (idDemandeOrigine)
- [ ] VisaDTO.java créée
- [ ] CarteResidentDTO.java créée
- [ ] DemandeRechercheDTO.java créée
- [ ] DemandeResumeeDTO.java créée
- [ ] DuplicataCreateDTO.java créée
- [ ] TransfertCreateDTO.java créée
- [ ] DemandeCreateDTO enrichie
- [ ] VisaRepository créée
- [ ] CarteResidentRepository créée

**PHASE 2 : FORMULAIRE & MAPPERS**
- [ ] formulaire.html adaptée (sections optionnelles)
- [ ] Git commit du formulaire (seul)

**PHASE 3 : ROUTES & CONTRÔLEURS**
- [ ] DuplicataController créé (stubs)
- [ ] TransfertController créé (stubs)
- [ ] DemandeController inchangé (Sprint 1)

**PHASE 4 : CONTRATS DE SERVICE**
- [ ] VisaService signatures (creer, desactiver)
- [ ] CarteResidentService signatures (creer)
- [ ] DemandeService enrichi avec signatures (approuverDemandeNouvelle, rechercherDemandesApprouvees, creerDuplicata, creerTransfert)

**PHASE 5 : DOCUMENTATION**
- [ ] SPRINT2_SYNC.md créé à la racine
- [ ] SPRINT2_DEV1_TASKS.md créé
- [ ] SPRINT2_DEV2_TASKS.md créé
- [ ] Kickoff message envoyé aux devs

════════════════════════════════════════════════════════════════════════════════
TEMPS TOTAL ESTIMÉ : 8-10 heures de travail TL
════════════════════════════════════════════════════════════════════════════════

Avec cette préparation :
✅ ZÉRO conflit de merge entre Dev1 et Dev2
✅ Chaque dev a ses domaines isolés
✅ Code partagé (DTOs, formulaire) préparé une fois
✅ Contrats clairs = moins de questions/blocages
✅ Dépendances identifiées = planification efficace
