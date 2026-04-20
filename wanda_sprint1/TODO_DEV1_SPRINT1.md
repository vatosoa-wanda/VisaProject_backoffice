# TODO — DEV 1 : DEMANDEUR + PASSEPORT
## Sprint 1 — Feature : `feature/sprint1_1_Enregistrement_Demandeur_Passeport`

> **Convention de lecture**
> - `(O)` Obligatoire · `(F)` Facultatif · `(U)` Unique · `(H)` Hidden · `(S)` Select
> - `✅ TEST` = test unitaire à écrire immédiatement après l'étape
> - `⚠️ RG` = Règle de Gestion métier à respecter
> - `🔗 API TEAMLEAD` = dépendance sur le code fourni par le Team Lead
> - `📤 LIVRAISON DEV2` = élément que Dev 2 consommera — contrat d'interface à respecter

---

## PRÉ-REQUIS — Ce que le TeamLead doit avoir livré avant de commencer

| Élément attendu du TeamLead | Package | Usage |
|---|---|---|
| `TypeDemande.java` (entity) | `entity/` | référencé dans `Demande` |
| `StatutDemande.java` (entity) | `entity/` | référencé dans `Demande` |
| `TypePiece.java` (entity) | `entity/` | référencé dans `Piece` |
| `TypeDemandeRepository.java` | `repository/` | utilisé par Dev2 |
| `StatutDemandeRepository.java` | `repository/` | utilisé par Dev2 |
| `BusinessException.java` | `exception/` | levée dans les services |
| `ResourceNotFoundException.java` | `exception/` | levée dans les services |
| Script SQL initialisé et base accessible | — | entités `situation_familiale`, `nationalite` pré-insérées |

> Si le TeamLead n'a pas encore livré `BusinessException` ou `ResourceNotFoundException`, les créer temporairement dans le package `exception/` et supprimer les doublons à la livraison.

---

## CE QUE DEV 1 DOIT LIVRER À DEV 2

> ⚠️ Ces éléments sont des **contrats d'interface**. Dev 2 en dépend directement. Ne pas modifier les signatures sans concertation.

| Élément livré | Signature / Type de retour | Consommé par |
|---|---|---|
| `Demandeur.java` | entité JPA complète | `Demande.java` (Dev2) |
| `Passeport.java` | entité JPA complète | `VisaTransformable.java` (Dev2) |
| `DemandeurDTO.java` | champs état civil complets | `DemandeCreateDTO.java` (Dev2) |
| `PasseportDTO.java` | numéro + dates | `DemandeCreateDTO.java` (Dev2) |
| `DemandeurService.creerOuRecuperer(DemandeurDTO) : Demandeur` | public, @Transactional | `DemandeService.creerDemande()` (Dev2) |
| `PasseportService.creer(PasseportDTO, Demandeur) : Passeport` | public, @Transactional | `DemandeService.creerDemande()` (Dev2) |
| `SituationFamilialeRepository.findAll()` | `List<SituationFamiliale>` | `DemandeController` (Dev2) |
| `NationaliteRepository.findAll()` | `List<Nationalite>` | `DemandeController` (Dev2) |

---

## STRUCTURE DES FICHIERS À CRÉER

```
src/main/java/com/projet/visa/
├── entity/
│   ├── SituationFamiliale.java          ← à créer
│   ├── Nationalite.java                 ← à créer
│   ├── Demandeur.java                   ← à créer  📤 LIVRAISON DEV2
│   └── Passeport.java                   ← à créer  📤 LIVRAISON DEV2
├── dto/
│   ├── DemandeurDTO.java                ← à créer  📤 LIVRAISON DEV2
│   ├── DemandeurResponseDTO.java        ← à créer
│   └── PasseportDTO.java                ← à créer  📤 LIVRAISON DEV2
├── mapper/
│   ├── DemandeurMapper.java             ← à créer
│   └── PasseportMapper.java             ← à créer
├── repository/
│   ├── SituationFamilialeRepository.java ← à créer  📤 LIVRAISON DEV2
│   ├── NationaliteRepository.java        ← à créer  📤 LIVRAISON DEV2
│   ├── DemandeurRepository.java          ← à créer
│   └── PasseportRepository.java          ← à créer
├── service/
│   ├── DemandeurService.java            ← à créer  📤 LIVRAISON DEV2
│   └── PasseportService.java            ← à créer  📤 LIVRAISON DEV2
└── controller/
    └── DemandeurController.java         ← à créer (endpoints de test / consultation)

src/main/resources/templates/
└── demandeur/
    └── recherche.html                   ← à créer (page de recherche demandeur existant)

src/test/java/com/projet/visa/
├── service/
│   ├── DemandeurServiceTest.java        ← à créer
│   └── PasseportServiceTest.java        ← à créer
├── mapper/
│   ├── DemandeurMapperTest.java         ← à créer
│   └── PasseportMapperTest.java         ← à créer
├── repository/
│   └── DemandeurRepositoryTest.java     ← à créer
└── controller/
    └── DemandeurControllerTest.java     ← à créer
```

---

---

# ÉTAPE 1 — ENTITÉS (Entity Layer)

> **Ordre d'implémentation obligatoire** : SituationFamiliale → Nationalite → Demandeur → Passeport

---

## 1.1 — `SituationFamiliale.java`

**Fichier** : `src/main/java/com/projet/visa/entity/SituationFamiliale.java`

```java
@Entity
@Table(name = "situation_familiale")
public class SituationFamiliale {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;
    // valeurs pré-insérées en base : "Celibataire", "Marie", "Divorce", "Veuf"

    // getters / setters
}
```

**Notes** :
- Entité de référence, données pré-insérées via le script SQL du TeamLead
- Pas de création/modification par l'application (lecture seule depuis le formulaire)
- Utilisée dans le `select` du formulaire état civil

---

## 1.2 — `Nationalite.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Nationalite.java`

```java
@Entity
@Table(name = "nationalite")
public class Nationalite {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "libelle", nullable = false, unique = true)
    private String libelle;
    // valeurs pré-insérées : "Malagasy", "Française", "Chinoise", "Indienne", "Comorienne"

    // getters / setters
}
```

**Notes** :
- Entité de référence, lecture seule depuis le formulaire
- Utilisée dans le `select` nationalité du bloc état civil

---

## 1.3 — `Demandeur.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Demandeur.java`

```java
@Entity
@Table(name = "demandeur")
public class Demandeur {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "nom", nullable = false, length = 100)
    private String nom;                          // (O)

    @Column(name = "prenom", length = 100)
    private String prenom;                       // (F)

    @Column(name = "nom_jeune_fille", length = 100)
    private String nomJeuneFille;                // (F)

    @Column(name = "date_naissance", nullable = false)
    private LocalDate dateNaissance;             // (O)

    @Column(name = "lieu_naissance", length = 150)
    private String lieuNaissance;               // (F)

    @ManyToOne
    @JoinColumn(name = "id_situation_familiale")
    private SituationFamiliale situationFamiliale;  // (O, S)

    @ManyToOne
    @JoinColumn(name = "id_nationalite", nullable = false)
    private Nationalite nationalite;             // (O, S)

    @Column(name = "adresse_madagascar", nullable = false)
    private String adresseMadagascar;            // (O)

    @Column(name = "telephone", nullable = false, length = 20)
    private String telephone;                    // (O)

    @Column(name = "email", length = 150)
    private String email;                        // (F)

    // getters / setters
}
```

⚠️ **RG-01** : `nom`, `dateNaissance`, `idNationalite`, `adresseMadagascar`, `telephone` sont obligatoires
⚠️ **RG-02** : `dateNaissance` doit être dans le passé (vérification en service)
⚠️ **RG-03** : `telephone` doit correspondre à un format valide (chiffres, longueur 7–15)
⚠️ **RG-04** : `email`, s'il est fourni, doit être au format valide (vérification via annotation `@Email`)
⚠️ **RG-05** : un demandeur existant est identifié par `nom` + `dateNaissance` + `nationalite` → pas de doublon

---

## 1.4 — `Passeport.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Passeport.java`

```java
@Entity
@Table(name = "passeport")
public class Passeport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "numero", unique = true, length = 50)
    private String numero;                       // (O, U)

    @Column(name = "date_delivrance")
    private LocalDate dateDelivrance;            // (O)

    @Column(name = "date_expiration")
    private LocalDate dateExpiration;            // (O)

    @ManyToOne
    @JoinColumn(name = "id_demandeur", nullable = false)
    private Demandeur demandeur;                 // lien obligatoire

    // getters / setters
}
```

⚠️ **RG-06** : `numero` doit être unique en base (contrainte `unique = true` + vérification en service)
⚠️ **RG-07** : `dateExpiration` doit être strictement après `dateDelivrance` (vérification en service)
⚠️ **RG-08** : `dateExpiration` doit être dans le futur au moment de la soumission (le passeport ne doit pas être expiré)
⚠️ **RG-09** : un demandeur ne peut avoir qu'un seul passeport actif (non expiré) à la fois (vérification en service)
⚠️ **RG-10** : le `Demandeur` est obligatoire (non null)

---

### ✅ TEST ÉTAPE 1 — `EntityInstantiationTest.java`

```java
// Test 1.1 — SituationFamiliale instanciable
SituationFamiliale sf = new SituationFamiliale();
sf.setLibelle("Celibataire");
assertEquals("Celibataire", sf.getLibelle());

// Test 1.2 — Nationalite instanciable
Nationalite nat = new Nationalite();
nat.setLibelle("Malagasy");
assertNotNull(nat.getLibelle());

// Test 1.3 — Demandeur : champs obligatoires définis
Demandeur d = new Demandeur();
d.setNom("Rakoto");
d.setDateNaissance(LocalDate.of(1990, 5, 15));
assertNotNull(d.getNom());
assertNotNull(d.getDateNaissance());

// Test 1.4 — Demandeur : dateNaissance dans le passé (RG-02)
LocalDate datePassee = LocalDate.of(1990, 1, 1);
assertTrue(datePassee.isBefore(LocalDate.now()));

// Test 1.5 — Passeport : dateExpiration > dateDelivrance (RG-07)
Passeport p = new Passeport();
p.setDateDelivrance(LocalDate.of(2020, 1, 1));
p.setDateExpiration(LocalDate.of(2030, 1, 1));
assertTrue(p.getDateExpiration().isAfter(p.getDateDelivrance()));

// Test 1.6 — Passeport : dateExpiration dans le futur (RG-08)
assertTrue(p.getDateExpiration().isAfter(LocalDate.now()));
```

---

---

# ÉTAPE 2 — DTOs

---

## 2.1 — `DemandeurDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeurDTO.java`

> 📤 **LIVRAISON DEV2** — Ce DTO est intégré dans `DemandeCreateDTO.java` de Dev2 tel quel.
> Ne pas renommer les champs sans prévenir Dev2.

```java
public class DemandeurDTO {

    // ── Champs obligatoires ──────────────────────────────────────

    @NotBlank(message = "Le nom est obligatoire")
    private String nom;                                        // (O)

    @NotNull(message = "La date de naissance est obligatoire")
    @Past(message = "La date de naissance doit être dans le passé")
    private LocalDate dateNaissance;                           // (O)

    @NotNull(message = "La nationalité est obligatoire")
    private Long idNationalite;                                // (O, S)

    @NotBlank(message = "L'adresse Madagascar est obligatoire")
    private String adresseMadagascar;                          // (O)

    @NotBlank(message = "Le téléphone est obligatoire")
    @Pattern(
        regexp = "^[0-9+\\s\\-]{7,15}$",
        message = "Format téléphone invalide (7 à 15 chiffres)"
    )
    private String telephone;                                  // (O)

    @NotNull(message = "La situation familiale est obligatoire")
    private Long idSituationFamiliale;                         // (O, S)

    // ── Champs facultatifs ───────────────────────────────────────

    private String prenom;                                     // (F)

    private String nomJeuneFille;                              // (F)

    private String lieuNaissance;                              // (F)

    @Email(message = "Format email invalide")
    private String email;                                      // (F)

    // getters / setters
}
```

---

## 2.2 — `DemandeurResponseDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeurResponseDTO.java`

```java
public class DemandeurResponseDTO {

    private Long id;
    private String nom;
    private String prenom;
    private String nomJeuneFille;
    private LocalDate dateNaissance;
    private String lieuNaissance;
    private String situationFamiliale;     // libellé ex: "Celibataire"
    private String nationalite;            // libellé ex: "Malagasy"
    private String adresseMadagascar;
    private String telephone;
    private String email;

    // getters / setters
}
```

---

## 2.3 — `PasseportDTO.java`

**Fichier** : `src/main/java/com/projet/visa/dto/PasseportDTO.java`

> 📤 **LIVRAISON DEV2** — Ce DTO est intégré dans `DemandeCreateDTO.java` de Dev2 tel quel.
> Ne pas renommer les champs sans prévenir Dev2.

```java
public class PasseportDTO {

    @NotBlank(message = "Le numéro de passeport est obligatoire")
    private String numero;                                     // (O, U)

    @NotNull(message = "La date de délivrance est obligatoire")
    @Past(message = "La date de délivrance doit être dans le passé")
    private LocalDate dateDelivrance;                          // (O)

    @NotNull(message = "La date d'expiration est obligatoire")
    @Future(message = "Le passeport ne doit pas être expiré")
    private LocalDate dateExpiration;                          // (O)

    // getters / setters
}
```

---

### ✅ TEST ÉTAPE 2 — `DtoValidationTest.java`

```java
// Initialisation du validator
private Validator validator;

@BeforeEach
void setUp() {
    ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
    validator = factory.getValidator();
}

// Test 2.1 — DemandeurDTO : nom blank → violation
DemandeurDTO dto = buildDemandeurDTOValide();
dto.setNom("");
Set<ConstraintViolation<DemandeurDTO>> violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("nom")));

// Test 2.2 — DemandeurDTO : dateNaissance dans le futur → violation @Past
dto = buildDemandeurDTOValide();
dto.setDateNaissance(LocalDate.now().plusYears(1));
violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateNaissance")));

// Test 2.3 — DemandeurDTO : email invalide → violation @Email
dto = buildDemandeurDTOValide();
dto.setEmail("pas_un_email");
violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("email")));

// Test 2.4 — DemandeurDTO : email null → pas de violation (facultatif)
dto = buildDemandeurDTOValide();
dto.setEmail(null);
violations = validator.validate(dto);
assertTrue(violations.stream().noneMatch(v -> v.getPropertyPath().toString().equals("email")));

// Test 2.5 — DemandeurDTO : téléphone format invalide → violation @Pattern
dto = buildDemandeurDTOValide();
dto.setTelephone("abc");
violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v -> v.getPropertyPath().toString().equals("telephone")));

// Test 2.6 — PasseportDTO : numéro blank → violation
PasseportDTO pDto = buildPasseportDTOValide();
pDto.setNumero("");
Set<ConstraintViolation<PasseportDTO>> pViol = validator.validate(pDto);
assertTrue(pViol.stream().anyMatch(v -> v.getPropertyPath().toString().equals("numero")));

// Test 2.7 — PasseportDTO : dateExpiration dans le passé → violation @Future
pDto = buildPasseportDTOValide();
pDto.setDateExpiration(LocalDate.now().minusDays(1));
pViol = validator.validate(pDto);
assertTrue(pViol.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateExpiration")));

// Test 2.8 — PasseportDTO : dateDelivrance dans le futur → violation @Past
pDto = buildPasseportDTOValide();
pDto.setDateDelivrance(LocalDate.now().plusDays(1));
pViol = validator.validate(pDto);
assertTrue(pViol.stream().anyMatch(v -> v.getPropertyPath().toString().equals("dateDelivrance")));

// Test 2.9 — DemandeurDTO complet valide → aucune violation
dto = buildDemandeurDTOValide();
violations = validator.validate(dto);
assertTrue(violations.isEmpty());
```

---

---

# ÉTAPE 3 — REPOSITORIES

---

## 3.1 — `SituationFamilialeRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/SituationFamilialeRepository.java`

> 📤 **LIVRAISON DEV2** — `findAll()` utilisé dans `DemandeController` (Dev2) pour peupler le select.

```java
public interface SituationFamilialeRepository extends JpaRepository<SituationFamiliale, Long> {

    Optional<SituationFamiliale> findByLibelle(String libelle);
    // → utile si on résout par libellé depuis un import ou un test
}
```

---

## 3.2 — `NationaliteRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/NationaliteRepository.java`

> 📤 **LIVRAISON DEV2** — `findAll()` utilisé dans `DemandeController` (Dev2) pour peupler le select.

```java
public interface NationaliteRepository extends JpaRepository<Nationalite, Long> {

    Optional<Nationalite> findByLibelle(String libelle);
    // → utile pour résoudre par libellé
}
```

---

## 3.3 — `DemandeurRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/DemandeurRepository.java`

```java
public interface DemandeurRepository extends JpaRepository<Demandeur, Long> {

    Optional<Demandeur> findByNomAndDateNaissanceAndNationaliteId(
        String nom,
        LocalDate dateNaissance,
        Long nationaliteId
    );
    // → clé de déduplication (RG-05) : rechercher un demandeur existant avant d'en créer un nouveau

    List<Demandeur> findByNom(String nom);
    // → recherche par nom pour la page de recherche

    List<Demandeur> findByNomContainingIgnoreCase(String nom);
    // → recherche partielle insensible à la casse

    boolean existsByNomAndDateNaissanceAndNationaliteId(
        String nom,
        LocalDate dateNaissance,
        Long nationaliteId
    );
    // → vérification rapide d'existence sans charger l'entité
}
```

---

## 3.4 — `PasseportRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/PasseportRepository.java`

```java
public interface PasseportRepository extends JpaRepository<Passeport, Long> {

    Optional<Passeport> findByNumero(String numero);
    // → vérification unicité du numéro de passeport (RG-06)

    Optional<Passeport> findByDemandeurId(Long demandeurId);
    // → récupérer le passeport lié à un demandeur

    boolean existsByNumero(String numero);
    // → vérification rapide d'existence sans charger l'entité

    List<Passeport> findByDemandeurIdAndDateExpirationAfter(Long demandeurId, LocalDate date);
    // → trouver les passeports non expirés d'un demandeur (RG-09)
}
```

---

### ✅ TEST ÉTAPE 3 — `RepositoryTest.java` (tests d'intégration avec H2 in-memory)

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)  // utilise H2
class RepositoryTest {

    @Autowired DemandeurRepository demandeurRepository;
    @Autowired PasseportRepository passeportRepository;
    @Autowired SituationFamilialeRepository sfRepository;
    @Autowired NationaliteRepository natRepository;

    // Test 3.1 — SituationFamilialeRepository : findAll retourne des données
    @Test
    void situationFamiliale_findAll_nonVide() {
        // Les données sont insérées par le script SQL (data.sql ou @Sql)
        // Si H2 sans data.sql, insérer manuellement
        SituationFamiliale sf = new SituationFamiliale();
        sf.setLibelle("Celibataire");
        sfRepository.save(sf);
        assertFalse(sfRepository.findAll().isEmpty());
    }

    // Test 3.2 — NationaliteRepository : findByLibelle retourne la bonne entité
    @Test
    void nationalite_findByLibelle_OK() {
        Nationalite nat = new Nationalite();
        nat.setLibelle("Malagasy");
        natRepository.save(nat);
        Optional<Nationalite> found = natRepository.findByLibelle("Malagasy");
        assertTrue(found.isPresent());
        assertEquals("Malagasy", found.get().getLibelle());
    }

    // Test 3.3 — DemandeurRepository : save puis findById OK
    @Test
    void demandeur_save_findById_OK() {
        Demandeur d = buildDemandeurEntity();
        Demandeur saved = demandeurRepository.save(d);
        assertNotNull(saved.getId());
        assertTrue(demandeurRepository.findById(saved.getId()).isPresent());
    }

    // Test 3.4 — DemandeurRepository : findByNomAndDateNaissanceAndNationaliteId → présent
    @Test
    void demandeur_findByDeduplication_present() {
        Demandeur d = buildDemandeurEntity();  // nom="Rakoto", dateNaissance=1990-01-01, nationaliteId=1
        demandeurRepository.save(d);

        Optional<Demandeur> found = demandeurRepository
            .findByNomAndDateNaissanceAndNationaliteId("Rakoto", LocalDate.of(1990, 1, 1), 1L);
        assertTrue(found.isPresent());
    }

    // Test 3.5 — DemandeurRepository : findByNomAndDateNaissanceAndNationaliteId → absent si inconnu
    @Test
    void demandeur_findByDeduplication_absent() {
        Optional<Demandeur> found = demandeurRepository
            .findByNomAndDateNaissanceAndNationaliteId("INCONNU", LocalDate.of(2000, 1, 1), 99L);
        assertFalse(found.isPresent());
    }

    // Test 3.6 — DemandeurRepository : findByNomContainingIgnoreCase retourne les bons résultats
    @Test
    void demandeur_findByNomPartiel_OK() {
        Demandeur d = buildDemandeurEntity();
        d.setNom("Rakotomalala");
        demandeurRepository.save(d);

        List<Demandeur> results = demandeurRepository.findByNomContainingIgnoreCase("rakoto");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().anyMatch(r -> r.getNom().equalsIgnoreCase("Rakotomalala")));
    }

    // Test 3.7 — PasseportRepository : findByNumero retourne vide si absent
    @Test
    void passeport_findByNumero_vide_siAbsent() {
        Optional<Passeport> result = passeportRepository.findByNumero("INCONNU-999");
        assertFalse(result.isPresent());
    }

    // Test 3.8 — PasseportRepository : save puis findByNumero OK
    @Test
    void passeport_save_findByNumero_OK() {
        Demandeur d = demandeurRepository.save(buildDemandeurEntity());
        Passeport p = buildPasseportEntity(d, "PP-001");
        passeportRepository.save(p);

        Optional<Passeport> found = passeportRepository.findByNumero("PP-001");
        assertTrue(found.isPresent());
        assertEquals("PP-001", found.get().getNumero());
    }

    // Test 3.9 — PasseportRepository : existsByNumero true si présent
    @Test
    void passeport_existsByNumero_OK() {
        Demandeur d = demandeurRepository.save(buildDemandeurEntity());
        passeportRepository.save(buildPasseportEntity(d, "PP-002"));
        assertTrue(passeportRepository.existsByNumero("PP-002"));
        assertFalse(passeportRepository.existsByNumero("PP-INEXISTANT"));
    }

    // Test 3.10 — PasseportRepository : passeports non expirés d'un demandeur
    @Test
    void passeport_findNonExpire_OK() {
        Demandeur d = demandeurRepository.save(buildDemandeurEntity());
        Passeport p = buildPasseportEntity(d, "PP-003");
        p.setDateExpiration(LocalDate.now().plusYears(5));  // non expiré
        passeportRepository.save(p);

        List<Passeport> nonExpires = passeportRepository
            .findByDemandeurIdAndDateExpirationAfter(d.getId(), LocalDate.now());
        assertFalse(nonExpires.isEmpty());
    }
}
```

---

---

# ÉTAPE 4 — MAPPERS

---

## 4.1 — `DemandeurMapper.java`

**Fichier** : `src/main/java/com/projet/visa/mapper/DemandeurMapper.java`

```java
@Component
public class DemandeurMapper {

    /**
     * Convertit un DemandeurDTO en entité Demandeur.
     * Les entités SituationFamiliale et Nationalite sont passées résolues
     * (récupérées par les repositories dans le service).
     *
     * @param dto                   données du formulaire
     * @param situationFamiliale    entité résolue (peut être null si non fourni)
     * @param nationalite           entité résolue (non null)
     * @return                      entité Demandeur (non persistée)
     */
    public Demandeur toEntity(
            DemandeurDTO dto,
            SituationFamiliale situationFamiliale,
            Nationalite nationalite) {

        Demandeur entity = new Demandeur();
        entity.setNom(dto.getNom());
        entity.setPrenom(dto.getPrenom());
        entity.setNomJeuneFille(dto.getNomJeuneFille());
        entity.setDateNaissance(dto.getDateNaissance());
        entity.setLieuNaissance(dto.getLieuNaissance());
        entity.setSituationFamiliale(situationFamiliale);
        entity.setNationalite(nationalite);
        entity.setAdresseMadagascar(dto.getAdresseMadagascar());
        entity.setTelephone(dto.getTelephone());
        entity.setEmail(dto.getEmail());
        return entity;
    }

    /**
     * Convertit une entité Demandeur en DTO de réponse (affichage).
     *
     * @param entity    entité persistée avec relations chargées
     * @return          DemandeurResponseDTO prêt pour la vue
     */
    public DemandeurResponseDTO toResponseDTO(Demandeur entity) {
        DemandeurResponseDTO dto = new DemandeurResponseDTO();
        dto.setId(entity.getId());
        dto.setNom(entity.getNom());
        dto.setPrenom(entity.getPrenom());
        dto.setNomJeuneFille(entity.getNomJeuneFille());
        dto.setDateNaissance(entity.getDateNaissance());
        dto.setLieuNaissance(entity.getLieuNaissance());
        dto.setSituationFamiliale(
            entity.getSituationFamiliale() != null
                ? entity.getSituationFamiliale().getLibelle()
                : null
        );
        dto.setNationalite(entity.getNationalite().getLibelle());
        dto.setAdresseMadagascar(entity.getAdresseMadagascar());
        dto.setTelephone(entity.getTelephone());
        dto.setEmail(entity.getEmail());
        return dto;
    }
}
```

---

## 4.2 — `PasseportMapper.java`

**Fichier** : `src/main/java/com/projet/visa/mapper/PasseportMapper.java`

```java
@Component
public class PasseportMapper {

    /**
     * Convertit un PasseportDTO en entité Passeport.
     * Le Demandeur est passé résolu depuis le service.
     *
     * @param dto           données du formulaire
     * @param demandeur     entité Demandeur résolue (non null)
     * @return              entité Passeport (non persistée)
     */
    public Passeport toEntity(PasseportDTO dto, Demandeur demandeur) {
        Passeport entity = new Passeport();
        entity.setNumero(dto.getNumero());
        entity.setDateDelivrance(dto.getDateDelivrance());
        entity.setDateExpiration(dto.getDateExpiration());
        entity.setDemandeur(demandeur);
        return entity;
    }

    /**
     * Convertit une entité Passeport en DTO de lecture.
     *
     * @param entity    entité persistée
     * @return          PasseportDTO de lecture
     */
    public PasseportDTO toDTO(Passeport entity) {
        PasseportDTO dto = new PasseportDTO();
        dto.setNumero(entity.getNumero());
        dto.setDateDelivrance(entity.getDateDelivrance());
        dto.setDateExpiration(entity.getDateExpiration());
        return dto;
    }
}
```

---

### ✅ TEST ÉTAPE 4 — `MapperTest.java`

```java
// Test 4.1 — DemandeurMapper.toEntity() : tous les champs mappés, id = null
DemandeurDTO dto = buildDemandeurDTOValide();
SituationFamiliale sf = buildSituationFamiliale("Marie");
Nationalite nat = buildNationalite("Malagasy");

Demandeur entity = demandeurMapper.toEntity(dto, sf, nat);

assertEquals(dto.getNom(), entity.getNom());
assertEquals(dto.getDateNaissance(), entity.getDateNaissance());
assertEquals(dto.getTelephone(), entity.getTelephone());
assertEquals(sf, entity.getSituationFamiliale());
assertEquals(nat, entity.getNationalite());
assertNull(entity.getId());   // pas encore persistée

// Test 4.2 — DemandeurMapper.toEntity() : situationFamiliale null → entité sans SF (champ F)
entity = demandeurMapper.toEntity(dto, null, nat);
assertNull(entity.getSituationFamiliale());

// Test 4.3 — DemandeurMapper.toResponseDTO() : libellés extraits correctement
Demandeur demandeur = buildDemandeurEntityAvecRelations();
DemandeurResponseDTO response = demandeurMapper.toResponseDTO(demandeur);
assertNotNull(response.getId());
assertEquals("Malagasy", response.getNationalite());
assertNotNull(response.getNom());

// Test 4.4 — DemandeurMapper.toResponseDTO() : situationFamiliale null → champ null dans DTO
demandeur.setSituationFamiliale(null);
response = demandeurMapper.toResponseDTO(demandeur);
assertNull(response.getSituationFamiliale());

// Test 4.5 — PasseportMapper.toEntity() : tous les champs mappés, demandeur lié
PasseportDTO pDto = buildPasseportDTOValide();
Demandeur dem = buildDemandeurEntityAvecRelations();
Passeport passeport = passeportMapper.toEntity(pDto, dem);

assertEquals(pDto.getNumero(), passeport.getNumero());
assertEquals(pDto.getDateDelivrance(), passeport.getDateDelivrance());
assertEquals(dem, passeport.getDemandeur());
assertNull(passeport.getId());

// Test 4.6 — PasseportMapper.toDTO() : round-trip numero inchangé
Passeport p = buildPasseportEntity(dem, "PP-MAPPER-TEST");
PasseportDTO pResult = passeportMapper.toDTO(p);
assertEquals("PP-MAPPER-TEST", pResult.getNumero());
```

---

---

# ÉTAPE 5 — SERVICES

---

## 5.1 — `DemandeurService.java`

**Fichier** : `src/main/java/com/projet/visa/service/DemandeurService.java`

```java
@Service
@Transactional
public class DemandeurService {

    private final DemandeurRepository demandeurRepository;
    private final SituationFamilialeRepository situationFamilialeRepository;
    private final NationaliteRepository nationaliteRepository;
    private final DemandeurMapper demandeurMapper;

    // ── Constructeur avec injection ──────────────────────────────

    // ════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE : creerOuRecuperer()
    //  📤 LIVRAISON DEV2 — signature immuable
    // ════════════════════════════════════════════════════════

    /**
     * Crée un nouveau demandeur ou récupère l'existant si déjà en base.
     *
     * Critère de déduplication (RG-05) :
     *   nom + dateNaissance + nationaliteId → identifiant métier unique
     *
     * Étapes internes :
     *   1. Vérifier si demandeur existant (déduplication)
     *   2. Si existant → retourner l'entité existante
     *   3. Si nouveau → valider les règles métier puis créer
     *
     * @param dto   données état civil du formulaire
     * @return      entité Demandeur persistée (nouvelle ou existante)
     * @throws BusinessException si règle métier violée
     * @throws ResourceNotFoundException si nationalité ou situation familiale introuvable
     */
    public Demandeur creerOuRecuperer(DemandeurDTO dto) {

        // ÉTAPE 1 — Vérifier déduplication (RG-05)
        Optional<Demandeur> existant = demandeurRepository
            .findByNomAndDateNaissanceAndNationaliteId(
                dto.getNom(),
                dto.getDateNaissance(),
                dto.getIdNationalite()
            );

        if (existant.isPresent()) {
            // ÉTAPE 2 — Retourner le demandeur existant sans modification
            return existant.get();
        }

        // ÉTAPE 3 — Nouveau demandeur : valider puis créer

        // Vérifier dateNaissance dans le passé (RG-02)
        if (!dto.getDateNaissance().isBefore(LocalDate.now())) {
            throw new BusinessException("La date de naissance doit être dans le passé.");
        }

        // Résoudre Nationalite (obligatoire)
        Nationalite nationalite = nationaliteRepository.findById(dto.getIdNationalite())
            .orElseThrow(() -> new ResourceNotFoundException(
                "Nationalité introuvable : id=" + dto.getIdNationalite()));

        // Résoudre SituationFamiliale (facultatif)
        SituationFamiliale situationFamiliale = null;
        if (dto.getIdSituationFamiliale() != null) {
            situationFamiliale = situationFamilialeRepository
                .findById(dto.getIdSituationFamiliale())
                .orElseThrow(() -> new ResourceNotFoundException(
                    "Situation familiale introuvable : id=" + dto.getIdSituationFamiliale()));
        }

        // Mapper et sauvegarder
        Demandeur entity = demandeurMapper.toEntity(dto, situationFamiliale, nationalite);
        return demandeurRepository.save(entity);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE : getDemandeur()
    // ════════════════════════════════════════════════════════

    /**
     * Récupère un demandeur par son identifiant.
     *
     * @param id    identifiant du demandeur
     * @return      DemandeurResponseDTO
     * @throws ResourceNotFoundException si demandeur absent
     */
    public DemandeurResponseDTO getDemandeur(Long id) {
        Demandeur demandeur = demandeurRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Demandeur introuvable : id=" + id));
        return demandeurMapper.toResponseDTO(demandeur);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE : rechercherParNom()
    // ════════════════════════════════════════════════════════

    /**
     * Recherche des demandeurs par nom (partiel, insensible à la casse).
     * Utilisé pour la page de recherche d'un demandeur existant.
     *
     * @param nom   nom ou fragment de nom à rechercher
     * @return      liste de DemandeurResponseDTO correspondants
     */
    public List<DemandeurResponseDTO> rechercherParNom(String nom) {
        if (nom == null || nom.isBlank()) {
            return Collections.emptyList();
        }
        return demandeurRepository.findByNomContainingIgnoreCase(nom.trim())
            .stream()
            .map(demandeurMapper::toResponseDTO)
            .collect(Collectors.toList());
    }
}
```

---

### ✅ TEST ÉTAPE 5.1 — `DemandeurServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DemandeurServiceTest {

    @Mock DemandeurRepository demandeurRepository;
    @Mock SituationFamilialeRepository situationFamilialeRepository;
    @Mock NationaliteRepository nationaliteRepository;
    @Mock DemandeurMapper demandeurMapper;
    @InjectMocks DemandeurService demandeurService;

    // ── Test 5.1.1 — Création OK : demandeur nouveau, données valides ──
    @Test
    void creerOuRecuperer_creation_OK() {
        DemandeurDTO dto = buildDemandeurDTOValide();
        Nationalite nat = buildNationalite("Malagasy");
        SituationFamiliale sf = buildSituationFamiliale("Celibataire");

        when(demandeurRepository.findByNomAndDateNaissanceAndNationaliteId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(nationaliteRepository.findById(dto.getIdNationalite()))
            .thenReturn(Optional.of(nat));
        when(situationFamilialeRepository.findById(dto.getIdSituationFamiliale()))
            .thenReturn(Optional.of(sf));
        when(demandeurMapper.toEntity(dto, sf, nat)).thenReturn(new Demandeur());
        when(demandeurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Demandeur result = demandeurService.creerOuRecuperer(dto);

        assertNotNull(result);
        verify(demandeurRepository, times(1)).save(any());
    }

    // ── Test 5.1.2 — RG-05 : demandeur existant → retourné sans création ──
    @Test
    void creerOuRecuperer_existant_retourneSansCreation() {
        DemandeurDTO dto = buildDemandeurDTOValide();
        Demandeur existant = buildDemandeurEntityAvecRelations();

        when(demandeurRepository.findByNomAndDateNaissanceAndNationaliteId(any(), any(), any()))
            .thenReturn(Optional.of(existant));

        Demandeur result = demandeurService.creerOuRecuperer(dto);

        assertEquals(existant, result);
        verify(demandeurRepository, never()).save(any());
        verify(nationaliteRepository, never()).findById(any());
    }

    // ── Test 5.1.3 — RG-02 : dateNaissance dans le futur → BusinessException ──
    @Test
    void creerOuRecuperer_dateNaissanceFutur_throwsException() {
        DemandeurDTO dto = buildDemandeurDTOValide();
        dto.setDateNaissance(LocalDate.now().plusDays(1));
        when(demandeurRepository.findByNomAndDateNaissanceAndNationaliteId(any(), any(), any()))
            .thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> demandeurService.creerOuRecuperer(dto));
        verify(demandeurRepository, never()).save(any());
    }

    // ── Test 5.1.4 — Nationalité introuvable → ResourceNotFoundException ──
    @Test
    void creerOuRecuperer_nationaliteInconnue_throwsException() {
        DemandeurDTO dto = buildDemandeurDTOValide();
        when(demandeurRepository.findByNomAndDateNaissanceAndNationaliteId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(nationaliteRepository.findById(dto.getIdNationalite()))
            .thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> demandeurService.creerOuRecuperer(dto));
    }

    // ── Test 5.1.5 — SituationFamiliale null : création sans SF → OK ──
    @Test
    void creerOuRecuperer_sansSituationFamiliale_OK() {
        DemandeurDTO dto = buildDemandeurDTOValide();
        dto.setIdSituationFamiliale(null);   // facultatif
        Nationalite nat = buildNationalite("Malagasy");

        when(demandeurRepository.findByNomAndDateNaissanceAndNationaliteId(any(), any(), any()))
            .thenReturn(Optional.empty());
        when(nationaliteRepository.findById(dto.getIdNationalite()))
            .thenReturn(Optional.of(nat));
        when(demandeurMapper.toEntity(dto, null, nat)).thenReturn(new Demandeur());
        when(demandeurRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Demandeur result = demandeurService.creerOuRecuperer(dto);
        assertNotNull(result);
        verify(situationFamilialeRepository, never()).findById(any());  // pas appelé si null
    }

    // ── Test 5.1.6 — getDemandeur : existant → DTO retourné ──
    @Test
    void getDemandeur_existant_retourneDTO() {
        Demandeur d = buildDemandeurEntityAvecRelations();
        d.setId(1L);
        DemandeurResponseDTO responseDTO = new DemandeurResponseDTO();
        when(demandeurRepository.findById(1L)).thenReturn(Optional.of(d));
        when(demandeurMapper.toResponseDTO(d)).thenReturn(responseDTO);

        DemandeurResponseDTO result = demandeurService.getDemandeur(1L);
        assertNotNull(result);
    }

    // ── Test 5.1.7 — getDemandeur : introuvable → ResourceNotFoundException ──
    @Test
    void getDemandeur_introuvable_throwsException() {
        when(demandeurRepository.findById(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> demandeurService.getDemandeur(999L));
    }

    // ── Test 5.1.8 — rechercherParNom : nom vide → liste vide ──
    @Test
    void rechercherParNom_nomVide_retourneListeVide() {
        List<DemandeurResponseDTO> result = demandeurService.rechercherParNom("  ");
        assertTrue(result.isEmpty());
        verify(demandeurRepository, never()).findByNomContainingIgnoreCase(any());
    }

    // ── Test 5.1.9 — rechercherParNom : résultats trouvés ──
    @Test
    void rechercherParNom_resultats_OK() {
        Demandeur d = buildDemandeurEntityAvecRelations();
        when(demandeurRepository.findByNomContainingIgnoreCase("rakoto"))
            .thenReturn(List.of(d));
        when(demandeurMapper.toResponseDTO(d)).thenReturn(new DemandeurResponseDTO());

        List<DemandeurResponseDTO> result = demandeurService.rechercherParNom("rakoto");
        assertEquals(1, result.size());
    }
}
```

---

## 5.2 — `PasseportService.java`

**Fichier** : `src/main/java/com/projet/visa/service/PasseportService.java`

```java
@Service
@Transactional
public class PasseportService {

    private final PasseportRepository passeportRepository;
    private final PasseportMapper passeportMapper;

    // ── Constructeur avec injection ──────────────────────────────

    // ════════════════════════════════════════════════════════
    //  MÉTHODE PRINCIPALE : creer()
    //  📤 LIVRAISON DEV2 — signature immuable
    // ════════════════════════════════════════════════════════

    /**
     * Crée et persiste un nouveau Passeport lié à un Demandeur.
     *
     * Règles appliquées :
     *   RG-06 : numéro doit être unique
     *   RG-07 : dateExpiration > dateDelivrance
     *   RG-08 : dateExpiration dans le futur
     *   RG-09 : demandeur n'a pas déjà un passeport actif (non expiré)
     *   RG-10 : demandeur non null
     *
     * @param dto           données passeport du formulaire
     * @param demandeur     entité Demandeur résolue (non null)
     * @return              entité Passeport persistée
     * @throws BusinessException si une règle métier est violée
     */
    public Passeport creer(PasseportDTO dto, Demandeur demandeur) {

        // RG-10 : demandeur obligatoire
        if (demandeur == null) {
            throw new BusinessException("Le demandeur est obligatoire pour créer un passeport.");
        }

        // RG-06 : vérifier unicité du numéro
        if (passeportRepository.existsByNumero(dto.getNumero())) {
            throw new BusinessException(
                "Le numéro de passeport '" + dto.getNumero() + "' est déjà utilisé.");
        }

        // RG-07 : dateExpiration > dateDelivrance
        if (!dto.getDateExpiration().isAfter(dto.getDateDelivrance())) {
            throw new BusinessException(
                "La date d'expiration doit être postérieure à la date de délivrance.");
        }

        // RG-08 : dateExpiration dans le futur
        if (!dto.getDateExpiration().isAfter(LocalDate.now())) {
            throw new BusinessException("Le passeport est expiré. Veuillez fournir un passeport valide.");
        }

        // RG-09 : demandeur n'a pas déjà un passeport actif
        List<Passeport> passeportsActifs = passeportRepository
            .findByDemandeurIdAndDateExpirationAfter(demandeur.getId(), LocalDate.now());
        if (!passeportsActifs.isEmpty()) {
            throw new BusinessException(
                "Ce demandeur possède déjà un passeport actif (non expiré).");
        }

        // Mapper et sauvegarder
        Passeport entity = passeportMapper.toEntity(dto, demandeur);
        return passeportRepository.save(entity);
    }

    // ════════════════════════════════════════════════════════
    //  MÉTHODE : getPasseportParDemandeur()
    // ════════════════════════════════════════════════════════

    /**
     * Récupère le passeport d'un demandeur par son id.
     *
     * @param demandeurId   id du demandeur
     * @return              PasseportDTO
     * @throws ResourceNotFoundException si aucun passeport trouvé
     */
    public PasseportDTO getPasseportParDemandeur(Long demandeurId) {
        Passeport passeport = passeportRepository.findByDemandeurId(demandeurId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Aucun passeport trouvé pour le demandeur id=" + demandeurId));
        return passeportMapper.toDTO(passeport);
    }
}
```

---

### ✅ TEST ÉTAPE 5.2 — `PasseportServiceTest.java`

```java
@ExtendWith(MockitoExtension.class)
class PasseportServiceTest {

    @Mock PasseportRepository passeportRepository;
    @Mock PasseportMapper passeportMapper;
    @InjectMocks PasseportService passeportService;

    // ── Test 5.2.1 — Création OK : toutes les règles respectées ──
    @Test
    void creer_OK() {
        PasseportDTO dto = buildPasseportDTOValide("PP-OK");
        Demandeur demandeur = buildDemandeurEntityAvecRelations();
        demandeur.setId(1L);

        when(passeportRepository.existsByNumero("PP-OK")).thenReturn(false);
        when(passeportRepository.findByDemandeurIdAndDateExpirationAfter(eq(1L), any()))
            .thenReturn(Collections.emptyList());
        when(passeportMapper.toEntity(dto, demandeur)).thenReturn(new Passeport());
        when(passeportRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        Passeport result = passeportService.creer(dto, demandeur);
        assertNotNull(result);
        verify(passeportRepository, times(1)).save(any());
    }

    // ── Test 5.2.2 — RG-06 : numéro dupliqué → BusinessException ──
    @Test
    void creer_numeroDuplique_throwsException() {
        PasseportDTO dto = buildPasseportDTOValide("PP-DUP");
        when(passeportRepository.existsByNumero("PP-DUP")).thenReturn(true);

        assertThrows(BusinessException.class,
            () -> passeportService.creer(dto, buildDemandeurEntityAvecRelations()));
        verify(passeportRepository, never()).save(any());
    }

    // ── Test 5.2.3 — RG-07 : dateExpiration <= dateDelivrance → BusinessException ──
    @Test
    void creer_datesIncoherentes_throwsException() {
        PasseportDTO dto = buildPasseportDTOValide("PP-DATE");
        dto.setDateDelivrance(LocalDate.of(2025, 6, 1));
        dto.setDateExpiration(LocalDate.of(2025, 5, 1));  // avant délivrance
        when(passeportRepository.existsByNumero(any())).thenReturn(false);

        assertThrows(BusinessException.class,
            () -> passeportService.creer(dto, buildDemandeurEntityAvecRelations()));
    }

    // ── Test 5.2.4 — RG-08 : dateExpiration dans le passé → BusinessException ──
    @Test
    void creer_passeportExpire_throwsException() {
        PasseportDTO dto = buildPasseportDTOValide("PP-EXP");
        dto.setDateDelivrance(LocalDate.of(2010, 1, 1));
        dto.setDateExpiration(LocalDate.of(2015, 1, 1));  // expiré
        when(passeportRepository.existsByNumero(any())).thenReturn(false);

        assertThrows(BusinessException.class,
            () -> passeportService.creer(dto, buildDemandeurEntityAvecRelations()));
    }

    // ── Test 5.2.5 — RG-09 : demandeur a déjà un passeport actif → BusinessException ──
    @Test
    void creer_demandeurDejaPasseportActif_throwsException() {
        PasseportDTO dto = buildPasseportDTOValide("PP-ACTIF");
        Demandeur demandeur = buildDemandeurEntityAvecRelations();
        demandeur.setId(1L);

        when(passeportRepository.existsByNumero(any())).thenReturn(false);
        when(passeportRepository.findByDemandeurIdAndDateExpirationAfter(eq(1L), any()))
            .thenReturn(List.of(new Passeport()));  // passeport actif existant

        assertThrows(BusinessException.class,
            () -> passeportService.creer(dto, demandeur));
        verify(passeportRepository, never()).save(any());
    }

    // ── Test 5.2.6 — RG-10 : demandeur null → BusinessException ──
    @Test
    void creer_demandeurNull_throwsException() {
        PasseportDTO dto = buildPasseportDTOValide("PP-NULL");
        assertThrows(BusinessException.class,
            () -> passeportService.creer(dto, null));
    }

    // ── Test 5.2.7 — getPasseportParDemandeur : OK ──
    @Test
    void getPasseportParDemandeur_OK() {
        Passeport p = buildPasseportEntity(buildDemandeurEntityAvecRelations(), "PP-GET");
        when(passeportRepository.findByDemandeurId(1L)).thenReturn(Optional.of(p));
        when(passeportMapper.toDTO(p)).thenReturn(new PasseportDTO());

        PasseportDTO result = passeportService.getPasseportParDemandeur(1L);
        assertNotNull(result);
    }

    // ── Test 5.2.8 — getPasseportParDemandeur : introuvable → ResourceNotFoundException ──
    @Test
    void getPasseportParDemandeur_introuvable_throwsException() {
        when(passeportRepository.findByDemandeurId(999L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
            () -> passeportService.getPasseportParDemandeur(999L));
    }
}
```

---

---

# ÉTAPE 6 — CONTROLLER

---

## 6.1 — `DemandeurController.java`

**Fichier** : `src/main/java/com/projet/visa/controller/DemandeurController.java`

**Rôle** : ce controller ne sert pas à la saisie principale (gérée par `DemandeController` de Dev2),
mais expose des endpoints utiles pour la **recherche** d'un demandeur existant et la **consultation**.

```java
@Controller
@RequestMapping("/demandeurs")
public class DemandeurController {

    private final DemandeurService demandeurService;

    // ── Constructeur avec injection ──────────────────────────────

    // ════════════════════════════════════════════════════════
    //  GET /demandeurs/recherche
    //  Page de recherche d'un demandeur existant
    // ════════════════════════════════════════════════════════
    @GetMapping("/recherche")
    public String afficherRechercheForm(
            @RequestParam(required = false) String nom,
            Model model) {

        model.addAttribute("nom", nom);
        if (nom != null && !nom.isBlank()) {
            model.addAttribute("resultats", demandeurService.rechercherParNom(nom));
        } else {
            model.addAttribute("resultats", Collections.emptyList());
        }
        return "demandeur/recherche";
    }

    // ════════════════════════════════════════════════════════
    //  GET /demandeurs/{id}
    //  Détail d'un demandeur (JSON — utile pour AJAX ou debug)
    // ════════════════════════════════════════════════════════
    @GetMapping("/{id}")
    @ResponseBody
    public ResponseEntity<DemandeurResponseDTO> getDemandeur(@PathVariable Long id) {
        DemandeurResponseDTO dto = demandeurService.getDemandeur(id);
        return ResponseEntity.ok(dto);
    }

    // ════════════════════════════════════════════════════════
    //  GET /demandeurs/recherche/api?nom=Rakoto
    //  Endpoint AJAX JSON pour autocomplétion dans le formulaire
    // ════════════════════════════════════════════════════════
    @GetMapping("/recherche/api")
    @ResponseBody
    public ResponseEntity<List<DemandeurResponseDTO>> rechercherParNomAjax(
            @RequestParam String nom) {

        List<DemandeurResponseDTO> resultats = demandeurService.rechercherParNom(nom);
        return ResponseEntity.ok(resultats);
    }
}
```

---

### ✅ TEST ÉTAPE 6 — `DemandeurControllerTest.java`

```java
@WebMvcTest(DemandeurController.class)
class DemandeurControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean DemandeurService demandeurService;

    // Test 6.1 — GET /demandeurs/recherche sans param → 200, vue "demandeur/recherche"
    @Test
    void rechercheForm_sansParam_renvoie200() throws Exception {
        mockMvc.perform(get("/demandeurs/recherche"))
               .andExpect(status().isOk())
               .andExpect(view().name("demandeur/recherche"))
               .andExpect(model().attributeExists("resultats"));
    }

    // Test 6.2 — GET /demandeurs/recherche?nom=Rakoto → appelle le service
    @Test
    void rechercheForm_avecNom_appelleService() throws Exception {
        when(demandeurService.rechercherParNom("Rakoto"))
            .thenReturn(List.of(new DemandeurResponseDTO()));

        mockMvc.perform(get("/demandeurs/recherche").param("nom", "Rakoto"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("resultats"));

        verify(demandeurService).rechercherParNom("Rakoto");
    }

    // Test 6.3 — GET /demandeurs/{id} existant → 200, JSON
    @Test
    void getDemandeur_existant_renvoieJSON() throws Exception {
        DemandeurResponseDTO dto = new DemandeurResponseDTO();
        dto.setId(1L);
        when(demandeurService.getDemandeur(1L)).thenReturn(dto);

        mockMvc.perform(get("/demandeurs/1"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Test 6.4 — GET /demandeurs/{id} introuvable → 404
    @Test
    void getDemandeur_introuvable_renvoie404() throws Exception {
        when(demandeurService.getDemandeur(999L))
            .thenThrow(new ResourceNotFoundException("Demandeur introuvable : id=999"));

        mockMvc.perform(get("/demandeurs/999"))
               .andExpect(status().isNotFound());
    }

    // Test 6.5 — GET /demandeurs/recherche/api?nom=Rak → 200, JSON liste
    @Test
    void rechercheApi_renvoieJSON() throws Exception {
        when(demandeurService.rechercherParNom("Rak"))
            .thenReturn(List.of(new DemandeurResponseDTO()));

        mockMvc.perform(get("/demandeurs/recherche/api").param("nom", "Rak"))
               .andExpect(status().isOk())
               .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    // Test 6.6 — GET /demandeurs/recherche/api?nom= → liste vide, pas d'erreur
    @Test
    void rechercheApi_nomVide_renvoieListeVide() throws Exception {
        when(demandeurService.rechercherParNom("")).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/demandeurs/recherche/api").param("nom", ""))
               .andExpect(status().isOk());
    }
}
```

---

---

# ÉTAPE 7 — TEMPLATE THYMELEAF

---

## 7.1 — `recherche.html`

**Fichier** : `src/main/resources/templates/demandeur/recherche.html`

**Rôle** : permettre de vérifier si un demandeur existe déjà avant de saisir une nouvelle demande.
Affiché dans un contexte d'administration ou de pré-remplissage du formulaire.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head>
    <title>Recherche Demandeur</title>
</head>
<body>

<h1>Rechercher un demandeur existant</h1>

<!-- Formulaire de recherche par nom -->
<form th:action="@{/demandeurs/recherche}" method="get">
    <label for="nom">Nom du demandeur</label>
    <input type="text"
           id="nom"
           name="nom"
           th:value="${nom}"
           placeholder="Ex: Rakoto..."
           autocomplete="off" />
    <button type="submit">Rechercher</button>
</form>

<!-- Résultats -->
<section id="resultats" th:if="${not #lists.isEmpty(resultats)}">
    <h2>Résultats</h2>
    <table>
        <thead>
            <tr>
                <th>Nom</th>
                <th>Prénom</th>
                <th>Date naissance</th>
                <th>Nationalité</th>
                <th>Téléphone</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="demandeur : ${resultats}">
                <td th:text="${demandeur.nom}"></td>
                <td th:text="${demandeur.prenom}"></td>
                <td th:text="${demandeur.dateNaissance}"></td>
                <td th:text="${demandeur.nationalite}"></td>
                <td th:text="${demandeur.telephone}"></td>
                <td>
                    <!-- Bouton pour utiliser ce demandeur dans une nouvelle demande -->
                    <a th:href="@{/demandes/nouvelle(demandeurId=${demandeur.id})}">
                        Utiliser ce demandeur
                    </a>
                </td>
            </tr>
        </tbody>
    </table>
</section>

<!-- Aucun résultat -->
<div th:if="${nom != null and #lists.isEmpty(resultats)}" class="alert alert-info">
    Aucun demandeur trouvé pour le nom : <strong th:text="${nom}"></strong>
</div>

<!-- Autocomplétion AJAX (optionnelle) -->
<script>
    const input = document.getElementById('nom');
    let timer;

    input.addEventListener('input', function () {
        clearTimeout(timer);
        const val = this.value.trim();
        if (val.length < 2) return;

        timer = setTimeout(() => {
            fetch('/demandeurs/recherche/api?nom=' + encodeURIComponent(val))
                .then(res => res.json())
                .then(data => {
                    // Optionnel : afficher les suggestions dans une datalist
                    console.log('Suggestions:', data);
                });
        }, 300);
    });
</script>

</body>
</html>
```

---

---

# ÉTAPE 8 — GESTION DES EXCEPTIONS

> Ces classes doivent exister dans le package `exception/`. Vérifier avec le TeamLead avant de les créer pour éviter les doublons.

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

## 8.3 — `GlobalExceptionHandler.java` (optionnel mais recommandé)

**Fichier** : `src/main/java/com/projet/visa/exception/GlobalExceptionHandler.java`

```java
@ControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Gère les ResourceNotFoundException → HTTP 404
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<String> handleNotFound(ResourceNotFoundException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(ex.getMessage());
    }

    /**
     * Gère les BusinessException → HTTP 400
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<String> handleBusiness(BusinessException ex) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ex.getMessage());
    }
}
```

---

---

# RÉCAPITULATIF DES RÈGLES DE GESTION

| Code | Règle | Implémentée dans |
|------|-------|-----------------|
| RG-01 | `nom`, `dateNaissance`, `idNationalite`, `adresseMadagascar`, `telephone` obligatoires | `DemandeurDTO` annotations + `DemandeurService` |
| RG-02 | `dateNaissance` doit être dans le passé | `DemandeurDTO` `@Past` + `DemandeurService.creerOuRecuperer()` |
| RG-03 | `telephone` format valide (7-15 chiffres) | `DemandeurDTO` `@Pattern` |
| RG-04 | `email` format valide si fourni | `DemandeurDTO` `@Email` |
| RG-05 | Pas de doublon demandeur : clé = `nom` + `dateNaissance` + `nationaliteId` | `DemandeurService.creerOuRecuperer()` |
| RG-06 | `numero` passeport unique en base | `PasseportService.creer()` + `unique = true` sur l'entité |
| RG-07 | `dateExpiration` passeport > `dateDelivrance` | `PasseportService.creer()` |
| RG-08 | `dateExpiration` passeport dans le futur (passeport non expiré) | `PasseportService.creer()` + `PasseportDTO` `@Future` |
| RG-09 | Un demandeur ne peut avoir qu'un seul passeport actif à la fois | `PasseportService.creer()` |
| RG-10 | Le `Demandeur` est obligatoire pour créer un `Passeport` | `PasseportService.creer()` |

---

---

# RÉCAPITULATIF DE TOUS LES TESTS

| # | Test | Classe | Type |
|---|------|--------|------|
| T-01 | SituationFamiliale instanciable | `EntityInstantiationTest` | Unitaire |
| T-02 | Nationalite instanciable | `EntityInstantiationTest` | Unitaire |
| T-03 | Demandeur : champs obligatoires définis | `EntityInstantiationTest` | Unitaire |
| T-04 | Demandeur : dateNaissance dans le passé | `EntityInstantiationTest` | Unitaire |
| T-05 | Passeport : dateExpiration > dateDelivrance | `EntityInstantiationTest` | Unitaire |
| T-06 | Passeport : dateExpiration dans le futur | `EntityInstantiationTest` | Unitaire |
| T-07 | DemandeurDTO : nom blank → violation | `DtoValidationTest` | Unitaire |
| T-08 | DemandeurDTO : dateNaissance futur → violation @Past | `DtoValidationTest` | Unitaire |
| T-09 | DemandeurDTO : email invalide → violation @Email | `DtoValidationTest` | Unitaire |
| T-10 | DemandeurDTO : email null → pas de violation | `DtoValidationTest` | Unitaire |
| T-11 | DemandeurDTO : téléphone invalide → violation @Pattern | `DtoValidationTest` | Unitaire |
| T-12 | PasseportDTO : numéro blank → violation | `DtoValidationTest` | Unitaire |
| T-13 | PasseportDTO : dateExpiration passée → violation @Future | `DtoValidationTest` | Unitaire |
| T-14 | PasseportDTO : dateDelivrance futur → violation @Past | `DtoValidationTest` | Unitaire |
| T-15 | DemandeurDTO complet valide → aucune violation | `DtoValidationTest` | Unitaire |
| T-16 | SituationFamilialeRepository : save + findAll non vide | `RepositoryTest` | Intégration H2 |
| T-17 | NationaliteRepository : findByLibelle OK | `RepositoryTest` | Intégration H2 |
| T-18 | DemandeurRepository : save + findById OK | `RepositoryTest` | Intégration H2 |
| T-19 | DemandeurRepository : findByDeduplication présent | `RepositoryTest` | Intégration H2 |
| T-20 | DemandeurRepository : findByDeduplication absent | `RepositoryTest` | Intégration H2 |
| T-21 | DemandeurRepository : findByNomContaining OK | `RepositoryTest` | Intégration H2 |
| T-22 | PasseportRepository : findByNumero vide si absent | `RepositoryTest` | Intégration H2 |
| T-23 | PasseportRepository : save + findByNumero OK | `RepositoryTest` | Intégration H2 |
| T-24 | PasseportRepository : existsByNumero OK | `RepositoryTest` | Intégration H2 |
| T-25 | PasseportRepository : passeports non expirés OK | `RepositoryTest` | Intégration H2 |
| T-26 | DemandeurMapper.toEntity() : champs mappés, id null | `MapperTest` | Unitaire |
| T-27 | DemandeurMapper.toEntity() : situationFamiliale null OK | `MapperTest` | Unitaire |
| T-28 | DemandeurMapper.toResponseDTO() : libellés extraits | `MapperTest` | Unitaire |
| T-29 | DemandeurMapper.toResponseDTO() : SF null → null dans DTO | `MapperTest` | Unitaire |
| T-30 | PasseportMapper.toEntity() : champs mappés, demandeur lié | `MapperTest` | Unitaire |
| T-31 | PasseportMapper.toDTO() : round-trip numéro inchangé | `MapperTest` | Unitaire |
| T-32 | DemandeurService : création OK | `DemandeurServiceTest` | Unitaire |
| T-33 | RG-05 : demandeur existant retourné sans création | `DemandeurServiceTest` | Unitaire |
| T-34 | RG-02 : dateNaissance futur → BusinessException | `DemandeurServiceTest` | Unitaire |
| T-35 | Nationalité introuvable → ResourceNotFoundException | `DemandeurServiceTest` | Unitaire |
| T-36 | DemandeurService : création sans SF → OK | `DemandeurServiceTest` | Unitaire |
| T-37 | getDemandeur existant → DTO retourné | `DemandeurServiceTest` | Unitaire |
| T-38 | getDemandeur introuvable → ResourceNotFoundException | `DemandeurServiceTest` | Unitaire |
| T-39 | rechercherParNom nom vide → liste vide | `DemandeurServiceTest` | Unitaire |
| T-40 | rechercherParNom résultats trouvés | `DemandeurServiceTest` | Unitaire |
| T-41 | PasseportService : création OK | `PasseportServiceTest` | Unitaire |
| T-42 | RG-06 : numéro dupliqué → BusinessException | `PasseportServiceTest` | Unitaire |
| T-43 | RG-07 : dates incohérentes → BusinessException | `PasseportServiceTest` | Unitaire |
| T-44 | RG-08 : passeport expiré → BusinessException | `PasseportServiceTest` | Unitaire |
| T-45 | RG-09 : demandeur a déjà un passeport actif → BusinessException | `PasseportServiceTest` | Unitaire |
| T-46 | RG-10 : demandeur null → BusinessException | `PasseportServiceTest` | Unitaire |
| T-47 | getPasseportParDemandeur OK | `PasseportServiceTest` | Unitaire |
| T-48 | getPasseportParDemandeur introuvable → ResourceNotFoundException | `PasseportServiceTest` | Unitaire |
| T-49 | GET /demandeurs/recherche sans param → 200, vue OK | `DemandeurControllerTest` | MVC |
| T-50 | GET /demandeurs/recherche?nom=X → appelle service | `DemandeurControllerTest` | MVC |
| T-51 | GET /demandeurs/{id} existant → 200, JSON | `DemandeurControllerTest` | MVC |
| T-52 | GET /demandeurs/{id} introuvable → 404 | `DemandeurControllerTest` | MVC |
| T-53 | GET /demandeurs/recherche/api?nom=X → 200, JSON liste | `DemandeurControllerTest` | MVC |
| T-54 | GET /demandeurs/recherche/api?nom= → liste vide, pas d'erreur | `DemandeurControllerTest` | MVC |

---

---

# ORDRE D'EXÉCUTION RECOMMANDÉ

```
Semaine 1
  ├─ Étape 1 : Entités  (1.1 → 1.4)          + Tests T-01 à T-06
  ├─ Étape 2 : DTOs     (2.1 → 2.3)          + Tests T-07 à T-15
  ├─ Étape 3 : Repos    (3.1 → 3.4)          + Tests T-16 à T-25
  └─ Étape 4 : Mappers  (4.1 → 4.2)          + Tests T-26 à T-31

Semaine 2
  ├─ Étape 5.1 : DemandeurService             + Tests T-32 à T-40
  ├─ Étape 5.2 : PasseportService             + Tests T-41 à T-48
  ├─ Étape 6   : DemandeurController          + Tests T-49 à T-54
  └─ Étape 7   : Template recherche.html
```

> ⚠️ **Priorité absolue** : livrer `DemandeurService.creerOuRecuperer()` et `PasseportService.creer()` en premier car Dev2 en dépend directement pour `DemandeService.creerDemande()`.
>
> ⚠️ Synchroniser avec Dev2 dès le début de la Semaine 1 pour valider les noms de champs dans `DemandeurDTO` et `PasseportDTO` — toute modification ultérieure de ces DTOs casse `DemandeCreateDTO` côté Dev2.
>
> ⚠️ Ne pas commencer l'Étape 6 (Controller) avant d'avoir tous les tests service au vert.
