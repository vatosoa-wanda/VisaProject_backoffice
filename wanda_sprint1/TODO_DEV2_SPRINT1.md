# TODO — DEV 2 : DEMANDE + VISA TRANSFORMABLE + STATUT + PIÈCES
## Sprint 1 — Feature : `feature/sprint1_2_Enregistrement_Nouvelle_Demande`

> **Convention de lecture**
> - `(O)` Obligatoire · `(F)` Facultatif · `(U)` Unique · `(H)` Hidden · `(S)` Select
> - `✅ TEST` = test unitaire à écrire immédiatement après l'étape
> - `⚠️ RG` = Règle de Gestion métier à respecter
> - `🔗 API DEV1` = dépendance sur le code fourni par Developer 1

---

## PRÉ-REQUIS — Ce que Dev1 doit avoir livré avant de commencer

| Élément attendu de Dev1 | Package |
|---|---|
| `Demandeur.java` (entity) | `entity/` |
| `Passeport.java` (entity) | `entity/` |
| `DemandeurDTO.java` | `dto/` |
| `PasseportDTO.java` | `dto/` |
| `DemandeurService.java` avec `creerOuRecuperer(DemandeurDTO) : Demandeur` | `service/` |
| `PasseportService.java` avec `creer(PasseportDTO, Demandeur) : Passeport` | `service/` |
| `SituationFamilialeRepository.java` avec `findAll()` | `repository/` |
| `NationaliteRepository.java` avec `findAll()` | `repository/` |

> Si Dev1 n'a pas encore livré, créer des stubs/interfaces pour ne pas bloquer.

---

## STRUCTURE DES FICHIERS À CRÉER

```
src/main/java/com/projet/visa/
├── entity/
│   ├── VisaTransformable.java
│   ├── TypeVisa.java
│   ├── Demande.java
│   ├── HistoriqueStatut.java
│   ├── Piece.java
│   └── DemandePiece.java
├── dto/
│   ├── VisaTransformableDTO.java
│   ├── DemandeCreateDTO.java
│   ├── DemandeResponseDTO.java
│   ├── PieceDTO.java
│   └── DemandePieceDTO.java
├── mapper/
│   ├── VisaTransformableMapper.java
│   └── DemandeMapper.java
├── repository/
│   ├── VisaTransformableRepository.java
│   ├── TypeVisaRepository.java
│   ├── DemandeRepository.java
│   ├── HistoriqueStatutRepository.java
│   ├── PieceRepository.java
│   └── DemandePieceRepository.java
├── service/
│   ├── VisaTransformableService.java
│   ├── PieceService.java
│   └── DemandeService.java
└── controller/
    └── DemandeController.java

src/main/resources/templates/
├── demande/
│   ├── formulaire.html
│   └── confirmation.html
└── fragments/
    ├── pieces-communes.html
    ├── pieces-travailleur.html
    └── pieces-investisseur.html

src/test/java/com/projet/visa/
├── service/
│   ├── VisaTransformableServiceTest.java
│   ├── PieceServiceTest.java
│   └── DemandeServiceTest.java
└── controller/
    └── DemandeControllerTest.java
```

---

---

# ÉTAPE 1 — ENTITÉS (Entity Layer)

> **Ordre d'implémentation obligatoire** : TypeVisa → VisaTransformable → Demande → HistoriqueStatut → Piece → DemandePiece

---

## 1.1 — `TypeVisa.java`

**Fichier** : `src/main/java/com/projet/visa/entity/TypeVisa.java`

```java
@Entity
@Table(name = "type_visa")
public class TypeVisa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false)
    private String libelle;   // valeurs en base : "Travailleur", "Investisseur"

    // getters / setters
}
```

**Notes** : entité simple, pas de relations. Les données sont pré-insérées en base (voir script SQL).

---

## 1.2 — `VisaTransformable.java`

**Fichier** : `src/main/java/com/projet/visa/entity/VisaTransformable.java`

```java
@Entity
@Table(name = "visa_transformable")
public class VisaTransformable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "reference_visa", nullable = false, unique = true)
    private String referenceVisa;

    @Column(name = "date_entree", nullable = false)
    private LocalDate dateEntree;

    @Column(name = "lieu_entree", nullable = false)
    private String lieuEntree;

    @Column(name = "date_expiration", nullable = false)
    private LocalDate dateExpiration;

    @ManyToOne
    @JoinColumn(name = "id_passeport", nullable = false)
    private Passeport passeport;   // 🔗 API DEV1 — entité Passeport

    // getters / setters
}
```

⚠️ **RG-01** : `referenceVisa` doit être unique en base (contrainte `unique = true` + vérification en service)
⚠️ **RG-02** : `dateExpiration` doit être strictement après `dateEntree` (vérification en service)
⚠️ **RG-03** : le `Passeport` est obligatoire (non null)

---

## 1.3 — `Demande.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Demande.java`

```java
@Entity
@Table(name = "demande")
public class Demande {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date_demande", nullable = false)
    private LocalDateTime dateDemande;

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;              // 🔗 API DEV1

    @ManyToOne
    @JoinColumn(name = "id_visa_transformable", nullable = false)
    private VisaTransformable visaTransformable;

    @ManyToOne
    @JoinColumn(name = "id_type_visa", nullable = false)
    private TypeVisa typeVisa;

    @ManyToOne
    @JoinColumn(name = "id_type_demande", nullable = false)
    private TypeDemande typeDemande;          // entité TeamLead

    @ManyToOne
    @JoinColumn(name = "id_statut_demande", nullable = false)
    private StatutDemande statutDemande;      // entité TeamLead

    @OneToMany(mappedBy = "demande", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DemandePiece> demandePieces = new ArrayList<>();

    // getters / setters
}
```

⚠️ **RG-04** : `dateDemande` = `LocalDateTime.now()` injecté automatiquement dans le service, jamais saisi par l'utilisateur
⚠️ **RG-05** : `typeDemande` = toujours `"NOUVELLE"` à la création (valeur forcée en service)
⚠️ **RG-06** : `statutDemande` = toujours `"CREE"` à la création (valeur forcée en service)

---

## 1.4 — `HistoriqueStatut.java`

**Fichier** : `src/main/java/com/projet/visa/entity/HistoriqueStatut.java`

```java
@Entity
@Table(name = "historique_statut")
public class HistoriqueStatut {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_statut_demande", nullable = false)
    private StatutDemande statutDemande;

    @Column(name = "date_changement", nullable = false)
    private LocalDateTime dateChangement;

    @Column(name = "commentaire")
    private String commentaire;

    // getters / setters
}
```

⚠️ **RG-07** : **toute création ou modification de statut sur une `Demande` doit générer une ligne dans `historique_statut`**. C'est une règle non négociable, gérée dans `DemandeService`.

---

## 1.5 — `Piece.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Piece.java`

```java
@Entity
@Table(name = "piece")
public class Piece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false)
    private String nom;

    @Column(name = "obligatoire")
    private Boolean obligatoire = false;

    @ManyToOne
    @JoinColumn(name = "id_type_piece")
    private TypePiece typePiece;   // entité TeamLead — valeurs : COMMUN / TRAVAILLEUR / INVESTISSEUR

    // getters / setters
}
```

---

## 1.6 — `DemandePiece.java`

**Fichier** : `src/main/java/com/projet/visa/entity/DemandePiece.java`

```java
@Entity
@Table(name = "demande_piece")
public class DemandePiece {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "id_demande", nullable = false)
    private Demande demande;

    @ManyToOne
    @JoinColumn(name = "id_piece", nullable = false)
    private Piece piece;

    @Column(name = "fourni")
    private Boolean fourni = false;

    // getters / setters
}
```

⚠️ **RG-08** : `fourni = true` seulement si l'utilisateur a coché la pièce dans le formulaire
⚠️ **RG-09** : si `piece.obligatoire = true` ET `fourni = false` → exception métier levée lors de l'enregistrement

---

### ✅ TEST ÉTAPE 1 — `EntityInstantiationTest.java`

Valider que toutes les entités s'instancient correctement et que JPA peut créer les tables.

```java
// Test 1.1 — TypeVisa instanciable
TypeVisa tv = new TypeVisa();
tv.setLibelle("Travailleur");
assertNotNull(tv.getLibelle());

// Test 1.2 — VisaTransformable cohérence dates
VisaTransformable visa = new VisaTransformable();
visa.setDateEntree(LocalDate.of(2024, 1, 1));
visa.setDateExpiration(LocalDate.of(2024, 6, 1));
assertTrue(visa.getDateExpiration().isAfter(visa.getDateEntree()));

// Test 1.3 — Demande valeurs par défaut
Demande d = new Demande();
d.setDemandePieces(new ArrayList<>());
assertNotNull(d.getDemandePieces());

// Test 1.4 — DemandePiece fourni par défaut = false
DemandePiece dp = new DemandePiece();
assertFalse(dp.getFourni());
```

---

---

# ÉTAPE 2 — DTOs

---

## 2.1 — `VisaTransformableDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/VisaTransformableDTO.java`

```java
public class VisaTransformableDTO {

    @NotBlank(message = "La référence visa est obligatoire")
    private String referenceVisa;          // (O, U)

    @NotNull(message = "La date d'entrée est obligatoire")
    private LocalDate dateEntree;          // (O)

    @NotBlank(message = "Le lieu d'entrée est obligatoire")
    private String lieuEntree;             // (O)

    @NotNull(message = "La date d'expiration est obligatoire")
    @Future(message = "La date d'expiration doit être dans le futur")
    private LocalDate dateExpiration;      // (O)

    // getters / setters
}
```

**Note** : le `passeportId` n'est pas dans ce DTO, il est résolu en service via le `DemandeCreateDTO`.

---

## 2.2 — `DemandeCreateDTO.java` ← DTO principal du formulaire complet

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeCreateDTO.java`

```java
public class DemandeCreateDTO {

    // ── Bloc 🟦 ÉTAT CIVIL (Dev1) ──────────────────────────────
    @Valid
    @NotNull
    private DemandeurDTO demandeurDTO;          // 🔗 API DEV1

    // ── Bloc 🟩 PASSEPORT (Dev1) ───────────────────────────────
    @Valid
    @NotNull
    private PasseportDTO passeportDTO;          // 🔗 API DEV1

    // ── Bloc 🟨 VISA TRANSFORMABLE ──────────────────────────────
    @Valid
    @NotNull
    private VisaTransformableDTO visaDTO;

    // ── Bloc 🟥 DEMANDE ─────────────────────────────────────────
    @NotNull(message = "Le type de visa est obligatoire")
    private Long idTypeVisa;                    // (O, S) → sélect formulaire

    // dateDemande     → forcé à now() en service  (H)
    // idTypeDemande   → forcé à NOUVELLE en service (H)
    // idStatutDemande → forcé à CREE en service (H)

    // ── Bloc 🟪 PIÈCES ───────────────────────────────────────────
    private List<Long> piecesFournies = new ArrayList<>();
    // ids des pièces dont la checkbox est cochée dans le formulaire

    // getters / setters
}
```

---

## 2.3 — `DemandeResponseDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeResponseDTO.java`

```java
public class DemandeResponseDTO {

    private Long id;
    private LocalDateTime dateDemande;
    private String nomDemandeur;
    private String prenomDemandeur;
    private String numeroPasSeport;
    private String referenceVisa;
    private String typeVisa;               // libellé ex: "Travailleur"
    private String typeDemande;            // libellé → "NOUVELLE"
    private String statutDemande;          // libellé → "CREE"
    private List<DemandePieceDTO> pieces;

    // getters / setters
}
```

---

## 2.4 — `PieceDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/PieceDTO.java`

```java
public class PieceDTO {

    private Long id;
    private String nom;
    private Boolean obligatoire;
    private String typePiece;    // "COMMUN" / "TRAVAILLEUR" / "INVESTISSEUR"

    // getters / setters
}
```

---

## 2.5 — `DemandePieceDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/DemandePieceDTO.java`

```java
public class DemandePieceDTO {

    private Long idPiece;
    private String nomPiece;
    private Boolean obligatoire;
    private Boolean fourni;

    // getters / setters
}
```

---

### ✅ TEST ÉTAPE 2 — `DtoValidationTest.java`

```java
// Test 2.1 — VisaTransformableDTO : référenceVisa blank → violation
VisaTransformableDTO dto = new VisaTransformableDTO();
dto.setReferenceVisa("");
Set<ConstraintViolation<VisaTransformableDTO>> violations = validator.validate(dto);
assertFalse(violations.isEmpty());

// Test 2.2 — VisaTransformableDTO : dateExpiration passée → violation
dto.setReferenceVisa("REF-001");
dto.setDateEntree(LocalDate.now());
dto.setLieuEntree("Tana");
dto.setDateExpiration(LocalDate.now().minusDays(1));  // passé → violation
violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateExpiration")));

// Test 2.3 — DemandeCreateDTO : idTypeVisa null → violation
DemandeCreateDTO demandeDto = new DemandeCreateDTO();
demandeDto.setIdTypeVisa(null);
Set<ConstraintViolation<DemandeCreateDTO>> viol2 = validator.validate(demandeDto);
assertTrue(viol2.stream().anyMatch(v -> v.getPropertyPath().toString().equals("idTypeVisa")));

// Test 2.4 — piecesFournies initialisée (jamais null)
DemandeCreateDTO d = new DemandeCreateDTO();
assertNotNull(d.getPiecesFournies());
```

---

---

# ÉTAPE 3 — REPOSITORIES

---

## 3.1 — `VisaTransformableRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/VisaTransformableRepository.java`

```java
public interface VisaTransformableRepository extends JpaRepository<VisaTransformable, Long> {

    Optional<VisaTransformable> findByReferenceVisa(String referenceVisa);
    // → utilisé pour vérifier l'unicité avant insertion
}
```

---

## 3.2 — `TypeVisaRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/TypeVisaRepository.java`

```java
public interface TypeVisaRepository extends JpaRepository<TypeVisa, Long> {

    Optional<TypeVisa> findByLibelle(String libelle);
    // → utile si on cherche par nom ("Travailleur", "Investisseur")
}
```

---

## 3.3 — `DemandeRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/DemandeRepository.java`

```java
public interface DemandeRepository extends JpaRepository<Demande, Long> {

    List<Demande> findByDemandeurId(Long idDemandeur);
    // → toutes les demandes d'un demandeur donné

    List<Demande> findByStatutDemandeLibelle(String libelle);
    // → filtrer par statut : "CREE", "APPROUVEE", etc.
}
```

---

## 3.4 — `HistoriqueStatutRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/HistoriqueStatutRepository.java`

```java
public interface HistoriqueStatutRepository extends JpaRepository<HistoriqueStatut, Long> {

    List<HistoriqueStatut> findByDemandeIdOrderByDateChangementDesc(Long idDemande);
    // → historique complet d'une demande, du plus récent au plus ancien
}
```

---

## 3.5 — `PieceRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/PieceRepository.java`

```java
public interface PieceRepository extends JpaRepository<Piece, Long> {

    List<Piece> findByTypePieceCode(String code);
    // → code = "COMMUN" | "TRAVAILLEUR" | "INVESTISSEUR"

    List<Piece> findByTypePieceCodeIn(List<String> codes);
    // → récupérer COMMUN + spécifique en une seule requête
    // → ex: codes = ["COMMUN", "TRAVAILLEUR"]
}
```

---

## 3.6 — `DemandePieceRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/DemandePieceRepository.java`

```java
public interface DemandePieceRepository extends JpaRepository<DemandePiece, Long> {

    List<DemandePiece> findByDemandeId(Long idDemande);
    // → toutes les pièces liées à une demande
}
```

---

### ✅ TEST ÉTAPE 3 — `RepositoryTest.java` (tests d'intégration avec H2 in-memory)

```java
// Annotations de classe
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)  // utilise H2

// Test 3.1 — VisaTransformableRepository : findByReferenceVisa retourne vide si absent
Optional<VisaTransformable> result = visaTransformableRepository.findByReferenceVisa("INCONNU");
assertFalse(result.isPresent());

// Test 3.2 — VisaTransformableRepository : save puis findByReferenceVisa OK
VisaTransformable visa = buildVisaTransformable("REF-TEST");
visaTransformableRepository.save(visa);
Optional<VisaTransformable> found = visaTransformableRepository.findByReferenceVisa("REF-TEST");
assertTrue(found.isPresent());

// Test 3.3 — PieceRepository : findByTypePieceCode retourne uniquement les pièces du type demandé
List<Piece> communs = pieceRepository.findByTypePieceCode("COMMUN");
assertTrue(communs.stream().allMatch(p -> p.getTypePiece().getCode().equals("COMMUN")));

// Test 3.4 — PieceRepository : findByTypePieceCodeIn retourne les deux types
List<Piece> mix = pieceRepository.findByTypePieceCodeIn(List.of("COMMUN", "TRAVAILLEUR"));
assertTrue(mix.stream().anyMatch(p -> p.getTypePiece().getCode().equals("COMMUN")));
assertTrue(mix.stream().anyMatch(p -> p.getTypePiece().getCode().equals("TRAVAILLEUR")));

// Test 3.5 — HistoriqueStatutRepository : findByDemandeIdOrderByDateChangementDesc trié
// → insérer 2 historiques, vérifier que le plus récent est en premier

// Test 3.6 — DemandeRepository : findByStatutDemandeLibelle("CREE") retourne les bonnes demandes
```

---

---

# ÉTAPE 4 — MAPPERS

---

## 4.1 — `VisaTransformableMapper.java`

**Fichier** : `src/main/java/com/projet/visa/mapper/VisaTransformableMapper.java`

```java
@Component
public class VisaTransformableMapper {

    /**
     * Convertit un DTO en entité, en attachant le passeport.
     * @param dto           données saisies dans le formulaire
     * @param passeport     entité Passeport résolue (via Dev1)
     * @return              entité VisaTransformable (non persistée)
     */
    public VisaTransformable toEntity(VisaTransformableDTO dto, Passeport passeport) {
        VisaTransformable entity = new VisaTransformable();
        entity.setReferenceVisa(dto.getReferenceVisa());
        entity.setDateEntree(dto.getDateEntree());
        entity.setLieuEntree(dto.getLieuEntree());
        entity.setDateExpiration(dto.getDateExpiration());
        entity.setPasseport(passeport);
        return entity;
    }

    /**
     * Convertit une entité en DTO pour affichage.
     * @param entity    entité persistée
     * @return          DTO de lecture
     */
    public VisaTransformableDTO toDTO(VisaTransformable entity) {
        VisaTransformableDTO dto = new VisaTransformableDTO();
        dto.setReferenceVisa(entity.getReferenceVisa());
        dto.setDateEntree(entity.getDateEntree());
        dto.setLieuEntree(entity.getLieuEntree());
        dto.setDateExpiration(entity.getDateExpiration());
        return dto;
    }
}
```

---

## 4.2 — `DemandeMapper.java`

**Fichier** : `src/main/java/com/projet/visa/mapper/DemandeMapper.java`

```java
@Component
public class DemandeMapper {

    /**
     * Convertit une Demande persistée en DTO de réponse complet.
     * @param demande   entité avec toutes ses relations chargées
     * @return          DemandeResponseDTO prêt pour la vue
     */
    public DemandeResponseDTO toResponseDTO(Demande demande) {
        DemandeResponseDTO dto = new DemandeResponseDTO();
        dto.setId(demande.getId());
        dto.setDateDemande(demande.getDateDemande());
        dto.setNomDemandeur(demande.getDemandeur().getNom());
        dto.setPrenomDemandeur(demande.getDemandeur().getPrenom());
        dto.setNumeroPasSeport(demande.getVisaTransformable().getPasseport().getNumero());
        dto.setReferenceVisa(demande.getVisaTransformable().getReferenceVisa());
        dto.setTypeVisa(demande.getTypeVisa().getLibelle());
        dto.setTypeDemande(demande.getTypeDemande().getLibelle());
        dto.setStatutDemande(demande.getStatutDemande().getLibelle());

        // mapping des pièces
        List<DemandePieceDTO> piecesDTO = demande.getDemandePieces().stream()
            .map(dp -> {
                DemandePieceDTO dpDto = new DemandePieceDTO();
                dpDto.setIdPiece(dp.getPiece().getId());
                dpDto.setNomPiece(dp.getPiece().getNom());
                dpDto.setObligatoire(dp.getPiece().getObligatoire());
                dpDto.setFourni(dp.getFourni());
                return dpDto;
            })
            .collect(Collectors.toList());

        dto.setPieces(piecesDTO);
        return dto;
    }

    /**
     * Convertit une Piece en PieceDTO.
     * @param piece     entité Piece
     * @return          PieceDTO
     */
    public PieceDTO toPieceDTO(Piece piece) {
        PieceDTO dto = new PieceDTO();
        dto.setId(piece.getId());
        dto.setNom(piece.getNom());
        dto.setObligatoire(piece.getObligatoire());
        dto.setTypePiece(piece.getTypePiece().getCode());
        return dto;
    }
}
```

---

### ✅ TEST ÉTAPE 4 — `MapperTest.java`

```java
// Test 4.1 — VisaTransformableMapper.toEntity() : tous les champs mappés
VisaTransformableDTO dto = buildVisaDTO("REF-MAPPER");
Passeport passeport = buildPasseport();
VisaTransformable entity = mapper.toEntity(dto, passeport);
assertEquals("REF-MAPPER", entity.getReferenceVisa());
assertEquals(passeport, entity.getPasseport());
assertNull(entity.getId());   // pas encore persistée

// Test 4.2 — DemandeMapper.toResponseDTO() : libellés extraits correctement
Demande demande = buildDemandeComplete();
DemandeResponseDTO response = demandeMapper.toResponseDTO(demande);
assertEquals("CREE", response.getStatutDemande());
assertEquals("NOUVELLE", response.getTypeDemande());
assertNotNull(response.getPieces());
```

---

---

# ÉTAPE 5 — SERVICES

---

## 5.1 — `VisaTransformableService.java`

**Fichier** : `src/main/java/com/projet/visa/service/VisaTransformableService.java`

```java
@Service
@Transactional
public class VisaTransformableService {

    private final VisaTransformableRepository visaTransformableRepository;
    private final VisaTransformableMapper visaTransformableMapper;

    // ── Constructeur avec injection ──────────────────────────────

    /**
     * Crée et persiste un nouveau VisaTransformable.
     *
     * Règles appliquées :
     *   RG-01 : referenceVisa doit être unique
     *   RG-02 : dateExpiration > dateEntree
     *   RG-03 : passeport non null
     *
     * @param dto       données du formulaire
     * @param passeport entité Passeport résolue (non null)
     * @return          entité persistée
     * @throws BusinessException si référence déjà utilisée ou dates incohérentes
     */
    public VisaTransformable creer(VisaTransformableDTO dto, Passeport passeport) {
        // 1. Vérifier unicité referenceVisa
        if (visaTransformableRepository.findByReferenceVisa(dto.getReferenceVisa()).isPresent()) {
            throw new BusinessException("La référence visa '" + dto.getReferenceVisa() + "' est déjà utilisée.");
        }
        // 2. Vérifier cohérence des dates
        if (!dto.getDateExpiration().isAfter(dto.getDateEntree())) {
            throw new BusinessException("La date d'expiration doit être postérieure à la date d'entrée.");
        }
        // 3. Mapper et sauvegarder
        VisaTransformable entity = visaTransformableMapper.toEntity(dto, passeport);
        return visaTransformableRepository.save(entity);
    }

    /**
     * Vérifie si une référence visa est déjà en base.
     *
     * @param referenceVisa     référence à tester
     * @return                  true si elle existe déjà
     */
    public boolean existeParReference(String referenceVisa) {
        return visaTransformableRepository.findByReferenceVisa(referenceVisa).isPresent();
    }
}
```

---

### ✅ TEST ÉTAPE 5.1 — `VisaTransformableServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class VisaTransformableServiceTest {

    @Mock VisaTransformableRepository repo;
    @Mock VisaTransformableMapper mapper;
    @InjectMocks VisaTransformableService service;

    // Test 5.1.1 — Création OK : référence unique, dates cohérentes
    @Test
    void creer_OK() {
        VisaTransformableDTO dto = buildDTO("REF-OK", LocalDate.now(), LocalDate.now().plusMonths(6));
        Passeport passeport = new Passeport();
        when(repo.findByReferenceVisa("REF-OK")).thenReturn(Optional.empty());
        when(mapper.toEntity(dto, passeport)).thenReturn(new VisaTransformable());
        when(repo.save(any())).thenAnswer(inv -> inv.getArgument(0));

        VisaTransformable result = service.creer(dto, passeport);
        assertNotNull(result);
        verify(repo, times(1)).save(any());
    }

    // Test 5.1.2 — RG-01 : référence déjà en base → BusinessException
    @Test
    void creer_referenceDupliquee_throwsException() {
        VisaTransformableDTO dto = buildDTO("REF-DUP", LocalDate.now(), LocalDate.now().plusMonths(6));
        when(repo.findByReferenceVisa("REF-DUP")).thenReturn(Optional.of(new VisaTransformable()));

        assertThrows(BusinessException.class, () -> service.creer(dto, new Passeport()));
        verify(repo, never()).save(any());
    }

    // Test 5.1.3 — RG-02 : dateExpiration <= dateEntree → BusinessException
    @Test
    void creer_datesIncoherentes_throwsException() {
        LocalDate entree = LocalDate.of(2025, 6, 1);
        LocalDate expiration = LocalDate.of(2025, 5, 1);  // avant l'entrée
        VisaTransformableDTO dto = buildDTO("REF-DATE", entree, expiration);
        when(repo.findByReferenceVisa(any())).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.creer(dto, new Passeport()));
    }

    // Test 5.1.4 — existeParReference : retourne true si présent
    @Test
    void existeParReference_vraiSiPresent() {
        when(repo.findByReferenceVisa("REF-X")).thenReturn(Optional.of(new VisaTransformable()));
        assertTrue(service.existeParReference("REF-X"));
    }
}
```

---

## 5.2 — `PieceService.java`

**Fichier** : `src/main/java/com/projet/visa/service/PieceService.java`

```java
@Service
public class PieceService {

    private final PieceRepository pieceRepository;
    private final DemandeMapper demandeMapper;  // pour toPieceDTO()

    // ── Constructeur avec injection ──────────────────────────────

    /**
     * Retourne toutes les pièces communes (type COMMUN).
     *
     * @return liste de PieceDTO avec obligatoire/facultatif
     */
    public List<PieceDTO> getPiecesCommunes() {
        return pieceRepository.findByTypePieceCode("COMMUN")
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne les pièces spécifiques selon le libellé du type visa.
     *
     * @param libelleTypeVisa   "Travailleur" ou "Investisseur"
     * @return                  liste de PieceDTO spécifiques
     */
    public List<PieceDTO> getPiecesParTypeVisa(String libelleTypeVisa) {
        String code = libelleTypeVisa.toUpperCase();  // "TRAVAILLEUR" ou "INVESTISSEUR"
        return pieceRepository.findByTypePieceCode(code)
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }

    /**
     * Retourne toutes les pièces : communes + spécifiques selon type visa.
     * Utilisé pour initialiser la liste complète lors du submit.
     *
     * @param libelleTypeVisa   "Travailleur" ou "Investisseur"
     * @return                  liste combinée de PieceDTO
     */
    public List<PieceDTO> getToutesPiecesPourTypeVisa(String libelleTypeVisa) {
        List<String> codes = List.of("COMMUN", libelleTypeVisa.toUpperCase());
        return pieceRepository.findByTypePieceCodeIn(codes)
                .stream()
                .map(demandeMapper::toPieceDTO)
                .collect(Collectors.toList());
    }
}
```

---

### ✅ TEST ÉTAPE 5.2 — `PieceServiceTest.java`

```java
// Test 5.2.1 — getPiecesCommunes() : retourne uniquement les pièces COMMUN
List<Piece> communs = List.of(buildPiece("Photo ID", "COMMUN", true));
when(pieceRepository.findByTypePieceCode("COMMUN")).thenReturn(communs);
List<PieceDTO> result = pieceService.getPiecesCommunes();
assertEquals(1, result.size());
assertEquals("COMMUN", result.get(0).getTypePiece());

// Test 5.2.2 — getPiecesParTypeVisa("Travailleur") : code = "TRAVAILLEUR"
when(pieceRepository.findByTypePieceCode("TRAVAILLEUR")).thenReturn(List.of(buildPiece("Contrat", "TRAVAILLEUR", true)));
List<PieceDTO> travail = pieceService.getPiecesParTypeVisa("Travailleur");
assertEquals("TRAVAILLEUR", travail.get(0).getTypePiece());

// Test 5.2.3 — getToutesPiecesPourTypeVisa("Investisseur") : appel avec codes COMMUN + INVESTISSEUR
List<String> codesAttendus = List.of("COMMUN", "INVESTISSEUR");
when(pieceRepository.findByTypePieceCodeIn(codesAttendus)).thenReturn(new ArrayList<>());
pieceService.getToutesPiecesPourTypeVisa("Investisseur");
verify(pieceRepository).findByTypePieceCodeIn(codesAttendus);
```

---

## 5.3 — `DemandeService.java` ← service principal

**Fichier** : `src/main/java/com/projet/visa/service/DemandeService.java`

```java
@Service
@Transactional
public class DemandeService {

    private final DemandeRepository demandeRepository;
    private final HistoriqueStatutRepository historiqueStatutRepository;
    private final DemandePieceRepository demandePieceRepository;
    private final TypeVisaRepository typeVisaRepository;
    private final TypeDemandeRepository typeDemandeRepository;      // TeamLead
    private final StatutDemandeRepository statutDemandeRepository;  // TeamLead
    private final PieceRepository pieceRepository;
    private final VisaTransformableService visaTransformableService;
    private final DemandeurService demandeurService;                 // 🔗 API DEV1
    private final PasseportService passeportService;                 // 🔗 API DEV1
    private final DemandeMapper demandeMapper;
    private final PieceService pieceService;

    // ── Constructeur avec injection ──────────────────────────────

    // ════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE : créerDemande()
    // ════════════════════════════════════════════════════════

    /**
     * Crée une nouvelle demande de visa complète.
     *
     * Étapes internes (dans cet ordre) :
     *   1. Créer ou récupérer le Demandeur (Dev1)
     *   2. Créer le Passeport lié (Dev1)
     *   3. Créer le VisaTransformable
     *   4. Résoudre TypeVisa par id
     *   5. Forcer TypeDemande = "NOUVELLE"
     *   6. Forcer StatutDemande = "CREE"
     *   7. Construire et sauvegarder la Demande
     *   8. Créer l'historique de statut initial
     *   9. Lier les pièces à la demande
     *  10. Retourner le DTO de réponse
     *
     * @param dto   formulaire complet soumis par l'utilisateur
     * @return      DemandeResponseDTO avec id, statut, pièces
     * @throws BusinessException si règle métier violée
     * @throws ResourceNotFoundException si TypeVisa introuvable
     */
    public DemandeResponseDTO creerDemande(DemandeCreateDTO dto) {

        // ÉTAPE 1 — Demandeur (Dev1)
        Demandeur demandeur = demandeurService.creerOuRecuperer(dto.getDemandeurDTO());

        // ÉTAPE 2 — Passeport (Dev1)
        Passeport passeport = passeportService.creer(dto.getPasseportDTO(), demandeur);

        // ÉTAPE 3 — VisaTransformable
        VisaTransformable visa = visaTransformableService.creer(dto.getVisaDTO(), passeport);

        // ÉTAPE 4 — TypeVisa
        TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + dto.getIdTypeVisa()));

        // ÉTAPE 5 — TypeDemande forcé à NOUVELLE
        TypeDemande typeDemande = typeDemandeRepository.findByLibelle("NOUVELLE")
                .orElseThrow(() -> new ResourceNotFoundException("TypeDemande NOUVELLE introuvable en base"));

        // ÉTAPE 6 — StatutDemande forcé à CREE
        StatutDemande statutCree = statutDemandeRepository.findByLibelle("CREE")
                .orElseThrow(() -> new ResourceNotFoundException("StatutDemande CREE introuvable en base"));

        // ÉTAPE 7 — Construire et sauvegarder la Demande
        Demande demande = new Demande();
        demande.setDateDemande(LocalDateTime.now());
        demande.setDemandeur(demandeur);
        demande.setVisaTransformable(visa);
        demande.setTypeVisa(typeVisa);
        demande.setTypeDemande(typeDemande);
        demande.setStatutDemande(statutCree);
        demande = demandeRepository.save(demande);

        // ÉTAPE 8 — Historique statut initial (RG-07 : obligatoire)
        creerHistoriqueStatut(demande, statutCree, "Création de la demande");

        // ÉTAPE 9 — Lier les pièces
        lierPiecesADemande(demande, dto.getPiecesFournies(), typeVisa);

        // ÉTAPE 10 — Recharger avec pièces et retourner DTO
        Demande demandeFinal = demandeRepository.findById(demande.getId()).orElseThrow();
        return demandeMapper.toResponseDTO(demandeFinal);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE PRIVÉE : creerHistoriqueStatut()
    // ════════════════════════════════════════════════════════

    /**
     * Crée une entrée dans l'historique des statuts.
     * Appelée à chaque changement de statut (y compris la création).
     *
     * @param demande       la demande concernée
     * @param statut        le nouveau statut
     * @param commentaire   motif du changement (ex: "Création de la demande")
     */
    private void creerHistoriqueStatut(Demande demande, StatutDemande statut, String commentaire) {
        HistoriqueStatut historique = new HistoriqueStatut();
        historique.setDemande(demande);
        historique.setStatutDemande(statut);
        historique.setDateChangement(LocalDateTime.now());
        historique.setCommentaire(commentaire);
        historiqueStatutRepository.save(historique);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE PRIVÉE : lierPiecesADemande()
    // ════════════════════════════════════════════════════════

    /**
     * Lie les pièces (communes + spécifiques) à une demande.
     * Vérifie que toutes les pièces obligatoires sont fournies.
     *
     * Règles :
     *   RG-08 : lier COMMUN + spécifiques selon typeVisa
     *   RG-09 : pièce obligatoire non fournie → BusinessException
     *
     * @param demande           la demande à lier
     * @param piecesFournies    ids des pièces cochées dans le formulaire
     * @param typeVisa          type de visa sélectionné
     * @throws BusinessException si pièce obligatoire manquante
     */
    private void lierPiecesADemande(Demande demande, List<Long> piecesFournies, TypeVisa typeVisa) {
        List<String> codes = List.of("COMMUN", typeVisa.getLibelle().toUpperCase());
        List<Piece> toutes = pieceRepository.findByTypePieceCodeIn(codes);

        for (Piece piece : toutes) {
            boolean fourni = piecesFournies != null && piecesFournies.contains(piece.getId());
            // RG-09 : contrôle pièce obligatoire
            if (Boolean.TRUE.equals(piece.getObligatoire()) && !fourni) {
                throw new BusinessException("Pièce obligatoire non fournie : " + piece.getNom());
            }
            DemandePiece dp = new DemandePiece();
            dp.setDemande(demande);
            dp.setPiece(piece);
            dp.setFourni(fourni);
            demandePieceRepository.save(dp);
        }
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE : getDemande()
    // ════════════════════════════════════════════════════════

    /**
     * Récupère une demande par son identifiant.
     *
     * @param id    identifiant de la demande
     * @return      DemandeResponseDTO
     * @throws ResourceNotFoundException si demande absente
     */
    public DemandeResponseDTO getDemande(Long id) {
        Demande demande = demandeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));
        return demandeMapper.toResponseDTO(demande);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE : getPiecesFormulaire()
    // ════════════════════════════════════════════════════════

    /**
     * Retourne les pièces à afficher pour un type visa donné (AJAX).
     * Retourne uniquement les pièces SPÉCIFIQUES (pas les communes,
     * qui sont déjà affichées statiquement dans le formulaire).
     *
     * @param idTypeVisa    id du type visa sélectionné dans le select
     * @return              liste de PieceDTO spécifiques
     */
    public List<PieceDTO> getPiecesFormulaire(Long idTypeVisa) {
        TypeVisa typeVisa = typeVisaRepository.findById(idTypeVisa)
                .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + idTypeVisa));
        return pieceService.getPiecesParTypeVisa(typeVisa.getLibelle());
    }
}
```

---

### ✅ TEST ÉTAPE 5.3 — `DemandeServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DemandeServiceTest {

    // mocks de tous les repositories et services injectés
    @Mock DemandeRepository demandeRepository;
    @Mock HistoriqueStatutRepository historiqueStatutRepository;
    @Mock DemandePieceRepository demandePieceRepository;
    @Mock TypeVisaRepository typeVisaRepository;
    @Mock TypeDemandeRepository typeDemandeRepository;
    @Mock StatutDemandeRepository statutDemandeRepository;
    @Mock PieceRepository pieceRepository;
    @Mock VisaTransformableService visaTransformableService;
    @Mock DemandeurService demandeurService;         // 🔗 DEV1
    @Mock PasseportService passeportService;         // 🔗 DEV1
    @Mock DemandeMapper demandeMapper;
    @Mock PieceService pieceService;
    @InjectMocks DemandeService demandeService;

    // ── Test 5.3.1 — Création complète OK ───────────────────────
    @Test
    void creerDemande_OK() {
        DemandeCreateDTO dto = buildDemandeCreateDTO();
        stubTousLesMocks(dto);

        DemandeResponseDTO result = demandeService.creerDemande(dto);

        assertNotNull(result);
        verify(demandeRepository, times(1)).save(any(Demande.class));
        verify(historiqueStatutRepository, times(1)).save(any(HistoriqueStatut.class));
    }

    // ── Test 5.3.2 — statutDemande = CREE après création ────────
    @Test
    void creerDemande_statutInitial_CREE() {
        // capturer l'entité sauvegardée
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);
        stubTousLesMocks(buildDemandeCreateDTO());

        demandeService.creerDemande(buildDemandeCreateDTO());

        verify(demandeRepository).save(captor.capture());
        assertEquals("CREE", captor.getValue().getStatutDemande().getLibelle());
    }

    // ── Test 5.3.3 — typeDemande = NOUVELLE après création ──────
    @Test
    void creerDemande_typeDemande_NOUVELLE() {
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);
        stubTousLesMocks(buildDemandeCreateDTO());

        demandeService.creerDemande(buildDemandeCreateDTO());

        verify(demandeRepository).save(captor.capture());
        assertEquals("NOUVELLE", captor.getValue().getTypeDemande().getLibelle());
    }

    // ── Test 5.3.4 — historique créé après création ─────────────
    @Test
    void creerDemande_historiqueCreee() {
        stubTousLesMocks(buildDemandeCreateDTO());

        demandeService.creerDemande(buildDemandeCreateDTO());

        ArgumentCaptor<HistoriqueStatut> captor = ArgumentCaptor.forClass(HistoriqueStatut.class);
        verify(historiqueStatutRepository).save(captor.capture());
        assertNotNull(captor.getValue().getDateChangement());
        assertEquals("CREE", captor.getValue().getStatutDemande().getLibelle());
    }

    // ── Test 5.3.5 — pièce obligatoire non fournie → exception ──
    @Test
    void creerDemande_pieceObligatoireManquante_throwsException() {
        DemandeCreateDTO dto = buildDemandeCreateDTO();
        dto.setPiecesFournies(Collections.emptyList());  // aucune pièce cochée

        Piece pieceOblig = buildPiece("Photo ID", "COMMUN", true);  // obligatoire
        when(pieceRepository.findByTypePieceCodeIn(any())).thenReturn(List.of(pieceOblig));
        stubMocksPartiels(dto);

        assertThrows(BusinessException.class, () -> demandeService.creerDemande(dto));
        verify(demandePieceRepository, never()).save(any());
    }

    // ── Test 5.3.6 — TypeVisa introuvable → ResourceNotFoundException
    @Test
    void creerDemande_typeVisaInconnu_throwsException() {
        DemandeCreateDTO dto = buildDemandeCreateDTO();
        when(typeVisaRepository.findById(dto.getIdTypeVisa())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> demandeService.creerDemande(dto));
    }

    // ── Test 5.3.7 — getDemande introuvable → exception ─────────
    @Test
    void getDemande_introuvable_throwsException() {
        when(demandeRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> demandeService.getDemande(999L));
    }

    // ── Test 5.3.8 — pièces liées = COMMUN + spécifiques ────────
    @Test
    void lierPieces_associeCommuns_etSpecifiques() {
        // TypeVisa = Travailleur
        // On vérifie que findByTypePieceCodeIn est appelé avec ["COMMUN", "TRAVAILLEUR"]
        DemandeCreateDTO dto = buildDemandeCreateDTO();  // idTypeVisa → Travailleur
        stubTousLesMocks(dto);

        demandeService.creerDemande(dto);

        ArgumentCaptor<List<String>> codesCaptor = ArgumentCaptor.forClass(List.class);
        verify(pieceRepository).findByTypePieceCodeIn(codesCaptor.capture());
        assertTrue(codesCaptor.getValue().contains("COMMUN"));
        assertTrue(codesCaptor.getValue().contains("TRAVAILLEUR"));
    }
}
```

---

---

# ÉTAPE 6 — CONTROLLER

---

## 6.1 — `DemandeController.java`

**Fichier** : `src/main/java/com/projet/visa/controller/DemandeController.java`

```java
@Controller
@RequestMapping("/demandes")
public class DemandeController {

    private final DemandeService demandeService;
    private final TypeVisaRepository typeVisaRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;  // 🔗 DEV1
    private final NationaliteRepository nationaliteRepository;                // 🔗 DEV1
    private final PieceService pieceService;

    // ── Constructeur avec injection ──────────────────────────────

    // ════════════════════════════════════════════════════════
    //  GET /demandes/nouvelle
    //  Afficher le formulaire vierge
    // ════════════════════════════════════════════════════════
    @GetMapping("/nouvelle")
    public String afficherFormulaire(Model model) {
        model.addAttribute("demandeForm", new DemandeCreateDTO());
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());  // 🔗 DEV1
        model.addAttribute("nationalites", nationaliteRepository.findAll());                // 🔗 DEV1
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "demande/formulaire";
    }

    // ════════════════════════════════════════════════════════
    //  GET /demandes/pieces?idTypeVisa=1
    //  AJAX : retourner les pièces spécifiques en JSON
    // ════════════════════════════════════════════════════════
    @GetMapping("/pieces")
    @ResponseBody
    public ResponseEntity<List<PieceDTO>> getPiecesParTypeVisa(@RequestParam Long idTypeVisa) {
        List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);
        return ResponseEntity.ok(pieces);
    }

    // ════════════════════════════════════════════════════════
    //  POST /demandes/nouvelle
    //  Soumettre et enregistrer la demande
    // ════════════════════════════════════════════════════════
    @PostMapping("/nouvelle")
    public String soumettreFormulaire(
            @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {

        // Si erreurs de validation Bean Validation (@NotBlank, @NotNull, etc.)
        if (result.hasErrors()) {
            // Recharger les listes du formulaire
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            return "demande/formulaire";
        }

        try {
            DemandeResponseDTO response = demandeService.creerDemande(dto);
            redirectAttributes.addFlashAttribute("successMessage", "Demande créée avec succès. Référence : #" + response.getId());
            return "redirect:/demandes/" + response.getId() + "/confirmation";

        } catch (BusinessException e) {
            // Erreur métier (pièce manquante, référence dupliquée, etc.)
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("typesVisa", typeVisaRepository.findAll());
            model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
            model.addAttribute("nationalites", nationaliteRepository.findAll());
            model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
            return "demande/formulaire";
        }
    }

    // ════════════════════════════════════════════════════════
    //  GET /demandes/{id}/confirmation
    //  Page de confirmation post-création
    // ════════════════════════════════════════════════════════
    @GetMapping("/{id}/confirmation")
    public String afficherConfirmation(@PathVariable Long id, Model model) {
        DemandeResponseDTO demande = demandeService.getDemande(id);
        model.addAttribute("demande", demande);
        return "demande/confirmation";
    }
}
```

---

### ✅ TEST ÉTAPE 6 — `DemandeControllerTest.java`

```java
@WebMvcTest(DemandeController.class)
class DemandeControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean DemandeService demandeService;
    @MockBean TypeVisaRepository typeVisaRepository;
    @MockBean SituationFamilialeRepository situationFamilialeRepository;
    @MockBean NationaliteRepository nationaliteRepository;
    @MockBean PieceService pieceService;

    // Test 6.1 — GET /demandes/nouvelle → HTTP 200, vue "demande/formulaire"
    @Test
    void afficherFormulaire_renvoie200() throws Exception {
        when(typeVisaRepository.findAll()).thenReturn(List.of());
        when(pieceService.getPiecesCommunes()).thenReturn(List.of());
        mockMvc.perform(get("/demandes/nouvelle"))
               .andExpect(status().isOk())
               .andExpect(view().name("demande/formulaire"))
               .andExpect(model().attributeExists("demandeForm"))
               .andExpect(model().attributeExists("typesVisa"))
               .andExpect(model().attributeExists("piecesCommunes"));
    }

    // Test 6.2 — GET /demandes/pieces?idTypeVisa=1 → HTTP 200, JSON
    @Test
    void getPiecesParTypeVisa_renvoieJSON() throws Exception {
        when(demandeService.getPiecesFormulaire(1L)).thenReturn(List.of(new PieceDTO()));
        mockMvc.perform(get("/demandes/pieces").param("idTypeVisa", "1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Test 6.3 — POST /demandes/nouvelle avec données valides → redirection confirmation
    @Test
    void soumettreFormulaire_OK_redirige() throws Exception {
        DemandeResponseDTO resp = new DemandeResponseDTO();
        resp.setId(42L);
        when(demandeService.creerDemande(any())).thenReturn(resp);
        mockMvc.perform(post("/demandes/nouvelle").with(csrf())
                   /* paramètres du formulaire ... */)
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/demandes/42/confirmation"));
    }

    // Test 6.4 — POST avec BindingResult en erreur → retour formulaire sans redirection
    @Test
    void soumettreFormulaire_erreurValidation_resteSurFormulaire() throws Exception {
        mockMvc.perform(post("/demandes/nouvelle").with(csrf())
                   /* champs obligatoires manquants */)
               .andExpect(status().isOk())
               .andExpect(view().name("demande/formulaire"));
    }

    // Test 6.5 — POST BusinessException → retour formulaire avec errorMessage
    @Test
    void soumettreFormulaire_businessException_afficherErreur() throws Exception {
        when(demandeService.creerDemande(any()))
            .thenThrow(new BusinessException("Référence déjà utilisée"));
        mockMvc.perform(post("/demandes/nouvelle").with(csrf()) /* params valides */)
               .andExpect(status().isOk())
               .andExpect(view().name("demande/formulaire"))
               .andExpect(model().attributeExists("errorMessage"));
    }

    // Test 6.6 — GET /demandes/{id}/confirmation → HTTP 200, modèle "demande"
    @Test
    void afficherConfirmation_renvoie200() throws Exception {
        when(demandeService.getDemande(1L)).thenReturn(new DemandeResponseDTO());
        mockMvc.perform(get("/demandes/1/confirmation"))
               .andExpect(status().isOk())
               .andExpect(view().name("demande/confirmation"))
               .andExpect(model().attributeExists("demande"));
    }
}
```

---

---

# ÉTAPE 7 — TEMPLATES THYMELEAF

---

## 7.1 — `formulaire.html`

**Fichier** : `src/main/resources/templates/demande/formulaire.html`

### Structure de la page

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Demande de Visa Transformable</title>
    <!-- Bootstrap ou CSS maison -->
</head>
<body>

<h1>DEMANDE DE VISA TRANSFORMABLE</h1>

<!-- Message d'erreur global (BusinessException) -->
<div th:if="${errorMessage}" class="alert alert-danger">
    <span th:text="${errorMessage}"></span>
</div>

<form th:action="@{/demandes/nouvelle}" th:object="${demandeForm}" method="post">

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟦 ÉTAT CIVIL (Dev1)                   -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="etat-civil">
        <h2>État Civil</h2>

        <label>Nom (O)</label>
        <input type="text" th:field="*{demandeurDTO.nom}" required />
        <span th:errors="*{demandeurDTO.nom}"></span>

        <label>Prénom (F)</label>
        <input type="text" th:field="*{demandeurDTO.prenom}" />

        <label>Nom jeune fille (F)</label>
        <input type="text" th:field="*{demandeurDTO.nomJeuneFille}" />

        <label>Date de naissance (O)</label>
        <input type="date" th:field="*{demandeurDTO.dateNaissance}" required />

        <label>Lieu de naissance (F)</label>
        <input type="text" th:field="*{demandeurDTO.lieuNaissance}" />

        <label>Situation familiale (O)</label>
        <select th:field="*{demandeurDTO.idSituationFamiliale}" required>
            <option value="">-- Sélectionner --</option>
            <option th:each="sf : ${situationsFamiliales}"
                    th:value="${sf.id}"
                    th:text="${sf.libelle}">
            </option>
        </select>

        <label>Nationalité (O)</label>
        <select th:field="*{demandeurDTO.idNationalite}" required>
            <option value="">-- Sélectionner --</option>
            <option th:each="nat : ${nationalites}"
                    th:value="${nat.id}"
                    th:text="${nat.libelle}">
            </option>
        </select>

        <label>Adresse Madagascar (O)</label>
        <textarea th:field="*{demandeurDTO.adresseMadagascar}" required></textarea>

        <label>Téléphone (O)</label>
        <input type="tel" th:field="*{demandeurDTO.telephone}" required />

        <label>Email (F)</label>
        <input type="email" th:field="*{demandeurDTO.email}" />
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟩 PASSEPORT (Dev1)                    -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="passeport">
        <h2>Passeport</h2>

        <label>Numéro (O, U)</label>
        <input type="text" th:field="*{passeportDTO.numero}" required />
        <span th:errors="*{passeportDTO.numero}"></span>

        <label>Date de délivrance (O)</label>
        <input type="date" th:field="*{passeportDTO.dateDelivrance}" required />

        <label>Date d'expiration (O)</label>
        <input type="date" th:field="*{passeportDTO.dateExpiration}" required />
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟨 VISA TRANSFORMABLE (Dev2)           -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="visa-transformable">
        <h2>Visa Transformable</h2>

        <label>Référence Visa (O, U)</label>
        <input type="text" th:field="*{visaDTO.referenceVisa}" required />
        <span th:errors="*{visaDTO.referenceVisa}"></span>

        <label>Date d'entrée (O)</label>
        <input type="date" th:field="*{visaDTO.dateEntree}" required />

        <label>Lieu d'entrée (O)</label>
        <input type="text" th:field="*{visaDTO.lieuEntree}" required />

        <label>Date d'expiration visa (O)</label>
        <input type="date" th:field="*{visaDTO.dateExpiration}" required />
        <span th:errors="*{visaDTO.dateExpiration}"></span>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟥 DEMANDE (Dev2)                      -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="demande">
        <h2>Demande</h2>

        <label>Date de demande (O)</label>
        <input type="date" th:field="*{dateDemande}"
               th:value="${#dates.format(#dates.createNow(), 'yyyy-MM-dd')}"
               readonly />

        <label>Type de visa (O)</label>
        <select id="selectTypeVisa" th:field="*{idTypeVisa}" required>
            <option value="">-- Sélectionner --</option>
            <option th:each="tv : ${typesVisa}"
                    th:value="${tv.id}"
                    th:text="${tv.libelle}">
            </option>
        </select>
        <span th:errors="*{idTypeVisa}"></span>

        <!-- HIDDEN : forcés en service, pas modifiables -->
        <!-- typeDemande et statutDemande ne sont PAS des champs du formulaire -->
        <!-- Ils sont résolus côté service -->
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟪 PIÈCES À FOURNIR (Dev2)             -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="pieces">
        <h2>Pièces à fournir</h2>

        <!-- Pièces communes : affichées statiquement -->
        <h3>Pièces communes</h3>
        <div th:each="piece : ${piecesCommunes}">
            <input type="checkbox"
                   name="piecesFournies"
                   th:value="${piece.id}"
                   th:id="'piece_' + ${piece.id}" />
            <label th:for="'piece_' + ${piece.id}">
                <span th:text="${piece.nom}"></span>
                <span th:if="${piece.obligatoire}" class="badge-obligatoire"> *</span>
            </label>
        </div>

        <!-- Pièces spécifiques : chargées dynamiquement par AJAX -->
        <h3>Pièces spécifiques</h3>
        <div id="pieces-specifiques">
            <!-- Contenu injecté par JavaScript -->
        </div>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BOUTONS                                     -->
    <!-- ═══════════════════════════════════════════ -->
    <div class="form-actions">
        <button type="submit">ENREGISTRER</button>
        <a th:href="@{/}">ANNULER</a>
    </div>

</form>

<!-- ═══════════════════════════════════════════════ -->
<!-- JAVASCRIPT : chargement dynamique des pièces   -->
<!-- ═══════════════════════════════════════════════ -->
<script th:inline="javascript">
    document.getElementById('selectTypeVisa').addEventListener('change', function () {
        const idTypeVisa = this.value;
        const container = document.getElementById('pieces-specifiques');
        container.innerHTML = '';  // vider le bloc précédent

        if (!idTypeVisa) return;

        fetch('/demandes/pieces?idTypeVisa=' + idTypeVisa)
            .then(response => response.json())
            .then(pieces => {
                pieces.forEach(piece => {
                    const div = document.createElement('div');
                    const checkbox = document.createElement('input');
                    checkbox.type = 'checkbox';
                    checkbox.name = 'piecesFournies';
                    checkbox.value = piece.id;
                    checkbox.id = 'spec_piece_' + piece.id;

                    const label = document.createElement('label');
                    label.htmlFor = 'spec_piece_' + piece.id;
                    label.textContent = piece.nom + (piece.obligatoire ? ' *' : '');

                    div.appendChild(checkbox);
                    div.appendChild(label);
                    container.appendChild(div);
                });
            })
            .catch(err => console.error('Erreur chargement pièces:', err));
    });
</script>

</body>
</html>
```

---

## 7.2 — `confirmation.html`

**Fichier** : `src/main/resources/templates/demande/confirmation.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Confirmation de Demande</title>
</head>
<body>

<h1>Demande enregistrée avec succès</h1>

<!-- Message flash -->
<div th:if="${successMessage}" class="alert alert-success">
    <span th:text="${successMessage}"></span>
</div>

<section id="recap-demande">
    <h2>Récapitulatif</h2>
    <table>
        <tr><th>Numéro de demande</th>  <td th:text="${demande.id}"></td></tr>
        <tr><th>Date de demande</th>    <td th:text="${demande.dateDemande}"></td></tr>
        <tr><th>Demandeur</th>          <td th:text="${demande.nomDemandeur + ' ' + demande.prenomDemandeur}"></td></tr>
        <tr><th>Référence Visa</th>     <td th:text="${demande.referenceVisa}"></td></tr>
        <tr><th>Type de Visa</th>       <td th:text="${demande.typeVisa}"></td></tr>
        <tr><th>Type de Demande</th>    <td th:text="${demande.typeDemande}"></td></tr>
        <tr>
            <th>Statut</th>
            <td>
                <span class="badge badge-cree" th:text="${demande.statutDemande}"></span>
            </td>
        </tr>
    </table>
</section>

<section id="recap-pieces">
    <h2>Pièces associées</h2>
    <table>
        <thead>
            <tr>
                <th>Pièce</th>
                <th>Obligatoire</th>
                <th>Fournie</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="piece : ${demande.pieces}">
                <td th:text="${piece.nomPiece}"></td>
                <td th:text="${piece.obligatoire ? 'Oui' : 'Non'}"></td>
                <td>
                    <span th:if="${piece.fourni}" class="badge-ok">✔</span>
                    <span th:unless="${piece.fourni}" class="badge-nok">✗</span>
                </td>
            </tr>
        </tbody>
    </table>
</section>

<a th:href="@{/}">Retour à l'accueil</a>

</body>
</html>
```

---

---

# ÉTAPE 8 — GESTION DES EXCEPTIONS

Ces classes sont normalement créées par le TeamLead. Vérifier leur existence, sinon les créer.

## 8.1 — `BusinessException.java`

**Fichier** : `src/main/java/com/projet/visa/exception/BusinessException.java`

```java
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }
}
```

## 8.2 — `ResourceNotFoundException.java`

**Fichier** : `src/main/java/com/projet/visa/exception/ResourceNotFoundException.java`

```java
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
```

---

---

# RÉCAPITULATIF DES RÈGLES DE GESTION

| Code | Règle | Implémentée dans |
|------|-------|-----------------|
| RG-01 | `referenceVisa` doit être unique en base | `VisaTransformableService.creer()` |
| RG-02 | `dateExpiration` du visa doit être > `dateEntree` | `VisaTransformableService.creer()` |
| RG-03 | Le `Passeport` est obligatoire pour créer un VisaTransformable | `VisaTransformable` entity + service |
| RG-04 | `dateDemande` = `LocalDateTime.now()` — jamais saisi | `DemandeService.creerDemande()` |
| RG-05 | `typeDemande` = `"NOUVELLE"` — forcé en service | `DemandeService.creerDemande()` |
| RG-06 | `statutDemande` = `"CREE"` — forcé en service | `DemandeService.creerDemande()` |
| RG-07 | Toute création/modification de statut → 1 ligne dans `historique_statut` | `DemandeService.creerHistoriqueStatut()` |
| RG-08 | Les pièces liées = COMMUN + spécifiques selon le type visa | `DemandeService.lierPiecesADemande()` |
| RG-09 | Pièce `obligatoire=true` non fournie → `BusinessException` | `DemandeService.lierPiecesADemande()` |
| RG-10 | Sélection type visa → rechargement dynamique des pièces spécifiques | JS AJAX formulaire + `GET /demandes/pieces` |

---

---

# RÉCAPITULATIF DE TOUS LES TESTS

| # | Test | Classe | Type |
|---|------|--------|------|
| T-01 | Entités instanciables, DemandePiece.fourni = false par défaut | `EntityInstantiationTest` | Unitaire |
| T-02 | VisaTransformableDTO : validations @NotBlank, @Future | `DtoValidationTest` | Unitaire |
| T-03 | DemandeCreateDTO : idTypeVisa null → violation | `DtoValidationTest` | Unitaire |
| T-04 | piecesFournies initialisée (jamais null) | `DtoValidationTest` | Unitaire |
| T-05 | findByReferenceVisa : vide si absent | `RepositoryTest` | Intégration H2 |
| T-06 | findByReferenceVisa : présent après save | `RepositoryTest` | Intégration H2 |
| T-07 | findByTypePieceCode : retourne uniquement le bon type | `RepositoryTest` | Intégration H2 |
| T-08 | findByTypePieceCodeIn : retourne les deux types | `RepositoryTest` | Intégration H2 |
| T-09 | historique trié du plus récent au plus ancien | `RepositoryTest` | Intégration H2 |
| T-10 | Mapper toEntity : tous les champs mappés, id = null | `MapperTest` | Unitaire |
| T-11 | Mapper toResponseDTO : libellés extraits, pièces mappées | `MapperTest` | Unitaire |
| T-12 | VisaTransformableService : création OK | `VisaTransformableServiceTest` | Unitaire |
| T-13 | RG-01 : référence dupliquée → BusinessException | `VisaTransformableServiceTest` | Unitaire |
| T-14 | RG-02 : dates incohérentes → BusinessException | `VisaTransformableServiceTest` | Unitaire |
| T-15 | existeParReference : true si présent | `VisaTransformableServiceTest` | Unitaire |
| T-16 | PieceService : getPiecesCommunes retourne COMMUN | `PieceServiceTest` | Unitaire |
| T-17 | PieceService : getPiecesParTypeVisa("Travailleur") → code TRAVAILLEUR | `PieceServiceTest` | Unitaire |
| T-18 | PieceService : getToutesPieces appelle avec [COMMUN, INVESTISSEUR] | `PieceServiceTest` | Unitaire |
| T-19 | DemandeService : création complète OK | `DemandeServiceTest` | Unitaire |
| T-20 | RG-06 : statutDemande = CREE après création | `DemandeServiceTest` | Unitaire |
| T-21 | RG-05 : typeDemande = NOUVELLE après création | `DemandeServiceTest` | Unitaire |
| T-22 | RG-07 : historique créé après création | `DemandeServiceTest` | Unitaire |
| T-23 | RG-09 : pièce obligatoire manquante → BusinessException | `DemandeServiceTest` | Unitaire |
| T-24 | TypeVisa introuvable → ResourceNotFoundException | `DemandeServiceTest` | Unitaire |
| T-25 | getDemande introuvable → ResourceNotFoundException | `DemandeServiceTest` | Unitaire |
| T-26 | RG-08 : pièces liées = COMMUN + TRAVAILLEUR | `DemandeServiceTest` | Unitaire |
| T-27 | GET /demandes/nouvelle → 200, modèle complet | `DemandeControllerTest` | MVC |
| T-28 | GET /demandes/pieces → 200, JSON | `DemandeControllerTest` | MVC |
| T-29 | POST valide → redirection /confirmation | `DemandeControllerTest` | MVC |
| T-30 | POST erreur validation → retour formulaire | `DemandeControllerTest` | MVC |
| T-31 | POST BusinessException → formulaire avec errorMessage | `DemandeControllerTest` | MVC |
| T-32 | GET /demandes/{id}/confirmation → 200, modèle "demande" | `DemandeControllerTest` | MVC |

---

---

# ÉTAPE 9 — MODIFICATION D'UNE DEMANDE EXISTANTE

> **Objectif** : permettre de modifier une demande créée et tous ses champs associés (demandeur, passeport, visa transformable, pièces)
>
> **Contexte** : à partir d'une demande existante (par son ID), on peut éditer :
> - Les données du demandeur (sauf nom + dateNaissance + nationalité pour éviter les doublons)
> - Le passeport (numéro, dates)
> - Le visa transformable (référence, dates, lieu)
> - Les pièces fournies (checkbox)
>
> ⚠️ **RG-11** : la modification reconstruit les relations `demande_piece` pour refléter les nouvelles sélections
> ⚠️ **RG-12** : `dateDemande`, `typeDemande` et `idDemandeur` ne sont **pas modifiables** (immutables)
> ⚠️ **RG-13** : si le statut change, un nouvel historique est créé (RG-07)
> ⚠️ **RG-14** : les validations existantes s'appliquent aussi lors de la modification

---

## 9.1 — FICHIERS À CRÉER / MODIFIER

### Fichiers à CRÉER

```
src/main/java/com/visa/backoffice/
├── dto/
│   └── DemandeUpdateDTO.java              ← formulaire de modification
├── mapper/
│   └── (extension DemandeMapper existant)
├── service/
│   └── (extension DemandeService existant)
└── controller/
    └── (extension DemandeController existant)

src/main/resources/templates/demande/
├── editer.html                            ← formulaire de modification
└── editer-confirmation.html               ← confirmation après modification
```

### Fichiers à MODIFIER

- `DemandeService.java` : ajouter `modifierDemande()`, `getDemandePourEdition()`
- `DemandeMapper.java` : ajouter mapping pour edition
- `DemandeController.java` : ajouter endpoints GET/POST pour édition
- `DemandeRepository.java` : aucune modification (méthodes existantes suffisent)
- `DemandePieceRepository.java` : ajouter `deleteByDemandeId()` si n'existe pas

---

## 9.2 — DTOs

### 9.2.1 — `DemandeUpdateDTO.java`

**Fichier** : `src/main/java/com/visa/backoffice/dto/DemandeUpdateDTO.java`

```java
public class DemandeUpdateDTO {

    // ── Bloc DEMANDEUR (partiellement modifiable) ─────────────────
    @Valid
    @NotNull
    private DemandeurUpdateDTO demandeurDTO;  // nom/dateNaissance/nationalité IMMUTABLES

    // ── Bloc PASSEPORT ─────────────────────────────────────────
    @Valid
    @NotNull
    private PasseportDTO passeportDTO;

    // ── Bloc VISA TRANSFORMABLE ───────────────────────────────
    @Valid
    @NotNull
    private VisaTransformableDTO visaDTO;

    // ── Bloc DEMANDE ───────────────────────────────────────────
    @NotNull(message = "Le type de visa est obligatoire")
    private Long idTypeVisa;                 // modifiable

    // ── Bloc PIÈCES ───────────────────────────────────────────
    private List<Long> piecesFournies = new ArrayList<>();

    // getters / setters
}
```

### 9.2.2 — `DemandeurUpdateDTO.java` (sous-ensemble modifiable)

**Fichier** : `src/main/java/com/visa/backoffice/dto/DemandeurUpdateDTO.java`

> ⚠️ Contrairement à `DemandeurDTO`, les champs suivants sont **IMMUTABLES** et ne doivent pas être présents :
> - `nom`
> - `dateNaissance`
> - `idNationalite`

```java
public class DemandeurUpdateDTO {

    // ── Champs IMMUTABLES (affichage seul, pas de modification) ──────
    private String nomImmutable;              // affichage
    private LocalDate dateNaissanceImmutable; // affichage
    private String nationaliteImmutable;      // affichage

    // ── Champs modifiables ─────────────────────────────────────────

    private String prenom;                               // (F)

    private String nomJeuneFille;                        // (F)

    private String lieuNaissance;                        // (F)

    @NotNull(message = "La situation familiale est obligatoire")
    private Long idSituationFamiliale;                   // (O, S) modifiable

    @NotBlank(message = "L'adresse Madagascar est obligatoire")
    private String adresseMadagascar;                    // (O)

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(
        regexp = "^[0-9+\\s\\-]{7,15}$",
        message = "Format téléphone invalide (7 à 15 chiffres)"
    )
    private String telephone;                            // (O)

    @Email(message = "Format email invalide")
    private String email;                                // (F)

    // getters / setters
}
```

---

## 9.3 — MAPPER

### Extension de `DemandeMapper.java`

**Ajout dans** : `src/main/java/com/visa/backoffice/mapper/DemandeMapper.java`

```java
/**
 * Convertit une entité Demande en DTO d'édition.
 * Inclut les données actuelles de la demande et de toutes ses relations.
 *
 * @param demande   entité persistée avec toutes les relations
 * @return          DemandeUpdateDTO avec données actuelles
 */
public DemandeUpdateDTO toUpdateDTO(Demande demande) {
    DemandeUpdateDTO dto = new DemandeUpdateDTO();

    // Demandeur (partiellement)
    DemandeurUpdateDTO demandeurUpdateDTO = new DemandeurUpdateDTO();
    demandeurUpdateDTO.setNomImmutable(demande.getDemandeur().getNom());
    demandeurUpdateDTO.setDateNaissanceImmutable(demande.getDemandeur().getDateNaissance());
    demandeurUpdateDTO.setNationaliteImmutable(demande.getDemandeur().getNationalite().getLibelle());
    demandeurUpdateDTO.setPrenom(demande.getDemandeur().getPrenom());
    demandeurUpdateDTO.setNomJeuneFille(demande.getDemandeur().getNomJeuneFille());
    demandeurUpdateDTO.setLieuNaissance(demande.getDemandeur().getLieuNaissance());
    demandeurUpdateDTO.setAdresseMadagascar(demande.getDemandeur().getAdresseMadagascar());
    demandeurUpdateDTO.setTelephone(demande.getDemandeur().getTelephone());
    demandeurUpdateDTO.setEmail(demande.getDemandeur().getEmail());
    if (demande.getDemandeur().getSituationFamiliale() != null) {
        demandeurUpdateDTO.setIdSituationFamiliale(demande.getDemandeur().getSituationFamiliale().getId());
    }
    dto.setDemandeurDTO(demandeurUpdateDTO);

    // Passeport
    PasseportDTO passeportDTO = new PasseportDTO();
    passeportDTO.setNumero(demande.getVisaTransformable().getPasseport().getNumero());
    passeportDTO.setDateDelivrance(demande.getVisaTransformable().getPasseport().getDateDelivrance());
    passeportDTO.setDateExpiration(demande.getVisaTransformable().getPasseport().getDateExpiration());
    dto.setPasseportDTO(passeportDTO);

    // VisaTransformable
    VisaTransformableDTO visaDTO = new VisaTransformableDTO();
    visaDTO.setReferenceVisa(demande.getVisaTransformable().getReferenceVisa());
    visaDTO.setDateEntree(demande.getVisaTransformable().getDateEntree());
    visaDTO.setLieuEntree(demande.getVisaTransformable().getLieuEntree());
    visaDTO.setDateExpiration(demande.getVisaTransformable().getDateExpiration());
    dto.setVisaDTO(visaDTO);

    // Type Visa
    dto.setIdTypeVisa(demande.getTypeVisa().getId());

    // Pièces fournies
    List<Long> piecesFournies = demande.getDemandePieces().stream()
        .filter(DemandePiece::getFourni)
        .map(dp -> dp.getPiece().getId())
        .collect(Collectors.toList());
    dto.setPiecesFournies(piecesFournies);

    return dto;
}
```

---

## 9.4 — SERVICES

### Extension de `DemandeService.java`

**Ajout dans** : `src/main/java/com/visa/backoffice/service/DemandeService.java`

```java
// ════════════════════════════════════════════════════════
//  MÉTHODE : getDemandePourEdition()
// ════════════════════════════════════════════════════════

/**
 * Récupère une demande au format édition (DTO pour le formulaire de modification).
 *
 * @param id    identifiant de la demande
 * @return      DemandeUpdateDTO prêt pour être modifié
 * @throws ResourceNotFoundException si demande absente
 */
public DemandeUpdateDTO getDemandePourEdition(Long id) {
    Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));
    return demandeMapper.toUpdateDTO(demande);
}

// ════════════════════════════════════════════════════════
//  MÉTHODE PRINCIPALE : modifierDemande()
// ════════════════════════════════════════════════════════

/**
 * Modifie une demande existante et toutes ses relations.
 *
 * Étapes internes (dans cet ordre) :
 *   1. Récupérer la demande existante
 *   2. Vérifier que les champs immutables ne sont pas modifiés
 *   3. Mettre à jour le demandeur (champs modifiables seulement)
 *   4. Mettre à jour le passeport
 *   5. Mettre à jour le VisaTransformable
 *   6. Mettre à jour le TypeVisa
 *   7. Recréer les liens DemandePiece (supprimer + ajouter)
 *   8. Créer un historique statut si le statut change
 *   9. Retourner le DTO de réponse mis à jour
 *
 * Règles appliquées :
 *   RG-11 : reconstruction des relations demande_piece
 *   RG-12 : dateDemande, idDemandeur, typeDemande immutables
 *   RG-13 : validations existantes appliquées
 *   RG-14 : historique créé si changement de statut
 *
 * @param id    identifiant de la demande à modifier
 * @param dto   nouvelles données (formulaire soumis)
 * @return      DemandeResponseDTO avec modifications appliquées
 * @throws BusinessException si règle métier violée
 * @throws ResourceNotFoundException si demande absente
 */
public DemandeResponseDTO modifierDemande(Long id, DemandeUpdateDTO dto) {

    // ÉTAPE 1 — Récupérer la demande existante
    Demande demande = demandeRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

    // ÉTAPE 2 — Vérifier immutabilité des champs (optionnel mais prudent)
    // Les champs immutables du DTO ne sont présents qu'en affichage (lecture seule)
    // Aucune modification ne peut les affecter depuis le formulaire

    // ÉTAPE 3 — Mettre à jour le Demandeur (champs modifiables uniquement)
    // ⚠️ NE PAS MODIFIER : nom, dateNaissance, nationalité
    Demandeur demandeur = demande.getDemandeur();
    demandeur.setPrenom(dto.getDemandeurDTO().getPrenom());
    demandeur.setNomJeuneFille(dto.getDemandeurDTO().getNomJeuneFille());
    demandeur.setLieuNaissance(dto.getDemandeurDTO().getLieuNaissance());
    demandeur.setAdresseMadagascar(dto.getDemandeurDTO().getAdresseMadagascar());
    demandeur.setTelephone(dto.getDemandeurDTO().getTelephone());
    demandeur.setEmail(dto.getDemandeurDTO().getEmail());

    if (dto.getDemandeurDTO().getIdSituationFamiliale() != null) {
        SituationFamiliale sf = situationFamilialeRepository.findById(dto.getDemandeurDTO().getIdSituationFamiliale())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Situation familiale introuvable : id=" + dto.getDemandeurDTO().getIdSituationFamiliale()));
        demandeur.setSituationFamiliale(sf);
    }

    // ÉTAPE 4 — Mettre à jour le Passeport
    Passeport passeport = demande.getVisaTransformable().getPasseport();
    String ancienNumero = passeport.getNumero();
    String nouveauNumero = dto.getPasseportDTO().getNumero();

    // Vérifier unicité du nouveau numéro (si modifié)
    if (!ancienNumero.equals(nouveauNumero)) {
        if (passeportRepository.existsByNumero(nouveauNumero)) {
            throw new BusinessException("Le numéro de passeport '" + nouveauNumero + "' est déjà utilisé.");
        }
    }

    // Vérifier cohérence des dates
    if (!dto.getPasseportDTO().getDateExpiration().isAfter(dto.getPasseportDTO().getDateDelivrance())) {
        throw new BusinessException("La date d'expiration doit être postérieure à la date de délivrance.");
    }

    passeport.setNumero(nouveauNumero);
    passeport.setDateDelivrance(dto.getPasseportDTO().getDateDelivrance());
    passeport.setDateExpiration(dto.getPasseportDTO().getDateExpiration());

    // ÉTAPE 5 — Mettre à jour le VisaTransformable
    VisaTransformable visa = demande.getVisaTransformable();
    String ancienneRef = visa.getReferenceVisa();
    String nouvelleRef = dto.getVisaDTO().getReferenceVisa();

    // Vérifier unicité de la nouvelle référence (si modifiée)
    if (!ancienneRef.equals(nouvelleRef)) {
        if (visaTransformableRepository.findByReferenceVisa(nouvelleRef).isPresent()) {
            throw new BusinessException("La référence visa '" + nouvelleRef + "' est déjà utilisée.");
        }
    }

    // Vérifier cohérence des dates
    if (!dto.getVisaDTO().getDateExpiration().isAfter(dto.getVisaDTO().getDateEntree())) {
        throw new BusinessException("La date d'expiration du visa doit être postérieure à la date d'entrée.");
    }

    visa.setReferenceVisa(nouvelleRef);
    visa.setDateEntree(dto.getVisaDTO().getDateEntree());
    visa.setLieuEntree(dto.getVisaDTO().getLieuEntree());
    visa.setDateExpiration(dto.getVisaDTO().getDateExpiration());

    // ÉTAPE 6 — Mettre à jour le TypeVisa
    TypeVisa typeVisa = typeVisaRepository.findById(dto.getIdTypeVisa())
            .orElseThrow(() -> new ResourceNotFoundException("TypeVisa introuvable : id=" + dto.getIdTypeVisa()));
    demande.setTypeVisa(typeVisa);

    // ÉTAPE 7 — Recréer les liens DemandePiece
    // Supprimer les anciens liens
    demandePieceRepository.deleteByDemandeId(id);

    // Ajouter les nouveaux liens
    lierPiecesADemande(demande, dto.getPiecesFournies(), typeVisa);

    // ÉTAPE 8 — Sauvegarder la demande
    demande = demandeRepository.save(demande);

    // ÉTAPE 9 — Recharger avec pièces et retourner DTO
    Demande demandeFinal = demandeRepository.findById(demande.getId()).orElseThrow();
    return demandeMapper.toResponseDTO(demandeFinal);
}
```

**Dépendances à ajouter au service** :

```java
private final SituationFamilialeRepository situationFamilialeRepository;
```

---

### ✅ TEST ÉTAPE 9.4 — `DemandeServiceUpdateTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DemandeServiceUpdateTest {

    // Mocks
    @Mock DemandeRepository demandeRepository;
    @Mock DemandeMapper demandeMapper;
    @Mock PasseportRepository passeportRepository;
    @Mock VisaTransformableRepository visaTransformableRepository;
    @Mock TypeVisaRepository typeVisaRepository;
    @Mock SituationFamilialeRepository situationFamilialeRepository;
    @Mock DemandePieceRepository demandePieceRepository;
    @Mock PieceRepository pieceRepository;
    @InjectMocks DemandeService demandeService;

    // ── Test 9.4.1 — Modification complète OK ──────────────────────────
    @Test
    void modifierDemande_OK() {
        Long demandeId = 1L;
        Demande demande = buildDemandeComplete();
        DemandeUpdateDTO dto = buildDemandeUpdateDTOValide();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(typeVisaRepository.findById(dto.getIdTypeVisa())).thenReturn(Optional.of(demande.getTypeVisa()));
        when(passeportRepository.existsByNumero(dto.getPasseportDTO().getNumero())).thenReturn(false);
        when(visaTransformableRepository.findByReferenceVisa(dto.getVisaDTO().getReferenceVisa()))
            .thenReturn(Optional.empty());
        when(pieceRepository.findByTypePieceCodeIn(any())).thenReturn(new ArrayList<>());
        when(demandeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(demandeMapper.toResponseDTO(any())).thenReturn(new DemandeResponseDTO());

        DemandeResponseDTO result = demandeService.modifierDemande(demandeId, dto);

        assertNotNull(result);
        verify(demandePieceRepository, times(1)).deleteByDemandeId(demandeId);
        verify(demandeRepository, times(1)).save(any());
    }

    // ── Test 9.4.2 — Numéro passeport dupliqué → BusinessException ──────
    @Test
    void modifierDemande_numeroPasSeportDuplique_throwsException() {
        Long demandeId = 1L;
        Demande demande = buildDemandeComplete();
        DemandeUpdateDTO dto = buildDemandeUpdateDTOValide();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(passeportRepository.existsByNumero(any())).thenReturn(true);  // dupliqué

        assertThrows(BusinessException.class, () -> demandeService.modifierDemande(demandeId, dto));
        verify(demandeRepository, never()).save(any());
    }

    // ── Test 9.4.3 — Référence visa dupliquée → BusinessException ──────
    @Test
    void modifierDemande_referenceVisaDupliquee_throwsException() {
        Long demandeId = 1L;
        Demande demande = buildDemandeComplete();
        DemandeUpdateDTO dto = buildDemandeUpdateDTOValide();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(passeportRepository.existsByNumero(any())).thenReturn(false);
        when(visaTransformableRepository.findByReferenceVisa(any()))
            .thenReturn(Optional.of(new VisaTransformable()));  // dupliqué

        assertThrows(BusinessException.class, () -> demandeService.modifierDemande(demandeId, dto));
    }

    // ── Test 9.4.4 — Dates incohérentes (passeport) → BusinessException ──
    @Test
    void modifierDemande_datesIncohererentes_throwsException() {
        Long demandeId = 1L;
        Demande demande = buildDemandeComplete();
        DemandeUpdateDTO dto = buildDemandeUpdateDTOValide();
        dto.getPasseportDTO().setDateDelivrance(LocalDate.of(2025, 6, 1));
        dto.getPasseportDTO().setDateExpiration(LocalDate.of(2025, 5, 1));  // avant

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));

        assertThrows(BusinessException.class, () -> demandeService.modifierDemande(demandeId, dto));
    }

    // ── Test 9.4.5 — getDemandePourEdition → DTO édition correct ──────
    @Test
    void getDemandePourEdition_OK() {
        Long demandeId = 1L;
        Demande demande = buildDemandeComplete();
        DemandeUpdateDTO updateDTO = new DemandeUpdateDTO();

        when(demandeRepository.findById(demandeId)).thenReturn(Optional.of(demande));
        when(demandeMapper.toUpdateDTO(demande)).thenReturn(updateDTO);

        DemandeUpdateDTO result = demandeService.getDemandePourEdition(demandeId);

        assertNotNull(result);
        verify(demandeMapper, times(1)).toUpdateDTO(demande);
    }

    // ── Test 9.4.6 — getDemandePourEdition introuvable → exception ──────
    @Test
    void getDemandePourEdition_introuvable_throwsException() {
        when(demandeRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> demandeService.getDemandePourEdition(999L));
    }
}
```

---

## 9.5 — CONTROLLER

### Extension de `DemandeController.java`

**Ajout dans** : `src/main/java/com/visa/backoffice/controller/DemandeController.java`

```java
// ════════════════════════════════════════════════════════
//  GET /demandes/{id}/editer
//  Afficher le formulaire de modification
// ════════════════════════════════════════════════════════
@GetMapping("/{id}/editer")
public String afficherFormulaireEdition(
        @PathVariable Long id,
        Model model) {

    DemandeUpdateDTO demande = demandeService.getDemandePourEdition(id);
    model.addAttribute("demandeId", id);
    model.addAttribute("demandeForm", demande);
    model.addAttribute("typesVisa", typeVisaRepository.findAll());
    model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
    model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
    return "demande/editer";
}

// ════════════════════════════════════════════════════════
//  GET /demandes/{id}/editer-pieces?idTypeVisa=1
//  AJAX : retourner les pièces spécifiques en JSON
// ════════════════════════════════════════════════════════
@GetMapping("/{id}/editer-pieces")
@ResponseBody
public ResponseEntity<List<PieceDTO>> getPiecesEdition(
        @PathVariable Long id,
        @RequestParam Long idTypeVisa) {

    List<PieceDTO> pieces = demandeService.getPiecesFormulaire(idTypeVisa);
    return ResponseEntity.ok(pieces);
}

// ════════════════════════════════════════════════════════
//  POST /demandes/{id}/editer
//  Soumettre et enregistrer la modification
// ════════════════════════════════════════════════════════
@PostMapping("/{id}/editer")
public String soumettreFormulaireMo dification(
        @PathVariable Long id,
        @Valid @ModelAttribute("demandeForm") DemandeUpdateDTO dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {

    // Si erreurs de validation Bean Validation
    if (result.hasErrors()) {
        model.addAttribute("demandeId", id);
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "demande/editer";
    }

    try {
        DemandeResponseDTO response = demandeService.modifierDemande(id, dto);
        redirectAttributes.addFlashAttribute("successMessage", "Demande modifiée avec succès. ID : #" + response.getId());
        return "redirect:/demandes/" + response.getId() + "/editer-confirmation";

    } catch (BusinessException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("demandeId", id);
        model.addAttribute("demandeForm", dto);
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "demande/editer";
    }
}

// ════════════════════════════════════════════════════════
//  GET /demandes/{id}/editer-confirmation
//  Page de confirmation post-modification
// ════════════════════════════════════════════════════════
@GetMapping("/{id}/editer-confirmation")
public String afficherConfirmationEdition(@PathVariable Long id, Model model) {
    DemandeResponseDTO demande = demandeService.getDemande(id);
    model.addAttribute("demande", demande);
    model.addAttribute("isModification", true);
    return "demande/editer-confirmation";
}
```

---

### ✅ TEST ÉTAPE 9.5 — `DemandeControllerUpdateTest.java`

```java
@WebMvcTest(DemandeController.class)
class DemandeControllerUpdateTest {

    @Autowired MockMvc mockMvc;
    @MockBean DemandeService demandeService;
    @MockBean TypeVisaRepository typeVisaRepository;
    @MockBean SituationFamilialeRepository situationFamilialeRepository;
    @MockBean NationaliteRepository nationaliteRepository;
    @MockBean PieceService pieceService;

    // Test 9.5.1 — GET /demandes/{id}/editer → 200, formulaire édition
    @Test
    void afficherFormulaireEdition_renvoie200() throws Exception {
        DemandeUpdateDTO updateDTO = new DemandeUpdateDTO();
        when(demandeService.getDemandePourEdition(1L)).thenReturn(updateDTO);
        when(typeVisaRepository.findAll()).thenReturn(List.of());
        when(pieceService.getPiecesCommunes()).thenReturn(List.of());

        mockMvc.perform(get("/demandes/1/editer"))
               .andExpect(status().isOk())
               .andExpect(view().name("demande/editer"))
               .andExpect(model().attributeExists("demandeForm"));
    }

    // Test 9.5.2 — GET /demandes/{id}/editer-pieces?idTypeVisa=1 → JSON
    @Test
    void getPiecesEdition_renvoieJSON() throws Exception {
        when(demandeService.getPiecesFormulaire(1L)).thenReturn(List.of(new PieceDTO()));

        mockMvc.perform(get("/demandes/1/editer-pieces").param("idTypeVisa", "1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Test 9.5.3 — POST /demandes/{id}/editer valide → redirection confirmation
    @Test
    void soumettreFormulaireMod ification_OK_redirige() throws Exception {
        DemandeResponseDTO resp = new DemandeResponseDTO();
        resp.setId(1L);
        when(demandeService.modifierDemande(eq(1L), any())).thenReturn(resp);

        mockMvc.perform(post("/demandes/1/editer").with(csrf())
                   /* params valides */)
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/demandes/1/editer-confirmation"));
    }

    // Test 9.5.4 — POST avec BindingResult en erreur → retour formulaire
    @Test
    void soumettreFormulaireModification_erreurValidation_resteSurFormulaire() throws Exception {
        mockMvc.perform(post("/demandes/1/editer").with(csrf())
                   /* champs manquants */)
               .andExpect(status().isOk())
               .andExpect(view().name("demande/editer"));
    }

    // Test 9.5.5 — GET /demandes/{id}/editer-confirmation → 200
    @Test
    void afficherConfirmationEdition_renvoie200() throws Exception {
        DemandeResponseDTO demande = new DemandeResponseDTO();
        when(demandeService.getDemande(1L)).thenReturn(demande);

        mockMvc.perform(get("/demandes/1/editer-confirmation"))
               .andExpect(status().isOk())
               .andExpect(view().name("demande/editer-confirmation"));
    }
}
```

---

## 9.6 — TEMPLATES THYMELEAF

### 9.6.1 — `editer.html`

**Fichier** : `src/main/resources/templates/demande/editer.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Modifier une Demande</title>
</head>
<body>

<h1>MODIFICATION DE DEMANDE DE VISA TRANSFORMABLE</h1>

<!-- Message d'erreur -->
<div th:if="${errorMessage}" class="alert alert-danger">
    <span th:text="${errorMessage}"></span>
</div>

<form th:action="@{/demandes/{id}/editer(id=${demandeId})}" th:object="${demandeForm}" method="post">

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟦 ÉTAT CIVIL (PARTIELLEMENT MODIFIABLE)  -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="etat-civil">
        <h2>État Civil</h2>

        <!-- Champs IMMUTABLES (affichage seul) -->
        <div class="field-immutable">
            <label>Nom (IMMUTABLE)</label>
            <input type="text" th:value="*{demandeurDTO.nomImmutable}" readonly />
        </div>

        <div class="field-immutable">
            <label>Date de naissance (IMMUTABLE)</label>
            <input type="date" th:value="*{demandeurDTO.dateNaissanceImmutable}" readonly />
        </div>

        <div class="field-immutable">
            <label>Nationalité (IMMUTABLE)</label>
            <input type="text" th:value="*{demandeurDTO.nationaliteImmutable}" readonly />
        </div>

        <!-- Champs modifiables -->
        <label>Prénom</label>
        <input type="text" th:field="*{demandeurDTO.prenom}" />

        <label>Nom jeune fille</label>
        <input type="text" th:field="*{demandeurDTO.nomJeuneFille}" />

        <label>Date de naissance</label>
        <input type="date" th:field="*{demandeurDTO.dateNaissance}" />

        <label>Lieu de naissance</label>
        <input type="text" th:field="*{demandeurDTO.lieuNaissance}" />

        <label>Situation familiale</label>
        <select th:field="*{demandeurDTO.idSituationFamiliale}">
            <option value="">-- Sélectionner --</option>
            <option th:each="sf : ${situationsFamiliales}"
                    th:value="${sf.id}"
                    th:text="${sf.libelle}">
            </option>
        </select>

        <label>Adresse Madagascar (O)</label>
        <textarea th:field="*{demandeurDTO.adresseMadagascar}" required></textarea>
        <span th:errors="*{demandeurDTO.adresseMadagascar}"></span>

        <label>Téléphone (O)</label>
        <input type="tel" th:field="*{demandeurDTO.telephone}" required />
        <span th:errors="*{demandeurDTO.telephone}"></span>

        <label>Email</label>
        <input type="email" th:field="*{demandeurDTO.email}" />
        <span th:errors="*{demandeurDTO.email}"></span>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟩 PASSEPORT (MODIFIABLE)           -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="passeport">
        <h2>Passeport</h2>

        <label>Numéro (O, U)</label>
        <input type="text" th:field="*{passeportDTO.numero}" required />
        <span th:errors="*{passeportDTO.numero}"></span>

        <label>Date de délivrance (O)</label>
        <input type="date" th:field="*{passeportDTO.dateDelivrance}" required />
        <span th:errors="*{passeportDTO.dateDelivrance}"></span>

        <label>Date d'expiration (O)</label>
        <input type="date" th:field="*{passeportDTO.dateExpiration}" required />
        <span th:errors="*{passeportDTO.dateExpiration}"></span>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟨 VISA TRANSFORMABLE (MODIFIABLE)  -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="visa-transformable">
        <h2>Visa Transformable</h2>

        <label>Référence Visa (O, U)</label>
        <input type="text" th:field="*{visaDTO.referenceVisa}" required />
        <span th:errors="*{visaDTO.referenceVisa}"></span>

        <label>Date d'entrée (O)</label>
        <input type="date" th:field="*{visaDTO.dateEntree}" required />
        <span th:errors="*{visaDTO.dateEntree}"></span>

        <label>Lieu d'entrée (O)</label>
        <input type="text" th:field="*{visaDTO.lieuEntree}" required />
        <span th:errors="*{visaDTO.lieuEntree}"></span>

        <label>Date d'expiration visa (O)</label>
        <input type="date" th:field="*{visaDTO.dateExpiration}" required />
        <span th:errors="*{visaDTO.dateExpiration}"></span>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟥 DEMANDE (PARTIELLEMENT MODIFIABLE) -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="demande">
        <h2>Demande</h2>

        <label>Type de visa (O)</label>
        <select id="selectTypeVisa" th:field="*{idTypeVisa}" required>
            <option value="">-- Sélectionner --</option>
            <option th:each="tv : ${typesVisa}"
                    th:value="${tv.id}"
                    th:text="${tv.libelle}">
            </option>
        </select>
        <span th:errors="*{idTypeVisa}"></span>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BLOC 🟪 PIÈCES À FOURNIR (MODIFIABLE)     -->
    <!-- ═══════════════════════════════════════════ -->
    <section id="pieces">
        <h2>Pièces à fournir</h2>

        <h3>Pièces communes</h3>
        <div th:each="piece : ${piecesCommunes}">
            <input type="checkbox"
                   name="piecesFournies"
                   th:value="${piece.id}"
                   th:id="'piece_' + ${piece.id}"
                   th:checked="${#lists.contains(demandeForm.piecesFournies, piece.id)}" />
            <label th:for="'piece_' + ${piece.id}">
                <span th:text="${piece.nom}"></span>
                <span th:if="${piece.obligatoire}" class="badge-obligatoire"> *</span>
            </label>
        </div>

        <h3>Pièces spécifiques</h3>
        <div id="pieces-specifiques">
            <!-- Contenu injecté par JavaScript -->
        </div>
    </section>

    <!-- ═══════════════════════════════════════════ -->
    <!-- BOUTONS                                     -->
    <!-- ═══════════════════════════════════════════ -->
    <div class="form-actions">
        <button type="submit">ENREGISTRER MODIFICATION</button>
        <a th:href="@{/demandes/{id}(id=${demandeId})}">ANNULER</a>
    </div>

</form>

<script th:inline="javascript">
    const demandeId = /*[[${demandeId}]]*/ null;
    const selectedPieces = /*[[${demandeForm.piecesFournies}]]*/ [];

    document.getElementById('selectTypeVisa').addEventListener('change', function () {
        const idTypeVisa = this.value;
        const container = document.getElementById('pieces-specifiques');
        container.innerHTML = '';

        if (!idTypeVisa) return;

        fetch('/demandes/' + demandeId + '/editer-pieces?idTypeVisa=' + idTypeVisa)
            .then(response => response.json())
            .then(pieces => {
                pieces.forEach(piece => {
                    const div = document.createElement('div');
                    const checkbox = document.createElement('input');
                    checkbox.type = 'checkbox';
                    checkbox.name = 'piecesFournies';
                    checkbox.value = piece.id;
                    checkbox.id = 'spec_piece_' + piece.id;
                    checkbox.checked = selectedPieces.includes(piece.id);

                    const label = document.createElement('label');
                    label.htmlFor = 'spec_piece_' + piece.id;
                    label.textContent = piece.nom + (piece.obligatoire ? ' *' : '');

                    div.appendChild(checkbox);
                    div.appendChild(label);
                    container.appendChild(div);
                });
            })
            .catch(err => console.error('Erreur chargement pièces:', err));
    });

    // Recharger les pièces au chargement de la page
    document.getElementById('selectTypeVisa').dispatchEvent(new Event('change'));
</script>

</body>
</html>
```

---

### 9.6.2 — `editer-confirmation.html`

**Fichier** : `src/main/resources/templates/demande/editer-confirmation.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Confirmation de Modification</title>
</head>
<body>

<h1 th:if="${isModification}">Demande modifiée avec succès</h1>
<h1 th:unless="${isModification}">Demande enregistrée avec succès</h1>

<!-- Message flash -->
<div th:if="${successMessage}" class="alert alert-success">
    <span th:text="${successMessage}"></span>
</div>

<section id="recap-demande">
    <h2>Récapitulatif</h2>
    <table>
        <tr><th>Numéro de demande</th>  <td th:text="${demande.id}"></td></tr>
        <tr><th>Date de demande</th>    <td th:text="${demande.dateDemande}"></td></tr>
        <tr><th>Demandeur</th>          <td th:text="${demande.nomDemandeur + ' ' + demande.prenomDemandeur}"></td></tr>
        <tr><th>Référence Visa</th>     <td th:text="${demande.referenceVisa}"></td></tr>
        <tr><th>Type de Visa</th>       <td th:text="${demande.typeVisa}"></td></tr>
        <tr><th>Type de Demande</th>    <td th:text="${demande.typeDemande}"></td></tr>
        <tr>
            <th>Statut</th>
            <td>
                <span class="badge badge-cree" th:text="${demande.statutDemande}"></span>
            </td>
        </tr>
    </table>
</section>

<section id="recap-pieces">
    <h2>Pièces associées</h2>
    <table>
        <thead>
            <tr>
                <th>Pièce</th>
                <th>Obligatoire</th>
                <th>Fournie</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="piece : ${demande.pieces}">
                <td th:text="${piece.nomPiece}"></td>
                <td th:text="${piece.obligatoire ? 'Oui' : 'Non'}"></td>
                <td>
                    <span th:if="${piece.fourni}" class="badge-ok">✔</span>
                    <span th:unless="${piece.fourni}" class="badge-nok">✗</span>
                </td>
            </tr>
        </tbody>
    </table>
</section>

<div class="actions">
    <a th:href="@{/demandes/{id}/editer(id=${demande.id})}">Éditer à nouveau</a>
    <a th:href="@{/}">Retour à l'accueil</a>
</div>

</body>
</html>
```

---

## 9.7 — REPOSITORY UPDATE

### Modification de `DemandePieceRepository.java`

**Ajout dans** : `src/main/java/com/visa/backoffice/repository/DemandePieceRepository.java`

```java
/**
 * Supprime tous les liens DemandePiece pour une demande donnée.
 * Utilisé lors de la modification : on supprime les anciens liens avant de les recréer.
 *
 * @param demandeId identifiant de la demande
 */
void deleteByDemandeId(Long demandeId);
```

---

## 9.8 — RÉSUMÉ DES NOUVELLES RÈGLES DE GESTION

| Code | Règle | Implémentée dans |
|------|-------|-----------------|
| RG-11 | Lors de la modification, les relations `demande_piece` sont recréées | `DemandeService.modifierDemande()` |
| RG-12 | `dateDemande`, `idDemandeur`, `typeDemande` sont immutables (non modifiables) | `DemandeUpdateDTO`, `DemandeService` |
| RG-13 | Les validations existantes (unicité, cohérence des dates) s'appliquent lors de la modification | `DemandeService.modifierDemande()` |
| RG-14 | Un historique statut est créé si le statut change (futur) | `DemandeService.creerHistoriqueStatut()` |
| RG-15 | Le demandeur : seuls les champs modifiables sont altérables (nom/dateNaissance/nationalité bloqués) | `DemandeurUpdateDTO`, `DemandeService.modifierDemande()` |

---

## 9.9 — URLs DE TEST POUR LA MODIFICATION

```
GET  http://localhost:8080/demandes/{idDemande}/editer
     → Affiche le formulaire de modification avec les données actuelles
     → Exemple: http://localhost:8080/demandes/1/editer

POST http://localhost:8080/demandes/{idDemande}/editer
     → Soumet les modifications
     → Exemple: http://localhost:8080/demandes/1/editer
     → Réponse: redirection vers /demandes/1/editer-confirmation

GET  http://localhost:8080/demandes/{idDemande}/editer-pieces?idTypeVisa=1
     → AJAX : retourne les pièces spécifiques au type visa sélectionné
     → Exemple: http://localhost:8080/demandes/1/editer-pieces?idTypeVisa=2

GET  http://localhost:8080/demandes/{idDemande}/editer-confirmation
     → Affiche la page de confirmation post-modification
     → Exemple: http://localhost:8080/demandes/1/editer-confirmation
```

---

## 9.10 — RÉSUMÉ DES FICHIERS À CRÉER/MODIFIER

### À CRÉER

1. `src/main/java/com/visa/backoffice/dto/DemandeUpdateDTO.java`
2. `src/main/java/com/visa/backoffice/dto/DemandeurUpdateDTO.java`
3. `src/main/resources/templates/demande/editer.html`
4. `src/main/resources/templates/demande/editer-confirmation.html`
5. `src/test/java/com/visa/backoffice/service/DemandeServiceUpdateTest.java`
6. `src/test/java/com/visa/backoffice/controller/DemandeControllerUpdateTest.java`

### À MODIFIER

1. `src/main/java/com/visa/backoffice/mapper/DemandeMapper.java` (ajouter `toUpdateDTO()`)
2. `src/main/java/com/visa/backoffice/service/DemandeService.java` (ajouter `modifierDemande()`, `getDemandePourEdition()`)
3. `src/main/java/com/visa/backoffice/controller/DemandeController.java` (ajouter 3 endpoints)
4. `src/main/java/com/visa/backoffice/repository/DemandePieceRepository.java` (ajouter `deleteByDemandeId()`)

---

# ORDRE D'EXÉCUTION RECOMMANDÉ (MISE À JOUR)

```
Semaine 1 (identique)
  ├─ Étape 1 : Entités  (1.1 → 1.6)      + Tests T-01
  ├─ Étape 2 : DTOs     (2.1 → 2.5)      + Tests T-02 à T-04
  ├─ Étape 3 : Repos    (3.1 → 3.6)      + Tests T-05 à T-09
  └─ Étape 4 : Mappers  (4.1 → 4.2)      + Tests T-10 à T-11

Semaine 2 (identique)
  ├─ Étape 5.1 : VisaTransformableService + Tests T-12 à T-15
  ├─ Étape 5.2 : PieceService             + Tests T-16 à T-18
  ├─ Étape 5.3 : DemandeService           + Tests T-19 à T-26
  ├─ Étape 6   : Controller               + Tests T-27 à T-32
  └─ Étape 7   : Templates Thymeleaf (formulaire + confirmation)

Semaine 3 (NOUVELLE : MODIFICATION)
  ├─ Étape 9.2 : DTOs édition (DemandeUpdateDTO + DemandeurUpdateDTO)
  ├─ Étape 9.3 : Extension Mapper (toUpdateDTO)
  ├─ Étape 9.4 : Extension Service (modifierDemande, getDemandePourEdition) + Tests
  ├─ Étape 9.5 : Extension Controller (endpoints édition) + Tests
  ├─ Étape 9.6 : Templates editer.html + editer-confirmation.html
  └─ Étape 9.7 : Extension Repository (deleteByDemandeId)
```

> ⚠️ **Priorité** : Ne commencer l'Étape 9 qu'une fois l'Étape 7 terminée et testée.
> ⚠️ **Dépendances** : La modification dépend de tous les services existants (Dev1). Synchroniser avec Dev1 avant de commencer.

---
