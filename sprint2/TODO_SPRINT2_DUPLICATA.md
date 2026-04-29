# TODO — SPRINT 2 : DEMANDE DE DUPLICATA
## Feature : `feature/sprint2_Demande_Duplicata`

> **Convention de lecture**
> - `(O)` Obligatoire · `(F)` Facultatif · `(U)` Unique · `(H)` Hidden · `(S)` Select
> - `✅ TEST` = test unitaire à écrire immédiatement après l'étape
> - `⚠️ RG` = Règle de Gestion métier à respecter
> - `🔗 SPRINT1` = élément déjà livré en Sprint 1, à réutiliser sans modification
> - `✏️ MODIF` = fichier Sprint 1 à modifier (ajout de méthode/champ, pas de réécriture)
> - `🆕 NOUVEAU` = fichier entièrement nouveau à créer

---

## CONTEXTE MÉTIER DU SPRINT 2

Le duplicata intervient lorsqu'un résident étranger a **perdu sa carte de résident ou son visa**
et doit en obtenir un nouveau à l'identique.

**Deux cas d'usage coexistent :**

### CAS A — Sans données antérieures (personne inconnue du système)
> Le demandeur n'a jamais fait de demande dans le système.
> Il faut tout ressaisir, créer la demande initiale (type `NOUVELLE`),
> l'approuver manuellement, PUIS créer la demande de duplicata.

Flux :
```
Saisie complète (état civil + passeport + visa)
  → Création Demande type=NOUVELLE, statut=CREE
  → Approbation manuelle → statut=APPROUVEE
  → Création Demande type=DUPLICATA (référence à la demande APPROUVEE)
```

### CAS B — Avec données antérieures (personne déjà connue du système)
> Le demandeur a déjà une demande `APPROUVEE` en base.
> On retrouve directement son dossier et on crée uniquement la demande duplicata.

Flux :
```
Recherche demandeur existant (par nom/passeport)
  → Sélection de la demande APPROUVEE à dupliquer
  → Création Demande type=DUPLICATA (référence à la demande APPROUVEE)
```

⚠️ **RG FONDAMENTALE** : **seule une demande avec statut `APPROUVEE` peut générer un duplicata.**
Une demande `CREE`, `TERMINEE` ou `REFUSEE` ne peut pas être dupliquée.

---

## CE QUI EXISTE DÉJÀ EN SPRINT 1 (à réutiliser)

| Élément Sprint 1 | Statut | Usage Sprint 2 |
|---|---|---|
| `Demandeur.java` | 🔗 intact | réutilisé tel quel |
| `Passeport.java` | 🔗 intact | réutilisé tel quel |
| `VisaTransformable.java` | 🔗 intact | réutilisé tel quel |
| `Demande.java` | ✏️ à modifier | ajouter champ `demandeOrigine` |
| `StatutDemande.java` | 🔗 intact | valeurs : CREE, APPROUVEE, TERMINEE, REFUSEE |
| `TypeDemande.java` | 🔗 intact | ajouter `DUPLICATA` si absent |
| `DemandeurService.creerOuRecuperer()` | 🔗 intact | appelé dans Cas A |
| `PasseportService.creer()` | 🔗 intact | appelé dans Cas A |
| `VisaTransformableService.creer()` | 🔗 intact | appelé dans Cas A |
| `DemandeService.creerDemande()` | ✏️ à modifier | ajout méthode `creerDuplicata()` |
| `DemandeRepository` | ✏️ à modifier | ajout méthode de recherche par statut + demandeur |
| `DemandeurRepository` | 🔗 intact | recherche par nom |
| `DemandeController` | ✏️ à modifier | ajout endpoints duplicata |
| `DemandeCreateDTO` | 🔗 intact | réutilisé pour Cas A (saisie complète) |

---

## MODIFICATIONS BASE DE DONNÉES

### Script SQL à exécuter (migration Sprint 2)

```sql
-- ─────────────────────────────────────────────────────────────
-- MIGRATION SPRINT 2 : support du duplicata
-- ─────────────────────────────────────────────────────────────

-- 1. Ajouter type_demande DUPLICATA si absent
INSERT INTO type_demande (libelle)
SELECT 'DUPLICATA'
WHERE NOT EXISTS (
    SELECT 1 FROM type_demande WHERE libelle = 'DUPLICATA'
);

-- 2. Ajouter la colonne de référence à la demande d'origine
--    (self-join : une demande DUPLICATA pointe vers une demande APPROUVEE)
ALTER TABLE demande
    ADD COLUMN id_demande_origine INT NULL,
    ADD CONSTRAINT fk_demande_origine
        FOREIGN KEY (id_demande_origine) REFERENCES demande(id);

-- 3. Ajouter statut REFUSEE si absent (utile pour les workflows futurs)
INSERT INTO statut_demande (libelle)
SELECT 'REFUSEE'
WHERE NOT EXISTS (
    SELECT 1 FROM statut_demande WHERE libelle = 'REFUSEE'
);

-- Vérification post-migration
-- SELECT * FROM type_demande;      → doit contenir NOUVELLE, DUPLICATA
-- SELECT * FROM statut_demande;    → doit contenir CREE, APPROUVEE, TERMINEE, REFUSEE
-- SELECT column_name FROM information_schema.columns
--   WHERE table_name = 'demande';  → doit contenir id_demande_origine
```

---

## STRUCTURE DES FICHIERS SPRINT 2

```
src/main/java/com/projet/visa/
├── entity/
│   └── Demande.java                          ✏️ MODIF  — ajouter champ demandeOrigine
├── dto/
│   ├── DuplicataCreateDTO.java               🆕 NOUVEAU
│   ├── DuplicataResponseDTO.java             🆕 NOUVEAU
│   ├── DemandeRechercheDTO.java              🆕 NOUVEAU (recherche demande approuvée)
│   └── DemandeResumeeDTO.java                🆕 NOUVEAU (aperçu d'une demande dans la liste)
├── mapper/
│   └── DemandeMapper.java                    ✏️ MODIF  — ajouter toDuplicataResponseDTO()
├── repository/
│   └── DemandeRepository.java                ✏️ MODIF  — ajouter méthodes de recherche
├── service/
│   └── DemandeService.java                   ✏️ MODIF  — ajouter creerDuplicata(), approuverDemande()
└── controller/
    └── DemandeController.java                ✏️ MODIF  — ajouter endpoints duplicata

src/main/resources/templates/
├── duplicata/
│   ├── choix-cas.html                        🆕 NOUVEAU (point d'entrée : Cas A ou Cas B)
│   ├── recherche-demande.html                🆕 NOUVEAU (Cas B : chercher demande APPROUVEE)
│   ├── formulaire-cas-a.html                 🆕 NOUVEAU (Cas A : saisie complète)
│   ├── formulaire-cas-b.html                 🆕 NOUVEAU (Cas B : saisie légère)
│   └── confirmation-duplicata.html           🆕 NOUVEAU
└── demande/
    └── formulaire.html                       ✏️ MODIF  — ajouter bouton "Approuver" (action manuelle)

src/test/java/com/projet/visa/
├── service/
│   └── DemandeServiceDuplicataTest.java      🆕 NOUVEAU
└── controller/
    └── DuplicataControllerTest.java          🆕 NOUVEAU
```

---

---

# ÉTAPE 1 — MODIFICATION ENTITÉ `Demande.java`

**Fichier** : `src/main/java/com/projet/visa/entity/Demande.java`
**Action** : ✏️ MODIF — ajouter uniquement le champ `demandeOrigine`

### Champ à ajouter dans la classe existante

```java
// ── À AJOUTER dans Demande.java ──────────────────────────────
// Référence à la demande APPROUVEE d'origine (null pour les demandes NOUVELLE)
// Renseigné uniquement quand typeDemande = DUPLICATA

@ManyToOne
@JoinColumn(name = "id_demande_origine", nullable = true)
private Demande demandeOrigine;

// getter / setter à ajouter
public Demande getDemandeOrigine() { return demandeOrigine; }
public void setDemandeOrigine(Demande demandeOrigine) { this.demandeOrigine = demandeOrigine; }
```

⚠️ **RG-01** : `demandeOrigine` est `null` pour toute demande de type `NOUVELLE`
⚠️ **RG-02** : `demandeOrigine` est **obligatoire et non null** pour toute demande de type `DUPLICATA`
⚠️ **RG-03** : `demandeOrigine.statutDemande` doit être `APPROUVEE` — toute autre valeur → `BusinessException`

---

### ✅ TEST ÉTAPE 1 — `DemandeEntityModifTest.java`

```java
// Test 1.1 — Demande NOUVELLE : demandeOrigine = null (RG-01)
Demande nouvelle = new Demande();
nouvelle.setDemandeOrigine(null);
assertNull(nouvelle.getDemandeOrigine());

// Test 1.2 — Demande DUPLICATA : demandeOrigine non null (RG-02)
Demande origine = new Demande();
origine.setId(1L);
Demande duplicata = new Demande();
duplicata.setDemandeOrigine(origine);
assertNotNull(duplicata.getDemandeOrigine());
assertEquals(1L, duplicata.getDemandeOrigine().getId());

// Test 1.3 — Vérification que l'ancienne structure fonctionne toujours
//            (pas de régression sur les champs Sprint 1)
Demande d = new Demande();
d.setDateDemande(LocalDateTime.now());
d.setDemandePieces(new ArrayList<>());
assertNotNull(d.getDemandePieces());
assertNotNull(d.getDateDemande());
```

---

---

# ÉTAPE 2 — MODIFICATION `TypeDemande` EN BASE

**Action** : vérifier que la valeur `DUPLICATA` est bien présente dans la table `type_demande`.
Le script SQL ci-dessus l'insère. Côté Java, l'entité `TypeDemande.java` (TeamLead) ne change pas.

**Vérification dans les services** : résoudre `TypeDemande` par libellé `"DUPLICATA"` via :

```java
// Dans DemandeService — même pattern que pour "NOUVELLE" en Sprint 1
TypeDemande typeDuplicata = typeDemandeRepository.findByLibelle("DUPLICATA")
    .orElseThrow(() -> new ResourceNotFoundException("TypeDemande DUPLICATA introuvable en base"));
```

---

---

# ÉTAPE 3 — DTOs

---

## 3.1 — `DuplicataCreateDTO.java` 🆕 NOUVEAU

**Fichier** : `src/main/java/com/projet/visa/dto/DuplicataCreateDTO.java`

> Ce DTO sert pour les **deux cas** (A et B).
> En Cas A, `idDemandeOrigine` est résolu après approbation de la demande NOUVELLE.
> En Cas B, `idDemandeOrigine` est sélectionné directement par l'utilisateur.

```java
public class DuplicataCreateDTO {

    // ── Commun aux deux cas ──────────────────────────────────────

    @NotNull(message = "La demande d'origine est obligatoire")
    private Long idDemandeOrigine;                  // (O) — id de la demande APPROUVEE

    @NotNull(message = "La date de demande est obligatoire")
    private LocalDate dateDemande;                  // (O) — auto : LocalDate.now()

    // typeDemande  → forcé à "DUPLICATA" en service   (H)
    // statutDemande → forcé à "CREE" en service        (H)

    // ── Pièces ───────────────────────────────────────────────────
    private List<Long> piecesFournies = new ArrayList<>();
    // ids des pièces cochées dans le formulaire duplicata

    // ── Indicateur de cas (utilisé par le controller) ────────────
    // Pas envoyé par le form, résolu par l'URL ou le paramètre de route
    // "CAS_A" | "CAS_B" — uniquement pour la logique controller

    // getters / setters
}
```

---

## 3.2 — `DuplicataResponseDTO.java` 🆕 NOUVEAU

**Fichier** : `src/main/java/com/projet/visa/dto/DuplicataResponseDTO.java`

```java
public class DuplicataResponseDTO {

    private Long id;                               // id de la nouvelle demande duplicata
    private LocalDateTime dateDemande;
    private String typeDemande;                    // "DUPLICATA"
    private String statutDemande;                  // "CREE"

    // Infos du demandeur (récupérées via demandeOrigine)
    private Long idDemandeur;
    private String nomDemandeur;
    private String prenomDemandeur;

    // Infos de la demande d'origine
    private Long idDemandeOrigine;
    private LocalDateTime dateDemandeOrigine;
    private String typeVisaOrigine;                // "Travailleur" ou "Investisseur"
    private String referenceVisaOrigine;

    // Pièces liées au duplicata
    private List<DemandePieceDTO> pieces;          // 🔗 SPRINT1 — DTO existant

    // getters / setters
}
```

---

## 3.3 — `DemandeRechercheDTO.java` 🆕 NOUVEAU

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeRechercheDTO.java`

> Critères de recherche pour trouver une demande APPROUVEE existante (Cas B).

```java
public class DemandeRechercheDTO {

    private String nomDemandeur;           // (F) recherche par nom
    private String numeroPasSeport;        // (F) recherche par numéro passeport
    private String referenceVisa;          // (F) recherche par référence visa

    // Au moins un champ doit être renseigné → vérification en service
    // getters / setters
}
```

---

## 3.4 — `DemandeResumeeDTO.java` 🆕 NOUVEAU

**Fichier** : `src/main/java/com/projet/visa/dto/DemandeResumeeDTO.java`

> DTO léger pour afficher la liste des demandes APPROUVEES dans le select (Cas B).

```java
public class DemandeResumeeDTO {

    private Long id;
    private LocalDateTime dateDemande;
    private String nomDemandeur;
    private String prenomDemandeur;
    private String typeVisa;               // "Travailleur" / "Investisseur"
    private String referenceVisa;
    private String statutDemande;          // toujours "APPROUVEE" dans ce contexte
    private String numeroPasSeport;

    // getters / setters
}
```

---

### ✅ TEST ÉTAPE 3 — `DuplicataDtoValidationTest.java`

```java
// Test 3.1 — DuplicataCreateDTO : idDemandeOrigine null → violation
DuplicataCreateDTO dto = new DuplicataCreateDTO();
dto.setIdDemandeOrigine(null);
Set<ConstraintViolation<DuplicataCreateDTO>> violations = validator.validate(dto);
assertTrue(violations.stream().anyMatch(v ->
    v.getPropertyPath().toString().equals("idDemandeOrigine")));

// Test 3.2 — DuplicataCreateDTO : piecesFournies initialisée (jamais null)
DuplicataCreateDTO dto2 = new DuplicataCreateDTO();
assertNotNull(dto2.getPiecesFournies());

// Test 3.3 — DuplicataCreateDTO valide → aucune violation
DuplicataCreateDTO dto3 = new DuplicataCreateDTO();
dto3.setIdDemandeOrigine(1L);
dto3.setDateDemande(LocalDate.now());
violations = validator.validate(dto3);
assertTrue(violations.isEmpty());

// Test 3.4 — DemandeRechercheDTO : tous les champs null → instanciable sans exception
DemandeRechercheDTO recherche = new DemandeRechercheDTO();
assertNull(recherche.getNomDemandeur());
assertNull(recherche.getNumeroPasSeport());
```

---

---

# ÉTAPE 4 — MODIFICATION `DemandeRepository.java`

**Fichier** : `src/main/java/com/projet/visa/repository/DemandeRepository.java`
**Action** : ✏️ MODIF — ajouter les méthodes de recherche nécessaires au Sprint 2

### Méthodes à ajouter à l'interface existante

```java
// ── À AJOUTER dans DemandeRepository.java ───────────────────────────────────

// Trouver toutes les demandes APPROUVEES d'un demandeur donné
// → utilisé pour lister les demandes éligibles au duplicata (Cas B)
List<Demande> findByDemandeurIdAndStatutDemandeLibelle(Long demandeurId, String libelle);

// Trouver toutes les demandes APPROUVEES (sans filtre demandeur)
// → utilisé pour la recherche globale (Cas B, recherche par passeport ou visa)
List<Demande> findByStatutDemandeLibelleAndDemandeurNomContainingIgnoreCase(
    String libelle,
    String nomDemandeur
);

// Vérifier si une demande DUPLICATA existe déjà pour une demande d'origine donnée
// → RG anti-doublon Sprint 2
boolean existsByDemandeOrigineIdAndTypeDemandeLibe(
    Long idDemandeOrigine,
    String libelleTypeDemande
);

// Trouver les demandes liées à un numéro de passeport
// → recherche Cas B par numéro passeport
@Query("SELECT d FROM Demande d " +
       "WHERE d.visaTransformable.passeport.numero = :numero " +
       "AND d.statutDemande.libelle = :statut")
List<Demande> findByPasseportNumeroAndStatut(
    @Param("numero") String numero,
    @Param("statut") String statut
);

// Trouver les demandes liées à une référence visa
// → recherche Cas B par référence visa
@Query("SELECT d FROM Demande d " +
       "WHERE d.visaTransformable.referenceVisa = :reference " +
       "AND d.statutDemande.libelle = :statut")
List<Demande> findByReferenceVisaAndStatut(
    @Param("reference") String reference,
    @Param("statut") String statut
);

// Trouver les demandes DUPLICATA issues d'une demande d'origine
// → affichage de l'historique des duplicatas
List<Demande> findByDemandeOrigineId(Long idDemandeOrigine);
```

---

### ✅ TEST ÉTAPE 4 — `DemandeRepositorySpring2Test.java`

```java
@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.ANY)
class DemandeRepositorySpring2Test {

    @Autowired DemandeRepository demandeRepository;
    // + autowired autres repos nécessaires pour construire les fixtures

    // Test 4.1 — findByDemandeurIdAndStatutDemandeLibelle("APPROUVEE")
    //            → retourne uniquement les demandes APPROUVEES du demandeur
    @Test
    void findApprovees_parDemandeur_OK() {
        Demande d = buildDemandeApprouvee(demandeur1);
        demandeRepository.save(d);

        List<Demande> results = demandeRepository
            .findByDemandeurIdAndStatutDemandeLibelle(demandeur1.getId(), "APPROUVEE");
        assertFalse(results.isEmpty());
        assertTrue(results.stream().allMatch(r ->
            r.getStatutDemande().getLibelle().equals("APPROUVEE")));
    }

    // Test 4.2 — findByDemandeurIdAndStatutDemandeLibelle("CREE")
    //            → ne retourne pas les demandes APPROUVEES
    @Test
    void findApprovees_parDemandeur_exclutAutresStatuts() {
        Demande d = buildDemandeStatut(demandeur1, "CREE");
        demandeRepository.save(d);
        List<Demande> results = demandeRepository
            .findByDemandeurIdAndStatutDemandeLibelle(demandeur1.getId(), "APPROUVEE");
        assertTrue(results.isEmpty());
    }

    // Test 4.3 — existsByDemandeOrigineId : true si duplicata déjà créé
    @Test
    void existsByDemandeOrigine_vraiSiPresent() {
        Demande origine = buildDemandeApprouvee(demandeur1);
        demandeRepository.save(origine);
        Demande dup = buildDemandeDuplicata(origine);
        demandeRepository.save(dup);

        assertTrue(demandeRepository
            .existsByDemandeOrigineIdAndTypeDemandeLibe(origine.getId(), "DUPLICATA"));
    }

    // Test 4.4 — findByPasseportNumeroAndStatut → retourne la bonne demande
    @Test
    void findByPasseportNumero_OK() {
        Demande d = buildDemandeApprouvee(demandeur1);  // passeport numéro "PP-SEARCH"
        demandeRepository.save(d);

        List<Demande> results = demandeRepository
            .findByPasseportNumeroAndStatut("PP-SEARCH", "APPROUVEE");
        assertFalse(results.isEmpty());
    }

    // Test 4.5 — findByDemandeOrigineId → liste les duplicatas d'une origine
    @Test
    void findByDemandeOrigine_OK() {
        Demande origine = buildDemandeApprouvee(demandeur1);
        demandeRepository.save(origine);
        Demande dup = buildDemandeDuplicata(origine);
        demandeRepository.save(dup);

        List<Demande> dups = demandeRepository.findByDemandeOrigineId(origine.getId());
        assertEquals(1, dups.size());
        assertEquals("DUPLICATA", dups.get(0).getTypeDemande().getLibelle());
    }
}
```

---

---

# ÉTAPE 5 — MODIFICATION `DemandeMapper.java`

**Fichier** : `src/main/java/com/projet/visa/mapper/DemandeMapper.java`
**Action** : ✏️ MODIF — ajouter deux méthodes de mapping

### Méthodes à ajouter dans la classe existante

```java
// ── À AJOUTER dans DemandeMapper.java ────────────────────────────────────────

/**
 * Convertit une Demande DUPLICATA en DuplicataResponseDTO.
 * Enrichit le DTO avec les informations de la demande d'origine.
 *
 * @param duplicata     entité Demande de type DUPLICATA (avec demandeOrigine chargée)
 * @return              DuplicataResponseDTO complet
 */
public DuplicataResponseDTO toDuplicataResponseDTO(Demande duplicata) {
    DuplicataResponseDTO dto = new DuplicataResponseDTO();
    dto.setId(duplicata.getId());
    dto.setDateDemande(duplicata.getDateDemande());
    dto.setTypeDemande(duplicata.getTypeDemande().getLibelle());
    dto.setStatutDemande(duplicata.getStatutDemande().getLibelle());

    // Infos demandeur
    dto.setIdDemandeur(duplicata.getDemandeur().getId());
    dto.setNomDemandeur(duplicata.getDemandeur().getNom());
    dto.setPrenomDemandeur(duplicata.getDemandeur().getPrenom());

    // Infos demande d'origine
    Demande origine = duplicata.getDemandeOrigine();
    dto.setIdDemandeOrigine(origine.getId());
    dto.setDateDemandeOrigine(origine.getDateDemande());
    dto.setTypeVisaOrigine(origine.getTypeVisa().getLibelle());
    dto.setReferenceVisaOrigine(origine.getVisaTransformable().getReferenceVisa());

    // Pièces
    List<DemandePieceDTO> piecesDTO = duplicata.getDemandePieces().stream()
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
 * Convertit une Demande en DemandeResumeeDTO (aperçu léger pour liste de sélection).
 *
 * @param demande   entité Demande (APPROUVEE attendue dans ce contexte)
 * @return          DemandeResumeeDTO
 */
public DemandeResumeeDTO toDemandeResumeeDTO(Demande demande) {
    DemandeResumeeDTO dto = new DemandeResumeeDTO();
    dto.setId(demande.getId());
    dto.setDateDemande(demande.getDateDemande());
    dto.setNomDemandeur(demande.getDemandeur().getNom());
    dto.setPrenomDemandeur(demande.getDemandeur().getPrenom());
    dto.setTypeVisa(demande.getTypeVisa().getLibelle());
    dto.setReferenceVisa(demande.getVisaTransformable().getReferenceVisa());
    dto.setStatutDemande(demande.getStatutDemande().getLibelle());
    dto.setNumeroPasSeport(demande.getVisaTransformable().getPasseport().getNumero());
    return dto;
}
```

---

### ✅ TEST ÉTAPE 5 — `DemandeMapperSpring2Test.java`

```java
// Test 5.1 — toDuplicataResponseDTO() : tous les champs mappés
Demande origine = buildDemandeApprouveeComplete();
Demande duplicata = buildDemandeDuplicataComplete(origine);
DuplicataResponseDTO dto = demandeMapper.toDuplicataResponseDTO(duplicata);

assertEquals("DUPLICATA", dto.getTypeDemande());
assertEquals("CREE", dto.getStatutDemande());
assertEquals(origine.getId(), dto.getIdDemandeOrigine());
assertEquals(origine.getTypeVisa().getLibelle(), dto.getTypeVisaOrigine());
assertNotNull(dto.getPieces());

// Test 5.2 — toDuplicataResponseDTO() : infos demandeur extraites de la demande (pas de l'origine)
assertEquals(duplicata.getDemandeur().getNom(), dto.getNomDemandeur());

// Test 5.3 — toDemandeResumeeDTO() : tous les champs présents
Demande d = buildDemandeApprouveeComplete();
DemandeResumeeDTO resumee = demandeMapper.toDemandeResumeeDTO(d);
assertNotNull(resumee.getId());
assertNotNull(resumee.getTypeVisa());
assertNotNull(resumee.getReferenceVisa());
assertEquals("APPROUVEE", resumee.getStatutDemande());
```

---

---

# ÉTAPE 6 — MODIFICATION `DemandeService.java`

**Fichier** : `src/main/java/com/projet/visa/service/DemandeService.java`
**Action** : ✏️ MODIF — ajouter 4 méthodes publiques + 1 méthode privée

### Méthodes à ajouter dans la classe existante

```java
// ══════════════════════════════════════════════════════════════
//  MÉTHODE : approuverDemande()
//  Passage manuel du statut CREE → APPROUVEE
//  Utilisé en Cas A : après création de la demande NOUVELLE,
//  l'agent approuve avant de créer le duplicata
// ══════════════════════════════════════════════════════════════

/**
 * Approuve une demande existante (statut CREE → APPROUVEE).
 * Crée une entrée dans l'historique statut.
 *
 * Règles :
 *   RG-04 : seule une demande en statut CREE peut être approuvée
 *   RG-05 : l'approbation génère une ligne dans historique_statut
 *
 * @param idDemande     identifiant de la demande à approuver
 * @param commentaire   motif d'approbation (facultatif)
 * @return              DemandeResponseDTO mis à jour
 * @throws ResourceNotFoundException si demande introuvable
 * @throws BusinessException si statut courant != CREE
 */
public DemandeResponseDTO approuverDemande(Long idDemande, String commentaire) {

    // 1. Charger la demande
    Demande demande = demandeRepository.findById(idDemande)
        .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + idDemande));

    // 2. Vérifier statut courant (RG-04)
    if (!"CREE".equals(demande.getStatutDemande().getLibelle())) {
        throw new BusinessException(
            "Impossible d'approuver une demande en statut : "
            + demande.getStatutDemande().getLibelle()
            + ". Seule une demande CREE peut être approuvée.");
    }

    // 3. Résoudre statut APPROUVEE
    StatutDemande statutApprouvee = statutDemandeRepository.findByLibelle("APPROUVEE")
        .orElseThrow(() -> new ResourceNotFoundException("StatutDemande APPROUVEE introuvable"));

    // 4. Mettre à jour le statut
    demande.setStatutDemande(statutApprouvee);
    demandeRepository.save(demande);

    // 5. Créer historique (RG-05)
    String motif = (commentaire != null && !commentaire.isBlank())
        ? commentaire
        : "Approbation de la demande";
    creerHistoriqueStatut(demande, statutApprouvee, motif);

    return demandeMapper.toResponseDTO(demande);
}


// ══════════════════════════════════════════════════════════════
//  MÉTHODE : creerDuplicata()
//  Création d'une demande DUPLICATA à partir d'une demande APPROUVEE
//  Commun aux Cas A et Cas B
// ══════════════════════════════════════════════════════════════

/**
 * Crée une nouvelle demande de type DUPLICATA.
 *
 * La demande DUPLICATA hérite du même Demandeur, VisaTransformable
 * et TypeVisa que la demande d'origine APPROUVEE.
 * Les pièces sont recalculées (communes + spécifiques).
 *
 * Règles :
 *   RG-02 : demandeOrigine obligatoire et non null
 *   RG-03 : demandeOrigine.statutDemande doit être APPROUVEE
 *   RG-06 : un seul duplicata actif (statut CREE) par demande d'origine
 *   RG-07 : typeDemande = "DUPLICATA" forcé
 *   RG-08 : statutDemande = "CREE" forcé
 *   RG-09 : création obligatoire d'une ligne dans historique_statut
 *   RG-10 : pièces obligatoires non fournies → BusinessException
 *
 * @param dto   données du formulaire duplicata
 * @return      DuplicataResponseDTO
 * @throws ResourceNotFoundException si demande d'origine introuvable
 * @throws BusinessException si règle métier violée
 */
public DuplicataResponseDTO creerDuplicata(DuplicataCreateDTO dto) {

    // ÉTAPE 1 — Charger et valider la demande d'origine
    Demande demandeOrigine = demandeRepository.findById(dto.getIdDemandeOrigine())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Demande d'origine introuvable : id=" + dto.getIdDemandeOrigine()));

    // RG-03 : vérifier statut APPROUVEE
    if (!"APPROUVEE".equals(demandeOrigine.getStatutDemande().getLibelle())) {
        throw new BusinessException(
            "Seule une demande APPROUVEE peut être dupliquée. "
            + "Statut actuel : " + demandeOrigine.getStatutDemande().getLibelle());
    }

    // RG-06 : vérifier absence de duplicata actif (CREE) pour cette origine
    boolean duplicataActifExiste = demandeRepository
        .existsByDemandeOrigineIdAndTypeDemandeLibe(
            dto.getIdDemandeOrigine(), "DUPLICATA");
    if (duplicataActifExiste) {
        throw new BusinessException(
            "Un duplicata existe déjà pour la demande #" + dto.getIdDemandeOrigine()
            + ". Veuillez traiter le duplicata en cours avant d'en créer un nouveau.");
    }

    // ÉTAPE 2 — Résoudre TypeDemande = DUPLICATA (RG-07)
    TypeDemande typeDuplicata = typeDemandeRepository.findByLibelle("DUPLICATA")
        .orElseThrow(() -> new ResourceNotFoundException("TypeDemande DUPLICATA introuvable"));

    // ÉTAPE 3 — Résoudre StatutDemande = CREE (RG-08)
    StatutDemande statutCree = statutDemandeRepository.findByLibelle("CREE")
        .orElseThrow(() -> new ResourceNotFoundException("StatutDemande CREE introuvable"));

    // ÉTAPE 4 — Construire la Demande DUPLICATA
    //           Elle hérite du Demandeur, VisaTransformable, TypeVisa de l'origine
    Demande duplicata = new Demande();
    duplicata.setDateDemande(LocalDateTime.now());
    duplicata.setDemandeur(demandeOrigine.getDemandeur());
    duplicata.setVisaTransformable(demandeOrigine.getVisaTransformable());
    duplicata.setTypeVisa(demandeOrigine.getTypeVisa());
    duplicata.setTypeDemande(typeDuplicata);
    duplicata.setStatutDemande(statutCree);
    duplicata.setDemandeOrigine(demandeOrigine);          // lien vers l'origine (RG-02)
    duplicata = demandeRepository.save(duplicata);

    // ÉTAPE 5 — Historique statut (RG-09)
    creerHistoriqueStatut(duplicata, statutCree, "Création demande duplicata");

    // ÉTAPE 6 — Lier les pièces (RG-10)
    lierPiecesADemande(duplicata, dto.getPiecesFournies(), demandeOrigine.getTypeVisa());

    // ÉTAPE 7 — Recharger et retourner
    Demande duplicataFinal = demandeRepository.findById(duplicata.getId()).orElseThrow();
    return demandeMapper.toDuplicataResponseDTO(duplicataFinal);
}


// ══════════════════════════════════════════════════════════════
//  MÉTHODE : rechercherDemandesApprouvees()
//  Recherche de demandes APPROUVEES selon critères (Cas B)
// ══════════════════════════════════════════════════════════════

/**
 * Recherche des demandes APPROUVEES selon les critères fournis.
 * Au moins un critère doit être renseigné.
 * Priorité : numéro passeport > référence visa > nom demandeur.
 *
 * @param rechercheDTO  critères de recherche
 * @return              liste de DemandeResumeeDTO (APPROUVEES uniquement)
 * @throws BusinessException si aucun critère n'est renseigné
 */
public List<DemandeResumeeDTO> rechercherDemandesApprouvees(DemandeRechercheDTO rechercheDTO) {

    boolean aucunCritere = (rechercheDTO.getNumeroPasSeport() == null
        || rechercheDTO.getNumeroPasSeport().isBlank())
        && (rechercheDTO.getReferenceVisa() == null
        || rechercheDTO.getReferenceVisa().isBlank())
        && (rechercheDTO.getNomDemandeur() == null
        || rechercheDTO.getNomDemandeur().isBlank());

    if (aucunCritere) {
        throw new BusinessException(
            "Au moins un critère de recherche est requis (nom, numéro passeport ou référence visa).");
    }

    List<Demande> resultats;

    // Priorité 1 : numéro passeport
    if (rechercheDTO.getNumeroPasSeport() != null && !rechercheDTO.getNumeroPasSeport().isBlank()) {
        resultats = demandeRepository.findByPasseportNumeroAndStatut(
            rechercheDTO.getNumeroPasSeport().trim(), "APPROUVEE");

    // Priorité 2 : référence visa
    } else if (rechercheDTO.getReferenceVisa() != null && !rechercheDTO.getReferenceVisa().isBlank()) {
        resultats = demandeRepository.findByReferenceVisaAndStatut(
            rechercheDTO.getReferenceVisa().trim(), "APPROUVEE");

    // Priorité 3 : nom demandeur
    } else {
        resultats = demandeRepository
            .findByStatutDemandeLibelleAndDemandeurNomContainingIgnoreCase(
                "APPROUVEE",
                rechercheDTO.getNomDemandeur().trim());
    }

    return resultats.stream()
        .map(demandeMapper::toDemandeResumeeDTO)
        .collect(Collectors.toList());
}


// ══════════════════════════════════════════════════════════════
//  MÉTHODE : getDuplicata()
// ══════════════════════════════════════════════════════════════

/**
 * Récupère une demande DUPLICATA par son id.
 *
 * @param id    identifiant de la demande duplicata
 * @return      DuplicataResponseDTO
 * @throws ResourceNotFoundException si demande introuvable
 * @throws BusinessException si la demande trouvée n'est pas de type DUPLICATA
 */
public DuplicataResponseDTO getDuplicata(Long id) {
    Demande demande = demandeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Demande introuvable : id=" + id));

    if (!"DUPLICATA".equals(demande.getTypeDemande().getLibelle())) {
        throw new BusinessException(
            "La demande #" + id + " n'est pas de type DUPLICATA.");
    }
    return demandeMapper.toDuplicataResponseDTO(demande);
}
```

---

### ✅ TEST ÉTAPE 6 — `DemandeServiceDuplicataTest.java`

```java
@ExtendWith(MockitoExtension.class)
class DemandeServiceDuplicataTest {

    // mocks identiques au Sprint 1 + les nouveaux
    @Mock DemandeRepository demandeRepository;
    @Mock HistoriqueStatutRepository historiqueStatutRepository;
    @Mock DemandePieceRepository demandePieceRepository;
    @Mock TypeDemandeRepository typeDemandeRepository;
    @Mock StatutDemandeRepository statutDemandeRepository;
    @Mock PieceRepository pieceRepository;
    @Mock DemandeMapper demandeMapper;
    @InjectMocks DemandeService demandeService;

    // ── Test 6.1 — approuverDemande() : statut CREE → APPROUVEE OK ────────────
    @Test
    void approuverDemande_OK() {
        Demande demande = buildDemandeStatut("CREE");
        demande.setId(1L);
        StatutDemande approuvee = buildStatut("APPROUVEE");

        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));
        when(statutDemandeRepository.findByLibelle("APPROUVEE")).thenReturn(Optional.of(approuvee));
        when(demandeRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
        when(demandeMapper.toResponseDTO(any())).thenReturn(new DemandeResponseDTO());

        DemandeResponseDTO result = demandeService.approuverDemande(1L, "Dossier complet");

        assertNotNull(result);
        verify(demandeRepository).save(argThat(d -> "APPROUVEE".equals(d.getStatutDemande().getLibelle())));
        verify(historiqueStatutRepository).save(any(HistoriqueStatut.class));
    }

    // ── Test 6.2 — RG-04 : approuver une demande APPROUVEE → BusinessException ─
    @Test
    void approuverDemande_dejaApprouvee_throwsException() {
        Demande demande = buildDemandeStatut("APPROUVEE");
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));

        assertThrows(BusinessException.class,
            () -> demandeService.approuverDemande(1L, null));
        verify(demandeRepository, never()).save(any());
    }

    // ── Test 6.3 — RG-04 : approuver une demande REFUSEE → BusinessException ───
    @Test
    void approuverDemande_refusee_throwsException() {
        Demande demande = buildDemandeStatut("REFUSEE");
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(demande));

        assertThrows(BusinessException.class,
            () -> demandeService.approuverDemande(1L, null));
    }

    // ── Test 6.4 — creerDuplicata() : création OK ─────────────────────────────
    @Test
    void creerDuplicata_OK() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        stubMocksDuplicata(dto, origine);

        DuplicataResponseDTO result = demandeService.creerDuplicata(dto);

        assertNotNull(result);
        verify(demandeRepository, atLeast(1)).save(any(Demande.class));
        verify(historiqueStatutRepository).save(any(HistoriqueStatut.class));
    }

    // ── Test 6.5 — RG-03 : demandeOrigine statut CREE → BusinessException ─────
    @Test
    void creerDuplicata_origineNonApprouvee_throwsException() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeStatut("CREE");
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(origine));

        assertThrows(BusinessException.class,
            () -> demandeService.creerDuplicata(dto));
        verify(demandeRepository, never()).save(any());
    }

    // ── Test 6.6 — RG-03 : demandeOrigine statut REFUSEE → BusinessException ──
    @Test
    void creerDuplicata_origineRefusee_throwsException() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeStatut("REFUSEE");
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(origine));

        assertThrows(BusinessException.class,
            () -> demandeService.creerDuplicata(dto));
    }

    // ── Test 6.7 — RG-06 : duplicata actif déjà existant → BusinessException ──
    @Test
    void creerDuplicata_duplicataDejaExiste_throwsException() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(origine));
        when(demandeRepository.existsByDemandeOrigineIdAndTypeDemandeLibe(1L, "DUPLICATA"))
            .thenReturn(true);  // duplicata déjà en cours

        assertThrows(BusinessException.class,
            () -> demandeService.creerDuplicata(dto));
    }

    // ── Test 6.8 — creerDuplicata() : hérite du Demandeur et TypeVisa de l'origine
    @Test
    void creerDuplicata_heriteInfosOrigine() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        stubMocksDuplicata(dto, origine);
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);

        demandeService.creerDuplicata(dto);

        verify(demandeRepository, atLeast(1)).save(captor.capture());
        Demande saved = captor.getAllValues().stream()
            .filter(d -> d.getDemandeOrigine() != null).findFirst().orElseThrow();
        assertEquals(origine.getDemandeur(), saved.getDemandeur());
        assertEquals(origine.getTypeVisa(), saved.getTypeVisa());
        assertEquals(origine.getVisaTransformable(), saved.getVisaTransformable());
    }

    // ── Test 6.9 — creerDuplicata() : typeDemande = DUPLICATA forcé (RG-07) ───
    @Test
    void creerDuplicata_typeDemande_DUPLICATA() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        stubMocksDuplicata(dto, origine);
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);

        demandeService.creerDuplicata(dto);

        verify(demandeRepository, atLeast(1)).save(captor.capture());
        Demande saved = captor.getAllValues().stream()
            .filter(d -> d.getDemandeOrigine() != null).findFirst().orElseThrow();
        assertEquals("DUPLICATA", saved.getTypeDemande().getLibelle());
    }

    // ── Test 6.10 — creerDuplicata() : statutDemande = CREE forcé (RG-08) ─────
    @Test
    void creerDuplicata_statutDemande_CREE() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        stubMocksDuplicata(dto, origine);
        ArgumentCaptor<Demande> captor = ArgumentCaptor.forClass(Demande.class);

        demandeService.creerDuplicata(dto);

        verify(demandeRepository, atLeast(1)).save(captor.capture());
        Demande saved = captor.getAllValues().stream()
            .filter(d -> d.getDemandeOrigine() != null).findFirst().orElseThrow();
        assertEquals("CREE", saved.getStatutDemande().getLibelle());
    }

    // ── Test 6.11 — creerDuplicata() : historique créé (RG-09) ─────────────────
    @Test
    void creerDuplicata_historiqueCreee() {
        DuplicataCreateDTO dto = buildDuplicataDTO(1L);
        Demande origine = buildDemandeApprouveeComplete();
        origine.setId(1L);
        stubMocksDuplicata(dto, origine);

        demandeService.creerDuplicata(dto);

        verify(historiqueStatutRepository, times(1)).save(any(HistoriqueStatut.class));
    }

    // ── Test 6.12 — rechercherDemandesApprouvees() : aucun critère → exception ─
    @Test
    void rechercherDemandesApprouvees_aucunCritere_throwsException() {
        DemandeRechercheDTO dto = new DemandeRechercheDTO();
        assertThrows(BusinessException.class,
            () -> demandeService.rechercherDemandesApprouvees(dto));
    }

    // ── Test 6.13 — rechercherDemandesApprouvees() : par nom → appelle le bon repo
    @Test
    void rechercherDemandesApprouvees_parNom_OK() {
        DemandeRechercheDTO dto = new DemandeRechercheDTO();
        dto.setNomDemandeur("Rakoto");
        when(demandeRepository.findByStatutDemandeLibelleAndDemandeurNomContainingIgnoreCase(
            "APPROUVEE", "Rakoto")).thenReturn(List.of(buildDemandeApprouveeComplete()));
        when(demandeMapper.toDemandeResumeeDTO(any())).thenReturn(new DemandeResumeeDTO());

        List<DemandeResumeeDTO> result = demandeService.rechercherDemandesApprouvees(dto);
        assertFalse(result.isEmpty());
    }

    // ── Test 6.14 — getDuplicata() : type non DUPLICATA → BusinessException ────
    @Test
    void getDuplicata_typeNonDuplicata_throwsException() {
        Demande d = buildDemandeStatut("CREE");
        d.setTypeDemande(buildTypeDemande("NOUVELLE"));
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(d));

        assertThrows(BusinessException.class,
            () -> demandeService.getDuplicata(1L));
    }
}
```

---

---

# ÉTAPE 7 — MODIFICATION `DemandeController.java`

**Fichier** : `src/main/java/com/projet/visa/controller/DemandeController.java`
**Action** : ✏️ MODIF — ajouter les endpoints Sprint 2 à la classe existante

### Endpoints à ajouter

```java
// ════════════════════════════════════════════════════════════════
//  GET /demandes/duplicata/choix
//  Point d'entrée : l'agent choisit entre Cas A et Cas B
// ════════════════════════════════════════════════════════════════
@GetMapping("/duplicata/choix")
public String afficherChoixCas(Model model) {
    model.addAttribute("rechercheDTO", new DemandeRechercheDTO());
    return "duplicata/choix-cas";
}


// ════════════════════════════════════════════════════════════════
//  GET /demandes/duplicata/recherche
//  Cas B : formulaire de recherche d'une demande APPROUVEE
// ════════════════════════════════════════════════════════════════
@GetMapping("/duplicata/recherche")
public String afficherRechercheDemandeApprouvee(
        @ModelAttribute("rechercheDTO") DemandeRechercheDTO rechercheDTO,
        Model model) {

    model.addAttribute("rechercheDTO", rechercheDTO);
    model.addAttribute("resultats", Collections.emptyList());
    return "duplicata/recherche-demande";
}


// ════════════════════════════════════════════════════════════════
//  POST /demandes/duplicata/recherche
//  Cas B : exécuter la recherche de demandes APPROUVEES
// ════════════════════════════════════════════════════════════════
@PostMapping("/duplicata/recherche")
public String executerRechercheDemandeApprouvee(
        @ModelAttribute("rechercheDTO") DemandeRechercheDTO rechercheDTO,
        Model model) {

    try {
        List<DemandeResumeeDTO> resultats =
            demandeService.rechercherDemandesApprouvees(rechercheDTO);
        model.addAttribute("resultats", resultats);
        model.addAttribute("rechercheDTO", rechercheDTO);
    } catch (BusinessException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("resultats", Collections.emptyList());
    }
    return "duplicata/recherche-demande";
}


// ════════════════════════════════════════════════════════════════
//  GET /demandes/duplicata/cas-b/formulaire?idDemandeOrigine=X
//  Cas B : formulaire léger (pièces uniquement, infos déjà connues)
// ════════════════════════════════════════════════════════════════
@GetMapping("/duplicata/cas-b/formulaire")
public String afficherFormulairesCasB(
        @RequestParam Long idDemandeOrigine,
        Model model) {

    try {
        DemandeResumeeDTO demandeOrigine =
            demandeMapper.toDemandeResumeeDTO(
                demandeRepository.findById(idDemandeOrigine)
                    .orElseThrow(() -> new ResourceNotFoundException(
                        "Demande introuvable : id=" + idDemandeOrigine)));

        DuplicataCreateDTO duplicataForm = new DuplicataCreateDTO();
        duplicataForm.setIdDemandeOrigine(idDemandeOrigine);
        duplicataForm.setDateDemande(LocalDate.now());

        model.addAttribute("demandeOrigine", demandeOrigine);
        model.addAttribute("duplicataForm", duplicataForm);
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        // pièces spécifiques chargées via AJAX (même endpoint Sprint 1 : GET /demandes/pieces)

    } catch (ResourceNotFoundException e) {
        model.addAttribute("errorMessage", e.getMessage());
        return "duplicata/recherche-demande";
    }
    return "duplicata/formulaire-cas-b";
}


// ════════════════════════════════════════════════════════════════
//  POST /demandes/duplicata/cas-b/soumettre
//  Cas B : enregistrement du duplicata
// ════════════════════════════════════════════════════════════════
@PostMapping("/duplicata/cas-b/soumettre")
public String soumettreFormulairesCasB(
        @Valid @ModelAttribute("duplicataForm") DuplicataCreateDTO dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
        // Recharger le contexte
        demandeRepository.findById(dto.getIdDemandeOrigine()).ifPresent(origine ->
            model.addAttribute("demandeOrigine",
                demandeMapper.toDemandeResumeeDTO(origine)));
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "duplicata/formulaire-cas-b";
    }

    try {
        DuplicataResponseDTO response = demandeService.creerDuplicata(dto);
        redirectAttributes.addFlashAttribute("successMessage",
            "Demande de duplicata créée. Référence : #" + response.getId());
        return "redirect:/demandes/duplicata/" + response.getId() + "/confirmation";

    } catch (BusinessException e) {
        model.addAttribute("errorMessage", e.getMessage());
        demandeRepository.findById(dto.getIdDemandeOrigine()).ifPresent(origine ->
            model.addAttribute("demandeOrigine",
                demandeMapper.toDemandeResumeeDTO(origine)));
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "duplicata/formulaire-cas-b";
    }
}


// ════════════════════════════════════════════════════════════════
//  GET /demandes/duplicata/cas-a/formulaire
//  Cas A : formulaire complet (réutilise le formulaire Sprint 1)
// ════════════════════════════════════════════════════════════════
@GetMapping("/duplicata/cas-a/formulaire")
public String afficherFormulaireCasA(Model model) {
    // Réutilise exactement le même modèle que le formulaire Sprint 1
    model.addAttribute("demandeForm", new DemandeCreateDTO());
    model.addAttribute("typesVisa", typeVisaRepository.findAll());
    model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
    model.addAttribute("nationalites", nationaliteRepository.findAll());
    model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
    model.addAttribute("modeCasA", true);   // flag pour différencier l'affichage
    return "duplicata/formulaire-cas-a";
}


// ════════════════════════════════════════════════════════════════
//  POST /demandes/duplicata/cas-a/soumettre
//  Cas A étape 1 : création de la demande NOUVELLE (réutilise Sprint 1)
// ════════════════════════════════════════════════════════════════
@PostMapping("/duplicata/cas-a/soumettre")
public String soumettreFormulaireCasA(
        @Valid @ModelAttribute("demandeForm") DemandeCreateDTO dto,
        BindingResult result,
        Model model,
        RedirectAttributes redirectAttributes) {

    if (result.hasErrors()) {
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        model.addAttribute("modeCasA", true);
        return "duplicata/formulaire-cas-a";
    }

    try {
        // Réutilise creerDemande() de Sprint 1
        DemandeResponseDTO demandeNouvelle = demandeService.creerDemande(dto);
        redirectAttributes.addFlashAttribute("successMessage",
            "Demande NOUVELLE créée. Veuillez l'approuver avant de créer le duplicata.");
        // Redirige vers la page d'approbation
        return "redirect:/demandes/" + demandeNouvelle.getId() + "/approbation";

    } catch (BusinessException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("typesVisa", typeVisaRepository.findAll());
        model.addAttribute("situationsFamiliales", situationFamilialeRepository.findAll());
        model.addAttribute("nationalites", nationaliteRepository.findAll());
        model.addAttribute("piecesCommunes", pieceService.getPiecesCommunes());
        return "duplicata/formulaire-cas-a";
    }
}


// ════════════════════════════════════════════════════════════════
//  GET /demandes/{id}/approbation
//  Cas A étape 2 : page de confirmation avant approbation
// ════════════════════════════════════════════════════════════════
@GetMapping("/{id}/approbation")
public String afficherPageApprobation(
        @PathVariable Long id,
        Model model) {

    DemandeResponseDTO demande = demandeService.getDemande(id);
    model.addAttribute("demande", demande);
    return "demande/approbation";
}


// ════════════════════════════════════════════════════════════════
//  POST /demandes/{id}/approuver
//  Cas A étape 2 : approbation de la demande NOUVELLE
// ════════════════════════════════════════════════════════════════
@PostMapping("/{id}/approuver")
public String approuverDemande(
        @PathVariable Long id,
        @RequestParam(required = false) String commentaire,
        RedirectAttributes redirectAttributes) {

    try {
        demandeService.approuverDemande(id, commentaire);
        redirectAttributes.addFlashAttribute("successMessage",
            "Demande #" + id + " approuvée. Vous pouvez maintenant créer le duplicata.");
        // Redirige directement vers le formulaire Cas B pré-rempli
        return "redirect:/demandes/duplicata/cas-b/formulaire?idDemandeOrigine=" + id;

    } catch (BusinessException e) {
        redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        return "redirect:/demandes/" + id + "/approbation";
    }
}


// ════════════════════════════════════════════════════════════════
//  GET /demandes/duplicata/{id}/confirmation
// ════════════════════════════════════════════════════════════════
@GetMapping("/duplicata/{id}/confirmation")
public String afficherConfirmationDuplicata(
        @PathVariable Long id,
        Model model) {

    DuplicataResponseDTO duplicata = demandeService.getDuplicata(id);
    model.addAttribute("duplicata", duplicata);
    return "duplicata/confirmation-duplicata";
}
```

---

### ✅ TEST ÉTAPE 7 — `DuplicataControllerTest.java`

```java
@WebMvcTest(DemandeController.class)
class DuplicataControllerTest {

    @Autowired MockMvc mockMvc;
    @MockBean DemandeService demandeService;
    @MockBean TypeVisaRepository typeVisaRepository;
    @MockBean SituationFamilialeRepository situationFamilialeRepository;
    @MockBean NationaliteRepository nationaliteRepository;
    @MockBean PieceService pieceService;
    @MockBean DemandeRepository demandeRepository;
    @MockBean DemandeMapper demandeMapper;

    // Test 7.1 — GET /demandes/duplicata/choix → 200, vue "duplicata/choix-cas"
    @Test
    void choixCas_renvoie200() throws Exception {
        mockMvc.perform(get("/demandes/duplicata/choix"))
               .andExpect(status().isOk())
               .andExpect(view().name("duplicata/choix-cas"))
               .andExpect(model().attributeExists("rechercheDTO"));
    }

    // Test 7.2 — GET /demandes/duplicata/recherche → 200, vue "duplicata/recherche-demande"
    @Test
    void rechercheDemandeApprouvee_renvoie200() throws Exception {
        mockMvc.perform(get("/demandes/duplicata/recherche"))
               .andExpect(status().isOk())
               .andExpect(view().name("duplicata/recherche-demande"));
    }

    // Test 7.3 — POST /demandes/duplicata/recherche → résultats dans le modèle
    @Test
    void recherchePost_avecResultats_OK() throws Exception {
        when(demandeService.rechercherDemandesApprouvees(any()))
            .thenReturn(List.of(new DemandeResumeeDTO()));

        mockMvc.perform(post("/demandes/duplicata/recherche").with(csrf())
                   .param("nomDemandeur", "Rakoto"))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("resultats"));
    }

    // Test 7.4 — POST recherche sans critère → errorMessage dans le modèle
    @Test
    void recherchePost_sansCritere_afficherErreur() throws Exception {
        when(demandeService.rechercherDemandesApprouvees(any()))
            .thenThrow(new BusinessException("Au moins un critère requis"));

        mockMvc.perform(post("/demandes/duplicata/recherche").with(csrf()))
               .andExpect(status().isOk())
               .andExpect(model().attributeExists("errorMessage"));
    }

    // Test 7.5 — GET /demandes/duplicata/cas-b/formulaire?idDemandeOrigine=1 → 200
    @Test
    void formulaireCasB_renvoie200() throws Exception {
        Demande d = buildDemandeApprouveeComplete();
        when(demandeRepository.findById(1L)).thenReturn(Optional.of(d));
        when(demandeMapper.toDemandeResumeeDTO(any())).thenReturn(new DemandeResumeeDTO());
        when(pieceService.getPiecesCommunes()).thenReturn(List.of());

        mockMvc.perform(get("/demandes/duplicata/cas-b/formulaire")
                   .param("idDemandeOrigine", "1"))
               .andExpect(status().isOk())
               .andExpect(view().name("duplicata/formulaire-cas-b"))
               .andExpect(model().attributeExists("demandeOrigine"))
               .andExpect(model().attributeExists("duplicataForm"));
    }

    // Test 7.6 — POST /demandes/duplicata/cas-b/soumettre OK → redirection confirmation
    @Test
    void soumettreFormulairesCasB_OK_redirige() throws Exception {
        DuplicataResponseDTO resp = new DuplicataResponseDTO();
        resp.setId(10L);
        when(demandeService.creerDuplicata(any())).thenReturn(resp);

        mockMvc.perform(post("/demandes/duplicata/cas-b/soumettre").with(csrf())
                   .param("idDemandeOrigine", "1"))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/demandes/duplicata/10/confirmation"));
    }

    // Test 7.7 — POST /demandes/{id}/approuver OK → redirection formulaire-cas-b
    @Test
    void approuverDemande_OK_redirige() throws Exception {
        when(demandeService.approuverDemande(eq(1L), any()))
            .thenReturn(new DemandeResponseDTO());

        mockMvc.perform(post("/demandes/1/approuver").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/demandes/duplicata/cas-b/formulaire?idDemandeOrigine=1"));
    }

    // Test 7.8 — POST /demandes/{id}/approuver BusinessException → redirection approbation
    @Test
    void approuverDemande_exception_redirige() throws Exception {
        when(demandeService.approuverDemande(eq(1L), any()))
            .thenThrow(new BusinessException("Statut invalide"));

        mockMvc.perform(post("/demandes/1/approuver").with(csrf()))
               .andExpect(status().is3xxRedirection())
               .andExpect(redirectedUrl("/demandes/1/approbation"));
    }

    // Test 7.9 — GET /demandes/duplicata/{id}/confirmation → 200, modèle "duplicata"
    @Test
    void confirmationDuplicata_renvoie200() throws Exception {
        when(demandeService.getDuplicata(10L)).thenReturn(new DuplicataResponseDTO());

        mockMvc.perform(get("/demandes/duplicata/10/confirmation"))
               .andExpect(status().isOk())
               .andExpect(view().name("duplicata/confirmation-duplicata"))
               .andExpect(model().attributeExists("duplicata"));
    }
}
```

---

---

# ÉTAPE 8 — TEMPLATES THYMELEAF

---

## 8.1 — `duplicata/choix-cas.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/duplicata/choix-cas.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Demande de Duplicata — Choix</title></head>
<body>

<h1>Demande de Duplicata</h1>
<p>Sélectionnez le cas correspondant à votre situation :</p>

<!-- ──────────────────────────────────────────── -->
<!-- CAS A : Personne inconnue du système         -->
<!-- ──────────────────────────────────────────── -->
<section class="cas-choice">
    <h2>CAS A — Demandeur sans dossier antérieur</h2>
    <p>
        Le demandeur n'a jamais soumis de dossier dans ce système.<br/>
        Vous devrez saisir toutes les informations, créer et approuver la demande initiale,
        puis enregistrer le duplicata.
    </p>
    <a th:href="@{/demandes/duplicata/cas-a/formulaire}" class="btn btn-primary">
        → Saisir un nouveau dossier (Cas A)
    </a>
</section>

<!-- ──────────────────────────────────────────── -->
<!-- CAS B : Personne avec dossier existant       -->
<!-- ──────────────────────────────────────────── -->
<section class="cas-choice">
    <h2>CAS B — Demandeur avec dossier antérieur approuvé</h2>
    <p>
        Le demandeur possède déjà une demande approuvée dans le système.<br/>
        Recherchez son dossier pour créer directement le duplicata.
    </p>
    <a th:href="@{/demandes/duplicata/recherche}" class="btn btn-secondary">
        → Rechercher un dossier existant (Cas B)
    </a>
</section>

<a th:href="@{/}">Retour à l'accueil</a>

</body>
</html>
```

---

## 8.2 — `duplicata/recherche-demande.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/duplicata/recherche-demande.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Recherche Dossier — Duplicata Cas B</title></head>
<body>

<h1>Rechercher un dossier approuvé (Cas B)</h1>

<!-- Message d'erreur -->
<div th:if="${errorMessage}" class="alert alert-danger">
    <span th:text="${errorMessage}"></span>
</div>

<!-- Formulaire de recherche multi-critères -->
<form th:action="@{/demandes/duplicata/recherche}"
      th:object="${rechercheDTO}"
      method="post">

    <p><em>Renseignez au moins un des critères ci-dessous :</em></p>

    <label>Nom du demandeur (F)</label>
    <input type="text" th:field="*{nomDemandeur}"
           placeholder="Ex: Rakotomalala..." />

    <label>Numéro de passeport (F)</label>
    <input type="text" th:field="*{numeroPasSeport}"
           placeholder="Ex: MG123456..." />

    <label>Référence visa (F)</label>
    <input type="text" th:field="*{referenceVisa}"
           placeholder="Ex: VT-2024-001..." />

    <button type="submit">Rechercher</button>
    <a th:href="@{/demandes/duplicata/choix}">Retour</a>
</form>

<!-- ─────────────────────────────────────── -->
<!-- Résultats de recherche                  -->
<!-- ─────────────────────────────────────── -->
<section th:if="${not #lists.isEmpty(resultats)}">
    <h2>Dossiers APPROUVÉS trouvés</h2>
    <table>
        <thead>
            <tr>
                <th>#</th>
                <th>Demandeur</th>
                <th>Type Visa</th>
                <th>Référence Visa</th>
                <th>Numéro Passeport</th>
                <th>Date Demande</th>
                <th>Statut</th>
                <th>Action</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="dem : ${resultats}">
                <td th:text="${dem.id}"></td>
                <td th:text="${dem.nomDemandeur + ' ' + dem.prenomDemandeur}"></td>
                <td th:text="${dem.typeVisa}"></td>
                <td th:text="${dem.referenceVisa}"></td>
                <td th:text="${dem.numeroPasSeport}"></td>
                <td th:text="${dem.dateDemande}"></td>
                <td>
                    <span class="badge badge-approuvee" th:text="${dem.statutDemande}"></span>
                </td>
                <td>
                    <a th:href="@{/demandes/duplicata/cas-b/formulaire(idDemandeOrigine=${dem.id})}"
                       class="btn btn-sm btn-success">
                        Créer le duplicata →
                    </a>
                </td>
            </tr>
        </tbody>
    </table>
</section>

<!-- Aucun résultat après recherche -->
<div th:if="${rechercheDTO != null
             and not #lists.isEmpty(rechercheDTO.nomDemandeur?:'')
             and #lists.isEmpty(resultats)}"
     class="alert alert-info">
    Aucun dossier APPROUVÉ trouvé pour ces critères.
</div>

</body>
</html>
```

---

## 8.3 — `duplicata/formulaire-cas-a.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/duplicata/formulaire-cas-a.html`

> Ce formulaire est **identique au formulaire Sprint 1** (`demande/formulaire.html`).
> La seule différence est la bannière d'information en haut et la route de soumission.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Duplicata Cas A — Saisie Dossier</title></head>
<body>

<!-- Bannière contextuelle Cas A -->
<div class="alert alert-info">
    <strong>CAS A — Étape 1/3 :</strong>
    Saisissez les informations du demandeur. Après validation,
    vous devrez approuver cette demande avant de créer le duplicata.
</div>

<h1>Saisie du Dossier Initial — Cas A</h1>

<!-- Message d'erreur -->
<div th:if="${errorMessage}" class="alert alert-danger">
    <span th:text="${errorMessage}"></span>
</div>

<!--
    FORMULAIRE IDENTIQUE À demande/formulaire.html (Sprint 1)
    Réutiliser intégralement les blocs :
      - 🟦 ÉTAT CIVIL
      - 🟩 PASSEPORT
      - 🟨 VISA TRANSFORMABLE
      - 🟥 DEMANDE (type visa select)
      - 🟪 PIÈCES (communes + dynamiques AJAX)

    Seul changement : action du formulaire pointe vers /duplicata/cas-a/soumettre
-->
<form th:action="@{/demandes/duplicata/cas-a/soumettre}"
      th:object="${demandeForm}"
      method="post">

    <!-- ═══════ Bloc 🟦 ÉTAT CIVIL ═══════ -->
    <!-- [identique Sprint 1 — copier le bloc] -->

    <!-- ═══════ Bloc 🟩 PASSEPORT ═══════ -->
    <!-- [identique Sprint 1 — copier le bloc] -->

    <!-- ═══════ Bloc 🟨 VISA TRANSFORMABLE ═══════ -->
    <!-- [identique Sprint 1 — copier le bloc] -->

    <!-- ═══════ Bloc 🟥 DEMANDE ═══════ -->
    <!-- [identique Sprint 1 — copier le bloc] -->

    <!-- ═══════ Bloc 🟪 PIÈCES ═══════ -->
    <!-- [identique Sprint 1 — copier le bloc + JS AJAX] -->

    <div class="form-actions">
        <button type="submit">ENREGISTRER ET CONTINUER →</button>
        <a th:href="@{/demandes/duplicata/choix}">ANNULER</a>
    </div>

</form>

<!-- Script AJAX identique au formulaire Sprint 1 -->
<script>
    // [copier le script JS du formulaire Sprint 1]
</script>

</body>
</html>
```

---

## 8.4 — `demande/approbation.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/demande/approbation.html`

> Page intermédiaire entre Étape 1 (saisie) et Étape 3 (duplicata) du Cas A.

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Approbation de la Demande — Cas A Étape 2</title></head>
<body>

<!-- Bannière contextuelle -->
<div class="alert alert-warning">
    <strong>CAS A — Étape 2/3 :</strong>
    Vérifiez le récapitulatif ci-dessous, puis approuvez la demande pour pouvoir créer le duplicata.
</div>

<!-- Message flash (succès/erreur redirectAttributes) -->
<div th:if="${successMessage}" class="alert alert-success" th:text="${successMessage}"></div>
<div th:if="${errorMessage}" class="alert alert-danger" th:text="${errorMessage}"></div>

<h1>Approbation de la demande #<span th:text="${demande.id}"></span></h1>

<!-- Récapitulatif de la demande à approuver -->
<section>
    <h2>Récapitulatif</h2>
    <table>
        <tr><th>Demandeur</th>
            <td th:text="${demande.nomDemandeur + ' ' + demande.prenomDemandeur}"></td></tr>
        <tr><th>Type Visa</th>      <td th:text="${demande.typeVisa}"></td></tr>
        <tr><th>Référence Visa</th> <td th:text="${demande.referenceVisa}"></td></tr>
        <tr><th>Type Demande</th>   <td th:text="${demande.typeDemande}"></td></tr>
        <tr><th>Statut actuel</th>
            <td><span class="badge badge-cree" th:text="${demande.statutDemande}"></span></td></tr>
        <tr><th>Date demande</th>   <td th:text="${demande.dateDemande}"></td></tr>
    </table>
</section>

<!-- Formulaire d'approbation -->
<form th:action="@{'/demandes/' + ${demande.id} + '/approuver'}" method="post">

    <label>Commentaire (F)</label>
    <textarea name="commentaire"
              placeholder="Ex: Dossier complet et vérifié..."></textarea>

    <div class="form-actions">
        <button type="submit" class="btn btn-success">
            ✔ APPROUVER ET CRÉER LE DUPLICATA →
        </button>
        <a th:href="@{/demandes/duplicata/choix}">ANNULER</a>
    </div>
</form>

</body>
</html>
```

---

## 8.5 — `duplicata/formulaire-cas-b.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/duplicata/formulaire-cas-b.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Duplicata Cas B — Confirmation Pièces</title></head>
<body>

<!-- Bannière contextuelle -->
<div class="alert alert-info">
    <strong>CAS B — Étape unique :</strong>
    Les informations du dossier sont déjà connues.
    Cochez les pièces fournies et confirmez la demande de duplicata.
</div>

<!-- Message d'erreur -->
<div th:if="${errorMessage}" class="alert alert-danger" th:text="${errorMessage}"></div>

<h1>Demande de Duplicata — Dossier existant</h1>

<!-- ─────────────────────────────────────────── -->
<!-- Récapitulatif LECTURE SEULE du dossier      -->
<!-- ─────────────────────────────────────────── -->
<section id="recap-dossier">
    <h2>Dossier d'origine (APPROUVÉ)</h2>
    <table>
        <tr><th>Demande #</th>      <td th:text="${demandeOrigine.id}"></td></tr>
        <tr><th>Demandeur</th>
            <td th:text="${demandeOrigine.nomDemandeur + ' ' + demandeOrigine.prenomDemandeur}"></td></tr>
        <tr><th>Numéro Passeport</th><td th:text="${demandeOrigine.numeroPasSeport}"></td></tr>
        <tr><th>Type Visa</th>       <td th:text="${demandeOrigine.typeVisa}"></td></tr>
        <tr><th>Référence Visa</th>  <td th:text="${demandeOrigine.referenceVisa}"></td></tr>
        <tr><th>Statut</th>
            <td><span class="badge badge-approuvee"
                      th:text="${demandeOrigine.statutDemande}"></span></td></tr>
    </table>
</section>

<!-- ─────────────────────────────────────────── -->
<!-- Formulaire pièces                           -->
<!-- ─────────────────────────────────────────── -->
<form th:action="@{/demandes/duplicata/cas-b/soumettre}"
      th:object="${duplicataForm}"
      method="post">

    <!-- Hidden : id de la demande d'origine -->
    <input type="hidden" th:field="*{idDemandeOrigine}" />
    <input type="hidden" th:field="*{dateDemande}" />

    <!-- ═══════ Bloc 🟪 PIÈCES ═══════ -->
    <section id="pieces">
        <h2>Pièces à fournir</h2>

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

        <h3>Pièces spécifiques</h3>
        <!-- Chargées automatiquement au chargement de la page (type visa connu) -->
        <div id="pieces-specifiques"></div>
    </section>

    <div class="form-actions">
        <button type="submit" class="btn btn-primary">CONFIRMER LE DUPLICATA</button>
        <a th:href="@{/demandes/duplicata/recherche}">RETOUR À LA RECHERCHE</a>
    </div>

</form>

<script th:inline="javascript">
    // Charger automatiquement les pièces spécifiques au chargement
    // (le type visa est connu depuis demandeOrigine)
    const typeVisaId = /*[[${demandeOrigine.typeVisaId}]]*/ null;
    // Note : ajouter typeVisaId dans DemandeResumeeDTO si non présent

    // Réutilisation du même endpoint AJAX Sprint 1 : GET /demandes/pieces?idTypeVisa=X
    if (typeVisaId) {
        fetch('/demandes/pieces?idTypeVisa=' + typeVisaId)
            .then(res => res.json())
            .then(pieces => {
                const container = document.getElementById('pieces-specifiques');
                pieces.forEach(piece => {
                    const div = document.createElement('div');
                    const cb = document.createElement('input');
                    cb.type = 'checkbox';
                    cb.name = 'piecesFournies';
                    cb.value = piece.id;
                    cb.id = 'spec_' + piece.id;
                    const lbl = document.createElement('label');
                    lbl.htmlFor = 'spec_' + piece.id;
                    lbl.textContent = piece.nom + (piece.obligatoire ? ' *' : '');
                    div.appendChild(cb);
                    div.appendChild(lbl);
                    container.appendChild(div);
                });
            });
    }
</script>

</body>
</html>
```

> ⚠️ **Note** : pour que le JS ci-dessus fonctionne, ajouter `typeVisaId` (Long) dans `DemandeResumeeDTO`.
> C'est une modification mineure du DTO Sprint 2 (3.4) :
> ```java
> private Long typeVisaId;   // à ajouter dans DemandeResumeeDTO
> // + mapper dans DemandeMapper.toDemandeResumeeDTO() :
> dto.setTypeVisaId(demande.getTypeVisa().getId());
> ```

---

## 8.6 — `duplicata/confirmation-duplicata.html` 🆕 NOUVEAU

**Fichier** : `src/main/resources/templates/duplicata/confirmation-duplicata.html`

```html
<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org">
<head><title>Duplicata Confirmé</title></head>
<body>

<h1>Demande de Duplicata enregistrée</h1>

<div th:if="${successMessage}" class="alert alert-success"
     th:text="${successMessage}"></div>

<section id="recap-duplicata">
    <h2>Récapitulatif du Duplicata</h2>
    <table>
        <tr><th>Numéro duplicata</th>    <td th:text="${duplicata.id}"></td></tr>
        <tr><th>Date de demande</th>     <td th:text="${duplicata.dateDemande}"></td></tr>
        <tr><th>Type demande</th>        <td th:text="${duplicata.typeDemande}"></td></tr>
        <tr><th>Demandeur</th>
            <td th:text="${duplicata.nomDemandeur + ' ' + duplicata.prenomDemandeur}"></td></tr>
        <tr>
            <th>Statut</th>
            <td><span class="badge badge-cree" th:text="${duplicata.statutDemande}"></span></td>
        </tr>
    </table>

    <h3>Référence au dossier d'origine</h3>
    <table>
        <tr><th>Demande origine #</th>   <td th:text="${duplicata.idDemandeOrigine}"></td></tr>
        <tr><th>Date origine</th>        <td th:text="${duplicata.dateDemandeOrigine}"></td></tr>
        <tr><th>Type Visa</th>           <td th:text="${duplicata.typeVisaOrigine}"></td></tr>
        <tr><th>Référence Visa</th>      <td th:text="${duplicata.referenceVisaOrigine}"></td></tr>
    </table>
</section>

<section id="pieces-duplicata">
    <h2>Pièces associées</h2>
    <table>
        <thead>
            <tr>
                <th>Pièce</th><th>Obligatoire</th><th>Fournie</th>
            </tr>
        </thead>
        <tbody>
            <tr th:each="piece : ${duplicata.pieces}">
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

# RÉCAPITULATIF DES RÈGLES DE GESTION

| Code | Règle | Implémentée dans |
|------|-------|-----------------|
| RG-01 | `demandeOrigine` = null pour toute demande NOUVELLE | `Demande.java` (nullable) |
| RG-02 | `demandeOrigine` obligatoire et non null pour toute demande DUPLICATA | `DemandeService.creerDuplicata()` |
| RG-03 | `demandeOrigine.statutDemande` doit être `APPROUVEE` pour créer un duplicata | `DemandeService.creerDuplicata()` |
| RG-04 | Seule une demande en statut `CREE` peut être approuvée | `DemandeService.approuverDemande()` |
| RG-05 | Toute approbation génère une ligne dans `historique_statut` | `DemandeService.approuverDemande()` |
| RG-06 | Un seul duplicata actif (statut `CREE`) par demande d'origine | `DemandeService.creerDuplicata()` + `DemandeRepository` |
| RG-07 | `typeDemande` = `"DUPLICATA"` forcé en service | `DemandeService.creerDuplicata()` |
| RG-08 | `statutDemande` = `"CREE"` forcé à la création du duplicata | `DemandeService.creerDuplicata()` |
| RG-09 | Création d'un duplicata → ligne obligatoire dans `historique_statut` | `DemandeService.creerDuplicata()` |
| RG-10 | Pièces obligatoires non fournies → `BusinessException` | `DemandeService.lierPiecesADemande()` (Sprint 1, réutilisé) |
| RG-11 | La demande DUPLICATA hérite du même Demandeur, VisaTransformable et TypeVisa que l'origine | `DemandeService.creerDuplicata()` |
| RG-12 | Au moins un critère de recherche (nom/passeport/visa) pour la recherche Cas B | `DemandeService.rechercherDemandesApprouvees()` |
| RG-13 | Cas A : flux obligatoire en 3 étapes → saisie → approbation → duplicata | `DemandeController` (enchaînement des redirections) |

---

---

# RÉCAPITULATIF DE TOUS LES TESTS

| # | Test | Classe | Type |
|---|------|--------|------|
| T-01 | Demande NOUVELLE : demandeOrigine = null | `DemandeEntityModifTest` | Unitaire |
| T-02 | Demande DUPLICATA : demandeOrigine non null | `DemandeEntityModifTest` | Unitaire |
| T-03 | Pas de régression sur les champs Sprint 1 | `DemandeEntityModifTest` | Unitaire |
| T-04 | DuplicataCreateDTO : idDemandeOrigine null → violation | `DuplicataDtoValidationTest` | Unitaire |
| T-05 | DuplicataCreateDTO : piecesFournies initialisée | `DuplicataDtoValidationTest` | Unitaire |
| T-06 | DuplicataCreateDTO valide → aucune violation | `DuplicataDtoValidationTest` | Unitaire |
| T-07 | DemandeRechercheDTO : tous champs null → instanciable | `DuplicataDtoValidationTest` | Unitaire |
| T-08 | findByDemandeurIdAndStatutDemandeLibelle("APPROUVEE") → retourne APPROUVEES | `DemandeRepositorySpring2Test` | Intégration H2 |
| T-09 | findByDemandeurIdAndStatutDemandeLibelle("CREE") → exclut APPROUVEES | `DemandeRepositorySpring2Test` | Intégration H2 |
| T-10 | existsByDemandeOrigineId → true si duplicata présent | `DemandeRepositorySpring2Test` | Intégration H2 |
| T-11 | findByPasseportNumeroAndStatut → retourne la bonne demande | `DemandeRepositorySpring2Test` | Intégration H2 |
| T-12 | findByDemandeOrigineId → liste les duplicatas | `DemandeRepositorySpring2Test` | Intégration H2 |
| T-13 | toDuplicataResponseDTO() : tous champs mappés | `DemandeMapperSpring2Test` | Unitaire |
| T-14 | toDuplicataResponseDTO() : infos demandeur de la demande courante | `DemandeMapperSpring2Test` | Unitaire |
| T-15 | toDemandeResumeeDTO() : tous champs présents | `DemandeMapperSpring2Test` | Unitaire |
| T-16 | approuverDemande() : CREE → APPROUVEE OK | `DemandeServiceDuplicataTest` | Unitaire |
| T-17 | RG-04 : approuver APPROUVEE → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-18 | RG-04 : approuver REFUSEE → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-19 | creerDuplicata() : création complète OK | `DemandeServiceDuplicataTest` | Unitaire |
| T-20 | RG-03 : origine statut CREE → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-21 | RG-03 : origine statut REFUSEE → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-22 | RG-06 : duplicata actif déjà existant → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-23 | creerDuplicata() : hérite Demandeur + TypeVisa + VisaTransformable | `DemandeServiceDuplicataTest` | Unitaire |
| T-24 | RG-07 : typeDemande = DUPLICATA forcé | `DemandeServiceDuplicataTest` | Unitaire |
| T-25 | RG-08 : statutDemande = CREE forcé | `DemandeServiceDuplicataTest` | Unitaire |
| T-26 | RG-09 : historique créé après création duplicata | `DemandeServiceDuplicataTest` | Unitaire |
| T-27 | RG-12 : rechercherDemandesApprouvees() sans critère → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-28 | rechercherDemandesApprouvees() par nom → appelle bon repo | `DemandeServiceDuplicataTest` | Unitaire |
| T-29 | getDuplicata() : type non DUPLICATA → BusinessException | `DemandeServiceDuplicataTest` | Unitaire |
| T-30 | GET /demandes/duplicata/choix → 200, vue OK | `DuplicataControllerTest` | MVC |
| T-31 | GET /demandes/duplicata/recherche → 200 | `DuplicataControllerTest` | MVC |
| T-32 | POST recherche avec résultats → modèle "resultats" | `DuplicataControllerTest` | MVC |
| T-33 | POST recherche sans critère → errorMessage | `DuplicataControllerTest` | MVC |
| T-34 | GET /duplicata/cas-b/formulaire → 200, modèle complet | `DuplicataControllerTest` | MVC |
| T-35 | POST cas-b/soumettre OK → redirection confirmation | `DuplicataControllerTest` | MVC |
| T-36 | POST /demandes/{id}/approuver OK → redirection formulaire-cas-b | `DuplicataControllerTest` | MVC |
| T-37 | POST approuver exception → redirection approbation | `DuplicataControllerTest` | MVC |
| T-38 | GET /duplicata/{id}/confirmation → 200, modèle "duplicata" | `DuplicataControllerTest` | MVC |

---

---

# ORDRE D'EXÉCUTION RECOMMANDÉ

```
Semaine 1 — Fondations
  ├─ Script SQL migration (ALTER TABLE + INSERT type_demande DUPLICATA)
  ├─ Étape 1 : Modification Demande.java (ajout demandeOrigine)  + Tests T-01 à T-03
  ├─ Étape 2 : Vérification TypeDemande DUPLICATA en base
  ├─ Étape 3 : DTOs Sprint 2 (4 nouveaux DTOs)                  + Tests T-04 à T-07
  └─ Étape 4 : Modification DemandeRepository                   + Tests T-08 à T-12

Semaine 2 — Service et Controller
  ├─ Étape 5 : Modification DemandeMapper (2 nouvelles méthodes) + Tests T-13 à T-15
  ├─ Étape 6 : Modification DemandeService (4 nouvelles méthodes)+ Tests T-16 à T-29
  ├─ Étape 7 : Modification DemandeController (10 nouveaux endpoints) + Tests T-30 à T-38
  └─ Étape 8 : Templates Thymeleaf (5 nouvelles pages + 1 modif)
```

---

## NAVIGATION COMPLÈTE SPRINT 2

```
/demandes/duplicata/choix
    ├── [Cas A] → /demandes/duplicata/cas-a/formulaire
    │               ↓ POST
    │           /demandes/{id}/approbation
    │               ↓ POST /approuver
    │           /demandes/duplicata/cas-b/formulaire?idDemandeOrigine={id}
    │               ↓ POST
    │           /demandes/duplicata/{id}/confirmation
    │
    └── [Cas B] → /demandes/duplicata/recherche
                    ↓ POST (recherche)
                  [Liste résultats] → lien "Créer duplicata"
                    ↓
                  /demandes/duplicata/cas-b/formulaire?idDemandeOrigine={id}
                    ↓ POST
                  /demandes/duplicata/{id}/confirmation
```

---

> ⚠️ Ne pas commencer les templates (Étape 8) avant que les tests du service soient tous au vert.
> ⚠️ La méthode `lierPiecesADemande()` de Sprint 1 est réutilisée **sans modification** pour les pièces du duplicata.
> ⚠️ L'endpoint AJAX `GET /demandes/pieces?idTypeVisa=X` de Sprint 1 est réutilisé **sans modification** dans les formulaires Cas A et Cas B.
> ⚠️ Penser à ajouter `typeVisaId` dans `DemandeResumeeDTO` pour permettre le chargement automatique des pièces spécifiques dans `formulaire-cas-b.html`.