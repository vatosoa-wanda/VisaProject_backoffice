# TODO Sprint 3 — DEV 1 (ETU003324 / Harenkantou)

## Contexte (analyse de l’existant — Dev2 déjà terminé)

### Déjà en place (OK)
- Page scan (GET) : contrôleur + page Thymeleaf
	- `src/main/java/com/visa/backoffice/controller/ScanController.java`
	- `src/main/resources/templates/scan/scan.html`
- Upload / suppression / remplacement documents + blocage côté `DocumentService` si statut = `SCAN_TERMINE`
	- `src/main/java/com/visa/backoffice/controller/DocumentController.java`
	- `src/main/java/com/visa/backoffice/service/DocumentService.java`
- Table `document` présente dans le script SQL (liée à `piece`)
	- `script/240426.sql`

### Manquant (DEV 1)
- Validation finale “Terminer le scan” : méthode métier + endpoint `POST /demandes/{id}/scan-terminer`
- Verrouillage total après `SCAN_TERMINE` (notamment interdiction de modifier une demande via `/demandes/{id}/modifier`)
- Export PDF attestation : `GET /demandes/{id}/attestation`
- Seed base : statut `SCAN_TERMINE` pas présent dans `statut_demande` (actuellement : CREE/TERMINEE/APPROUVEE/REFUSEE)

---

## 📋 RÉSUMÉ DES TÂCHES

| Tâche | Responsable | Type | Statut |
|------|-------------|------|--------|
| 1. Validation finale du scan (verrouillage) | ETU003324 | Backend + Frontend | À faire |
| 2. Blocage après scan terminé (modifs interdites) | ETU003324 | Backend + Frontend | À faire |
| 3. Export PDF (Attestation dossier) | ETU003324 | Backend + Frontend | À faire |
| 4. Tests globaux Sprint 3 | ETU003324 | Tests | À faire |

---

## 🎯 TÂCHE 1 — Validation finale du scan (Terminer le scan)

### Objectif (selon To-do-sprint3.txt)
- Bouton “Terminer le scan” visible si statut = `CREE`
- Confirmation : “Après validation, aucune modification ne sera possible”
- Backend : statut → `SCAN_TERMINE` + historisation dans `historique_statut`
- API : `POST /demandes/{id}/scan-terminer`

### 📁 Fichiers à modifier / créer

#### Backend
- Modifier : `src/main/java/com/visa/backoffice/service/DemandeService.java`
	- Ajouter : `terminerScan(Long demandeId)`
- Modifier : `src/main/java/com/visa/backoffice/controller/DemandeController.java`
	- Ajouter l’endpoint : `POST /demandes/{id}/scan-terminer`
- Modifier : `src/main/java/com/visa/backoffice/repository/StatutDemandeRepository.java` (déjà OK)
	- Utiliser `findByLibelle("SCAN_TERMINE")`

#### Frontend (Thymeleaf)
- Modifier : `src/main/resources/templates/scan/scan.html`
	- Remplacer le bouton placeholder “Terminer le scan” (actuellement un `alert(...)`) par un vrai `<form method="post">`

#### Base de données (scripts)
- Modifier : `script/240426.sql`
	- Ajouter le statut `SCAN_TERMINE` dans `statut_demande`

### 🔧 Fonctions / règles métier à implémenter

#### `DemandeService.terminerScan(demandeId)`
1. Charger la demande (sinon `ResourceNotFoundException`)
2. Vérifier `statutDemande.libelle == "CREE"` (sinon `BusinessException` : double validation interdite)
3. (Option) Vérifier cohérence avant verrouillage :
	 - au moins 1 document fourni, ou
	 - tous les documents attendus sont fournis
4. Récupérer `StatutDemande` “SCAN_TERMINE” via `statutDemandeRepository.findByLibelle("SCAN_TERMINE")`
5. Mettre à jour `demande.statutDemande` puis `save`
6. Ajouter une entrée dans `historique_statut`
	 - Réutiliser la logique existante `creerHistoriqueStatut(demande, statut, commentaire)`

#### Endpoint `POST /demandes/{id}/scan-terminer`
- Si succès : rediriger vers `/demandes/{id}/scan` avec flash message succès
- Si erreur métier : rediriger vers `/demandes/{id}/scan` avec flash message erreur

### ✅ Tests

#### Unit Tests (service)
- Créer : `src/test/java/com/visa/backoffice/service/DemandeServiceScanTest.java`
	- `terminerScan` passe de `CREE` → `SCAN_TERMINE`
	- `terminerScan` refuse si statut != `CREE`
	- Historique créé (au moins vérifier qu’une entrée est ajoutée)

#### Integration Tests (controller)
- Créer : `src/test/java/com/visa/backoffice/controller/DemandeScanTerminerControllerTest.java`
	- `POST /demandes/{id}/scan-terminer` ⇒ 302 redirect
	- Refus sur demande déjà verrouillée

---

## 🔒 TÂCHE 2 — Blocage après scan terminé (verrouillage total)

### Objectif
Si statut = `SCAN_TERMINE`, interdire :
- upload / suppression / remplacement documents
- modification de la demande (formulaire `/demandes/{id}/modifier`)

### État actuel
- Documents : déjà bloqués côté `DocumentService` (OK)
- Page scan : boutons déjà cachés/désactivés si dossier verrouillé (OK)

### À faire (important)

#### Backend
- Modifier : `src/main/java/com/visa/backoffice/service/DemandeService.java`
	- Dans `modifierDemande(...)`, refuser si `statutDemande.libelle == "SCAN_TERMINE"`
	- Idéal : lever `BusinessException` ou `DemandeVerrouilleException`

#### Frontend
- Modifier : `src/main/resources/templates/demande/liste.html`
	- Cacher ou désactiver le lien “Modifier” si statut = `SCAN_TERMINE`
- Modifier : `src/main/resources/templates/demande/details.html`
	- Cacher ou désactiver le bouton “Modifier la demande” si statut = `SCAN_TERMINE`

### ✅ Tests
- Unit test : `modifierDemande` refuse si `SCAN_TERMINE`
- (Option) Integration test : `POST /demandes/{id}/modifier` renvoie erreur contrôlée

---

## 📄 TÂCHE 3 — Export PDF (Attestation dossier)

### Objectif (selon To-do-sprint3.txt)
- API : `GET /demandes/{id}/attestation`
- Générer un PDF contenant :
	- Référence demande + statut
	- Etat civil
	- Passeport
	- Visa
	- Pièces fournies (basées sur `document` + `piece`)
	- Footer : date génération

### 📁 Fichiers à créer / modifier

#### Dépendances
- Modifier : `pom.xml`
	- Ajouter une librairie PDF (recommandé : PDFBox)

#### Backend
- Créer : `src/main/java/com/visa/backoffice/service/PdfService.java`
	- `byte[] genererAttestation(Long demandeId)`
- Modifier : `src/main/java/com/visa/backoffice/controller/DemandeController.java`
	- Ajouter `GET /demandes/{id}/attestation` qui retourne `application/pdf`

#### Frontend
- Modifier : `src/main/resources/templates/scan/scan.html`
	- Remplacer le bouton placeholder “Exporter PDF” par un lien vers `/demandes/{id}/attestation`

### 🔧 Points d’implémentation
- Le PDF peut être simple (texte + listes), pas besoin de mise en page complexe.
- Le nom du fichier côté réponse HTTP : `attestation_DEM-000123.pdf` (ou similaire).

### ✅ Tests
- Unit test `PdfService` : retour non vide, et idéalement contient l’en-tête `%PDF`
- Integration test endpoint : status 200 + `Content-Type: application/pdf`

---

## 🧪 TÂCHE 4 — TEST GLOBAL DU SPRINT (E2E)

### Scénario attendu
1. Création demande → statut = `CREE`
2. Accès à `/demandes/{id}/scan`
3. Upload partiel (plusieurs sessions)
4. Visualisation document OK
5. Export PDF OK
6. Terminer le scan
7. Vérifier : statut = `SCAN_TERMINE` + blocage total (documents + modification demande)

---

## 📌 Checklist de livraison (DEV 1)

- [ ] Statut `SCAN_TERMINE` ajouté dans les scripts DB
- [ ] `DemandeService.terminerScan(...)` implémenté + historique ajouté
- [ ] Endpoint `POST /demandes/{id}/scan-terminer` OK
- [ ] Bouton “Terminer le scan” (scan.html) : vrai POST + confirm
- [ ] Verrouillage : modification de demande interdite (backend + UI)
- [ ] Export PDF : service + endpoint + lien depuis scan.html
- [ ] Tests unitaires + intégration passent
