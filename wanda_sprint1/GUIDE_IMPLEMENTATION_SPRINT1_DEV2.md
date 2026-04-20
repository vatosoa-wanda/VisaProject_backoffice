# 📋 GUIDE D'IMPLÉMENTATION ET DE TEST - SPRINT 1 DEV 2

## ✅ FICHIERS CRÉÉS / MODIFIÉS

### **EXCEPTIONS (2 fichiers créés)**
- ✅ `src/main/java/com/visa/backoffice/exception/BusinessException.java`
- ✅ `src/main/java/com/visa/backoffice/exception/ResourceNotFoundException.java`

### **DTOs (5 fichiers créés)**
- ✅ `src/main/java/com/visa/backoffice/dto/VisaTransformableDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/DemandeCreateDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/DemandeResponseDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/PieceDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/DemandePieceDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/DemandeurDTO.java`
- ✅ `src/main/java/com/visa/backoffice/dto/PasseportDTO.java`

### **MAPPERS (2 fichiers créés)**
- ✅ `src/main/java/com/visa/backoffice/mapper/VisaTransformableMapper.java`
- ✅ `src/main/java/com/visa/backoffice/mapper/DemandeMapper.java`

### **SERVICES (4 fichiers modifiés)**
- ✅ `src/main/java/com/visa/backoffice/service/VisaTransformableService.java` (COMPLÉTÉ)
- ✅ `src/main/java/com/visa/backoffice/service/PieceService.java` (COMPLÉTÉ)
- ✅ `src/main/java/com/visa/backoffice/service/DemandeService.java` (COMPLÉTÉ - cœur du sprint)
- ✅ `src/main/java/com/visa/backoffice/service/DemandeurService.java` (AUGMENTÉ - creerOuRecuperer)
- ✅ `src/main/java/com/visa/backoffice/service/PasseportService.java` (AUGMENTÉ - creer)

### **REPOSITORIES (6 fichiers modifiés)**
- ✅ `src/main/java/com/visa/backoffice/repository/VisaTransformableRepository.java` (+ findByReferenceVisa)
- ✅ `src/main/java/com/visa/backoffice/repository/PieceRepository.java` (+ findByTypePieceCode, findByTypePieceCodeIn)
- ✅ `src/main/java/com/visa/backoffice/repository/TypeDemandeRepository.java` (+ findByLibelle)
- ✅ `src/main/java/com/visa/backoffice/repository/StatutDemandeRepository.java` (+ findByLibelle)
- ✅ `src/main/java/com/visa/backoffice/repository/DemandeurRepository.java` (+ findByNomAndDateNaissance)
- ✅ `src/main/java/com/visa/backoffice/repository/PasseportRepository.java` (+ findByNumero)

### **CONTROLLER (1 fichier créé)**
- ✅ `src/main/java/com/visa/backoffice/controller/DemandeController.java`

### **TEMPLATES THYMELEAF (2 fichiers créés)**
- ✅ `src/main/resources/templates/demande/formulaire.html`
- ✅ `src/main/resources/templates/demande/confirmation.html`

---

## 🧪 TESTS À EFFECTUER

### **PARTIE 1 : Tests de démarrage**
```bash
# 1. Vérifier que l'application démarre
mvn spring-boot:run

# 2. Vérifier l'accès au formulaire
curl http://localhost:8080/demandes/nouvelle
```

### **PARTIE 2 : Tests du Formulaire**

#### 2.1 - Afficher le formulaire vierge
- **URL**: `GET http://localhost:8080/demandes/nouvelle`
- **Attendu**: 
  - La page affiche le formulaire complet avec 4 sections (État Civil, Passeport, Visa Transformable, Demande, Pièces)
  - Les listes déroulantes sont remplies (Situations familiales, Nationalités, Types visa)
  - Les pièces communes sont affichées

#### 2.2 - AJAX : Charger les pièces spécifiques
- **URL**: `GET http://localhost:8080/demandes/pieces?idTypeVisa=1`
- **Attendu**: 
  - Réponse JSON contenant les pièces du type visa
  - Format: `[{"id": 1, "nom": "...", "obligatoire": true, "typePiece": "TRAVAILLEUR"}]`

### **PARTIE 3 : Tests de création complet**

#### 3.1 - Créer une demande valide (Travailleur)
```bash
# Soumettre le formulaire
POST /demandes/nouvelle
Content-Type: application/x-www-form-urlencoded

Champs à remplir:
- demandeurDTO.nom=Dupont
- demandeurDTO.prenom=Jean
- demandeurDTO.dateNaissance=1990-05-15
- demandeurDTO.idSituationFamiliale=1
- demandeurDTO.idNationalite=1
- demandeurDTO.adresseMadagascar=123 Rue Principale, Antananarivo
- demandeurDTO.telephone=0261234567
- demandeurDTO.email=jean@example.com
- passeportDTO.numero=A12345678
- passeportDTO.dateDelivrance=2020-01-01
- passeportDTO.dateExpiration=2030-01-01
- visaDTO.referenceVisa=VIS-2024-001
- visaDTO.dateEntree=2024-01-01
- visaDTO.lieuEntree=Tana
- visaDTO.dateExpiration=2025-01-01
- idTypeVisa=1 (Travailleur)
- piecesFournies=1,2,3 (IDs des pièces cochées)
```

**Attendu**: 
- Redirection vers `/demandes/{id}/confirmation`
- Message de succès: "Demande créée avec succès. Référence : #{id}"

#### 3.2 - Afficher la page de confirmation
- **URL**: `GET http://localhost:8080/demandes/{id}/confirmation`
- **Attendu**:
  - Affichage du récapitulatif de la demande
  - Affichage des pièces avec leur statut (fournie/non fournie)
  - Boutons "Retour" et "Nouvelle demande"

### **PARTIE 4 : Tests des validations**

#### 4.1 - Soumettre sans pièce obligatoire
- Laisser une pièce obligatoire non cochée
- **Attendu**: 
  - Retour au formulaire
  - Message d'erreur: "Pièce obligatoire non fournie : {nom_piece}"

#### 4.2 - Soumettre sans données obligatoires
- Laisser des champs obligatoires vides (nom, nationalité, etc.)
- **Attendu**: 
  - Retour au formulaire
  - Messages de validation Bootstrap affichés

#### 4.3 - Référence visa déjà utilisée
- Soumettre une deuxième demande avec la même référence visa
- **Attendu**: 
  - Retour au formulaire
  - Message d'erreur: "La référence visa '...' est déjà utilisée."

#### 4.4 - Dates incohérentes du visa
- Soumettre avec `dateEntree > dateExpiration`
- **Attendu**: 
  - Retour au formulaire
  - Message d'erreur: "La date d'expiration doit être postérieure à la date d'entrée."

#### 4.5 - Numéro passeport déjà utilisé
- Soumettre une deuxième demande avec le même numéro passeport
- **Attendu**: 
  - Retour au formulaire
  - Message d'erreur: "Le numéro de passeport '...' est déjà utilisé."

### **PARTIE 5 : Tests de la base de données**

#### 5.1 - Vérifier la création en base
```sql
-- Vérifier la demande
SELECT * FROM demande WHERE id = {id};

-- Vérifier l'historique
SELECT * FROM historique_statut WHERE id_demande = {id};

-- Vérifier les pièces liées
SELECT * FROM demande_piece WHERE id_demande = {id};
```

**Attendu**:
- Demande créée avec statut=CREE, type_demande=NOUVELLE
- Historique créé avec le statut initial
- Toutes les pièces (communes + spécifiques) liées avec le bon statut fourni

#### 5.2 - Vérifier l'unicité des contraintes
```sql
-- Vérifier que referenceVisa est unique
INSERT INTO visa_transformable (reference_visa, date_entree, lieu_entree, date_expiration, id_passeport) 
VALUES ('VIS-2024-001', '2024-01-01', 'Tana', '2025-01-01', 1);
-- → Erreur: violate unique constraint

-- Vérifier que numero passeport est unique
INSERT INTO passeport (numero, date_delivrance, date_expiration, id_demandeur) 
VALUES ('A12345678', '2020-01-01', '2030-01-01', 1);
-- → Erreur: violate unique constraint
```

### **PARTIE 6 : Tests des règles métier**

#### RG-01: referenceVisa doit être unique
✅ Testé en 4.3

#### RG-02: dateExpiration > dateEntree (visa)
✅ Testé en 4.4

#### RG-03: Passeport obligatoire
✅ Implicite dans la structure

#### RG-04: dateDemande = LocalDateTime.now()
- **Vérification**: La date créée doit être la date/heure actuelle (±1 minute)

#### RG-05: typeDemande = "NOUVELLE" (forcé en service)
✅ Vérifier en 5.1

#### RG-06: statutDemande = "CREE" (forcé en service)
✅ Vérifier en 5.1

#### RG-07: Historique créé à chaque changement de statut
✅ Vérifier en 5.1

#### RG-08: Pièces = COMMUN + spécifiques selon type visa
- Créer une demande avec type Travailleur
- Vérifier en base que les pièces COMMUN et TRAVAILLEUR sont liées

#### RG-09: Pièce obligatoire non fournie → exception
✅ Testé en 4.1

### **PARTIE 7 : Tests API JSON (si frontend)

#### 7.1 - Récupérer les pièces spécifiques
```bash
GET /demandes/pieces?idTypeVisa=1
Content-Type: application/json

Réponse attendue:
[
  {
    "id": 4,
    "nom": "Autorisation emploi",
    "obligatoire": true,
    "typePiece": "TRAVAILLEUR"
  },
  {
    "id": 5,
    "nom": "Attestation employeur",
    "obligatoire": true,
    "typePiece": "TRAVAILLEUR"
  }
]
```

---

## 🔗 DÉPENDANCES AVEC DEV 1

Les fonctionnalités suivantes dépendent du code de Dev 1:
- ✅ `DemandeurDTO` et `Demandeur` entity
- ✅ `PasseportDTO` et `Passeport` entity
- ✅ `DemandeurService.creerOuRecuperer()` → implémenté en local
- ✅ `PasseportService.creer()` → implémenté en local
- ✅ `SituationFamilialeRepository.findAll()`
- ✅ `NationaliteRepository.findAll()`

**Status**: Dev 1 a fourni les entités, mais les services manquaient les méthodes métier. 
Elles ont été implémentées en local par Dev 2.

---

## 📝 CHECKLIST DE VALIDATION

- [ ] Application démarre sans erreur
- [ ] Page formulaire s'affiche correctement
- [ ] AJAX charge les pièces spécifiques au changement du type visa
- [ ] Création complète valide redirige vers confirmation
- [ ] Page confirmation affiche tous les détails
- [ ] Validation Bean Validation fonctionne
- [ ] Erreurs métier (référence dupliquée, pièce manquante) s'affichent
- [ ] Base de données: demande créée avec statut CREE
- [ ] Base de données: historique créé
- [ ] Base de données: pièces liées avec bon statut fourni
- [ ] Unicité de referenceVisa respectée
- [ ] Unicité de numero passeport respectée
- [ ] Dates visa cohérentes (expiration > entrée)

---

## 🚀 NOTES D'IMPLÉMENTATION

### Points clés réalisés:
1. **Architecture MVC complète**: Controllers, Services, Repositories, DTOs, Mappers
2. **Validation**: Bean Validation + règles métier en service
3. **Transactions**: @Transactional sur les services
4. **Historique**: Création automatique à chaque création de demande
5. **Pièces dynamiques**: Chargement AJAX selon type visa
6. **Sécurité**: Validations strictes, unicités en base

### Points d'attention:
- Les méthodes `creerOuRecuperer` de Dev1 ne visent que la recherche par nom+date
- Les pièces spécifiques sont chargées par code TypePiece (TRAVAILLEUR, INVESTISSEUR)
- Les statuts CREE et typeDemande NOUVELLE sont forcés en service (non modifiables par l'utilisateur)

### Parties en attente (⏳ DEV 1):
- Validation avancée des DTOs (si besoin)
- Services complets pour Demandeur/Passeport (maintenant complétés par Dev 2)
