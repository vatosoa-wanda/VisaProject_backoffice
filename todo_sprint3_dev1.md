# TODO Sprint 3 - DEV 1 (ETU003366 / vatosoa-wanda)

**Note:** Selon le fichier To-do-sprint3.txt, vous êtes assigné comme **Dev 2**, mais ce document récapitule vos tâches pour le Sprint 3.

---

## 📋 RÉSUMÉ DES TÂCHES

| Tâche | Responsable | Type | Statut |
|-------|------------|------|--------|
| 1. Page scan des pièces justificatives | ETU003366 | Frontend + Backend | À faire |
| 2. Upload et gestion des documents | ETU003366 | Backend + API | À faire |
| 3. Visualisation des documents | ETU003366 | Backend + API | À faire |
| 4. Continuité du scan (reprise) | ETU003366 | Métier | À faire |

---

## 🎯 TÂCHE 1: PAGE SCAN DES PIÈCES JUSTIFICATIVES

### Description
Créer une page web `/demandes/{id}/scan` pour afficher et gérer le scan des pièces justificatives.

### 📁 Fichiers à créer

#### Backend
- `src/main/java/com/visa/backoffice/controller/ScanController.java` - Contrôleur REST pour les pages de scan
- `src/main/java/com/visa/backoffice/dto/ScanPageDTO.java` - DTO pour la page de scan
- `src/main/java/com/visa/backoffice/dto/InformationsDemandeDTO.java` - DTO pour les informations (état civil, passeport, visa)
- `src/main/java/com/visa/backoffice/dto/PiecesResumeDTO.java` - DTO pour le résumé des pièces

#### Frontend (Thymeleaf)
- `src/main/resources/templates/scan.html` - Template pour la page de scan

### 🔧 Méthodes/Classes à implémenter

#### ScanController
```java
// GET /demandes/{id}/scan
public String afficherPageScan(@PathVariable Long id, Model model)

// Récupère toutes les infos pour la page de scan
```

#### ScanPageDTO
```
- demandeId: Long
- reference: String
- statut: String (CREE / SCAN_TERMINE)
- demandeur: DemandeurDTO (nom, prénom, dateNaissance, situationFamiliale)
- passeport: PasseportDTO (numéro, dateDebut, dateFin)
- visa: VisaDTO (type, dates, référence)
- pieces: List<PieceDTO>
- nombrePiecesFournies: Integer
- nombrePiecesAttendues: Integer
```

#### PieceDTO
```
- id: Long
- typePiece: String
- statut: String (NON_FOURNI / FOURNI)
- fileName: String
```

### 📊 Base de données

#### Tables existantes à utiliser
- `demande` - pour les infos principales
- `demandeur` - pour l'état civil
- `passeport` - pour le passeport
- `visa` - pour le visa
- `piece` - pour les pièces (probablement existante)

#### Nouvelle table à créer (si elle n'existe pas)
```sql
CREATE TABLE document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_demande BIGINT NOT NULL,
    id_type_piece BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    date_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    statut VARCHAR(50) DEFAULT 'FOURNI',
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_type_piece) REFERENCES type_piece(id)
);
```

### ✅ Tests

#### Unit Tests
- `src/test/java/com/visa/backoffice/controller/ScanControllerTest.java`
  - Test: GET /demandes/{id}/scan retourne les données correctes
  - Test: Affichage du statut CREE
  - Test: Affichage du statut SCAN_TERMINE
  - Test: Les pièces s'affichent correctement
  
#### Integration Tests
- Vérifier que la page charge toutes les données du dossier
- Vérifier que le résumé des pièces est correct

---

## 📤 TÂCHE 2: UPLOAD ET GESTION DES DOCUMENTS

### Description
Implémenter les services métier et API REST pour l'upload, suppression et remplacement de documents.

### 📁 Fichiers à créer

#### Backend
- `src/main/java/com/visa/backoffice/model/Document.java` - Entité JPA Document
- `src/main/java/com/visa/backoffice/repository/DocumentRepository.java` - Repository JPA
- `src/main/java/com/visa/backoffice/service/DocumentService.java` - Service métier
- `src/main/java/com/visa/backoffice/controller/DocumentController.java` - Contrôleur REST
- `src/main/java/com/visa/backoffice/dto/DocumentUploadDTO.java` - DTO pour l'upload
- `src/main/java/com/visa/backoffice/exception/DemandeVerrouilleException.java` - Exception métier

### 🔧 Méthodes/Classes à implémenter

#### Document (Entité JPA)
```java
@Entity
@Table(name = "document")
public class Document {
    @Id @GeneratedValue
    private Long id;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_demande")
    private Demande demande;
    
    @ManyToOne(optional = false)
    @JoinColumn(name = "id_type_piece")
    private TypePiece typePiece;
    
    private String fileName;          // Nom original du fichier
    private String filePath;          // Chemin de stockage physique
    private Long fileSize;
    private String fileType;          // PDF, JPG, PNG
    private LocalDateTime dateUpload;
}
```

#### DocumentRepository
```java
public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByDemandeId(Long demandeId);
    Document findByIdAndDemandeId(Long id, Long demandeId);
    void deleteByIdAndDemandeId(Long id, Long demandeId);
}
```

#### DocumentService
```java
public class DocumentService {
    // Uploads un document et l'enregistre en base
    public Document uploadDocument(DocumentUploadDTO dto) throws IOException {
        // 1. Vérifier statut = CREE
        // 2. Générer nom unique
        // 3. Sauvegarder fichier physique
        // 4. Enregistrer Document en base
        // 5. Retourner Document
    }
    
    // Supprime un document (interdit si SCAN_TERMINE)
    public void supprimerDocument(Long docId) {
        // 1. Récupérer document
        // 2. Vérifier demande.statut != SCAN_TERMINE
        // 3. Supprimer fichier physique
        // 4. Supprimer en base
    }
    
    // Remplace un document (interdit si SCAN_TERMINE)
    public Document remplacerDocument(Long docId, MultipartFile fichier) throws IOException {
        // 1. Récupérer document existant
        // 2. Vérifier statut != SCAN_TERMINE
        // 3. Supprimer ancien fichier
        // 4. Sauvegarder nouveau fichier
        // 5. Mettre à jour en base
    }
    
    // Récupère tous les documents d'une demande
    public List<Document> getDocumentsByDemande(Long demandeId) {
        // Retourner tous les documents d'une demande
    }
}
```

#### DocumentController
```java
@RestController
@RequestMapping("/documents")
public class DocumentController {
    
    // POST /documents
    // Upload un document
    public ResponseEntity<Document> uploadDocument(@RequestParam Long demandeId,
                                                   @RequestParam MultipartFile fichier)
    
    // GET /documents?demandeId={id}
    // Liste les documents d'une demande
    public ResponseEntity<List<Document>> listDocuments(@RequestParam Long demandeId)
    
    // DELETE /documents/{id}
    // Supprime un document
    public ResponseEntity<Void> deleteDocument(@PathVariable Long id)
    
    // PUT /documents/{id}
    // Remplace un document
    public ResponseEntity<Document> updateDocument(@PathVariable Long id,
                                                   @RequestParam MultipartFile fichier)
}
```

#### DocumentUploadDTO
```java
public class DocumentUploadDTO {
    private Long demandeId;
    private Long typePieceId;
    private MultipartFile file;
}
```

### 📊 Base de données

#### Table à créer (ou vérifier si existante)
```sql
CREATE TABLE document (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_demande BIGINT NOT NULL,
    id_type_piece BIGINT NOT NULL,
    file_name VARCHAR(255) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    file_size BIGINT,
    file_type VARCHAR(100),
    date_upload TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_type_piece) REFERENCES type_piece(id)
);

CREATE INDEX idx_document_demande ON document(id_demande);
```

#### Configuration de stockage
- Créer dossier: `uploads/documents/` (configurable en application.properties)
- Naming strategy: `{demandeId}_{typePieceId}_{timestamp}_{originalFilename}`

### ✅ Tests

#### Unit Tests - DocumentService
- `src/test/java/com/visa/backoffice/service/DocumentServiceTest.java`
  - Test: uploadDocument OK
  - Test: supprimerDocument OK
  - Test: remplacerDocument OK
  - Test: refus upload si statut = SCAN_TERMINE
  - Test: refus suppression si statut = SCAN_TERMINE
  - Test: refus remplacement si statut = SCAN_TERMINE

#### Integration Tests - DocumentController
- `src/test/java/com/visa/backoffice/controller/DocumentControllerTest.java`
  - Test: POST /documents upload OK
  - Test: GET /documents?demandeId={id} retourne liste
  - Test: DELETE /documents/{id} supprime
  - Test: PUT /documents/{id} remplace
  - Test: 403 Forbidden si SCAN_TERMINE

#### Performance Tests
- Upload fichier 10 MB → OK
- Upload 50 fichiers → OK
- Lister 100 documents → OK

---

## 👁️ TÂCHE 3: VISUALISATION DES DOCUMENTS

### Description
Implémenter l'affichage des documents (PDF ou images).

### 📁 Fichiers à créer

#### Backend
- Méthodes dans `DocumentService` (déjà créé en Tâche 2)
- Route dans `DocumentController` (déjà créé en Tâche 2)

### 🔧 Méthodes/Classes à implémenter

#### DocumentController - Nouvelle route
```java
@GetMapping("/{id}/file")
public ResponseEntity<byte[]> downloadDocument(@PathVariable Long id) {
    // 1. Récupérer document
    // 2. Charger fichier
    // 3. Retourner avec bon Content-Type
    //    - PDF: application/pdf
    //    - JPG: image/jpeg
    //    - PNG: image/png
}
```

#### DocumentService - Nouvelle méthode
```java
public byte[] getDocumentFile(Long docId) throws IOException {
    // 1. Récupérer document
    // 2. Charger fichier du disque
    // 3. Retourner bytes
}

public String getContentType(String filePath) {
    // Déterminer le Content-Type selon l'extension
}
```

### Frontend (Thymeleaf - intégration à scan.html)
```html
<!-- Dans le tableau des pièces -->
<button onclick="viewDocument(/* id */)">Voir</button>

<script>
function viewDocument(docId) {
    // Ouvrir popup ou onglet avec GET /documents/{id}/file
    window.open('/documents/' + docId + '/file', '_blank');
}
</script>
```

### ✅ Tests

#### Unit Tests
- Test: GET /documents/{id}/file retourne PDF
- Test: GET /documents/{id}/file retourne Image
- Test: 404 si document n'existe pas
- Test: Content-Type correct (application/pdf, image/jpeg, etc.)

#### Integration Tests
- Upload PDF → Visualiser → PDF bien affiché
- Upload JPG → Visualiser → Image bien affichée
- Upload PNG → Visualiser → Image bien affichée

---

## 🔄 TÂCHE 4: CONTINUITÉ DU SCAN (REPRISE)

### Description
Permettre à l'utilisateur de quitter et revenir sans perdre ses uploads.

### 📁 Fichiers à créer

Aucun fichier supplémentaire. Cette tâche utilise les composants déjà créés.

### 🔧 Méthodes/Classes à implémenter

Aucune nouvelle méthode. La reprise fonctionne automatiquement grâce à:
1. **Base de données persistante** - Les documents sont en base (table `document`)
2. **Logique métier existante** - getDocumentsByDemande() récupère les documents stockés
3. **Page de scan** - reload de la page rechargera les données

### 📊 Base de données

Aucune table supplémentaire. Utiliser la table `document` créée en Tâche 2.

### ✅ Tests

#### Scénario de test complet
```
1. Créer demande → statut = CREE
2. Accéder à /demandes/{id}/scan
3. Upload document 1 → Vérifier en base ✓
4. Upload document 2 → Vérifier en base ✓
5. QUITTER sans validation (fermer onglet/navigateur)
6. Revenir à /demandes/{id}/scan
7. Vérifier que documents 1 et 2 sont toujours là ✓
8. Continuer uploads → document 3 ✓
```

#### Unit Tests
- Test: Documents conservés après déconnexion
- Test: Reprise possible à tout moment (avant SCAN_TERMINE)
- Test: Pas d'impact sur autres demandes

---

## 📋 CHECKLIST DE LIVRAISON

### Avant de passer au Dev 2 (ETU003324), vérifier:

- [ ] **Tâche 1 - Page Scan**
  - [ ] Controller ScanController créé et fonctionne
  - [ ] DTOs créés (ScanPageDTO, InformationsDemandeDTO, PiecesResumeDTO)
  - [ ] Template scan.html créé
  - [ ] Page affiche les infos du dossier (état civil, passeport, visa)
  - [ ] Tableau des pièces affiche correctement
  - [ ] Statut CREE/SCAN_TERMINE s'affiche
  - [ ] Résumé des pièces correct (X fournies / Y attendues)
  - [ ] Tests unitaires PASS
  - [ ] Tests d'intégration PASS

- [ ] **Tâche 2 - Upload & Gestion**
  - [ ] Entité Document créée
  - [ ] Repository DocumentRepository créé
  - [ ] Service DocumentService créé avec 4 méthodes
  - [ ] Controller DocumentController créé avec 4 routes
  - [ ] DTOs DocumentUploadDTO créé
  - [ ] Exception DemandeVerrouilleException créée
  - [ ] Table `document` créée en BDD
  - [ ] Dossier `uploads/documents/` créé
  - [ ] Upload fonctionne (POST /documents)
  - [ ] Listing fonctionne (GET /documents?demandeId={id})
  - [ ] Suppression fonctionne (DELETE /documents/{id})
  - [ ] Remplacement fonctionne (PUT /documents/{id})
  - [ ] Blocage OK si SCAN_TERMINE
  - [ ] Tests unitaires PASS
  - [ ] Tests d'intégration PASS

- [ ] **Tâche 3 - Visualisation**
  - [ ] Route GET /documents/{id}/file créée
  - [ ] Support PDF OK
  - [ ] Support JPG OK
  - [ ] Support PNG OK
  - [ ] Content-Type correct
  - [ ] Bouton "Voir" fonctionne en frontend
  - [ ] Tests PASS

- [ ] **Tâche 4 - Continuité**
  - [ ] Upload partiels conservés en BDD
  - [ ] Reprise possible après déconnexion
  - [ ] Pas de perte de données
  - [ ] Tests PASS

- [ ] **Code Quality**
  - [ ] Aucun SonarQube warning
  - [ ] Tous les tests PASS
  - [ ] Code bien formaté
  - [ ] Commentaires sur les parties complexes
  - [ ] Documentation API complète

- [ ] **Prêt pour Dev 2**
  - [ ] Branche créée: `feature/sprint_3_1_scan_documents` (si pas déjà fait)
  - [ ] Commits clairs avec messages explicites
  - [ ] PR préparée avec description détaillée
  - [ ] Review TL demandée

---

## 🧪 TESTS GLOBAUX (À LA FIN)

### Test End-to-End complet
```
Flux complet:
1. Création demande (déjà implémenté) → statut = CREE
2. Accès à /demandes/{id}/scan
   ✓ Infos du dossier affichées
   ✓ Tableau des pièces vide (NON_FOURNI)
3. Upload document 1
   ✓ Document en BDD
   ✓ Fichier physique créé
   ✓ Statut = FOURNI
4. Upload document 2
   ✓ Documents en BDD
5. Visualisation document 1
   ✓ PDF/Image s'affiche correctement
6. Suppression document 2
   ✓ Document supprimé de BDD
   ✓ Fichier physique supprimé
7. Remplacement document 1
   ✓ Ancien fichier supprimé
   ✓ Nouveau fichier créé
8. Déconnexion et reconnexion
   ✓ Documents toujours présents
9. Attendre Dev 2 pour "Terminer le scan"
   ✓ Après cela, statut = SCAN_TERMINE
   ✓ Aucun upload/suppression/remplacement possible
```

---

## 📌 NOTES IMPORTANTES

### Structure des fichiers et dossiers

```
src/main/java/com/visa/backoffice/
├── model/
│   └── Document.java (nouvelle)
├── repository/
│   └── DocumentRepository.java (nouvelle)
├── service/
│   ├── DocumentService.java (nouvelle)
│   └── ScanService.java (optionnel)
├── controller/
│   ├── ScanController.java (nouvelle)
│   └── DocumentController.java (nouvelle)
├── dto/
│   ├── DocumentUploadDTO.java (nouvelle)
│   ├── ScanPageDTO.java (nouvelle)
│   ├── InformationsDemandeDTO.java (nouvelle)
│   └── PiecesResumeDTO.java (nouvelle)
└── exception/
    └── DemandeVerrouilleException.java (nouvelle)

src/main/resources/
├── templates/
│   └── scan.html (nouvelle)
└── application.properties
    └── Ajouter: app.upload.dir=uploads/documents/

src/test/java/com/visa/backoffice/
├── service/
│   └── DocumentServiceTest.java (nouvelle)
└── controller/
    ├── ScanControllerTest.java (nouvelle)
    └── DocumentControllerTest.java (nouvelle)

uploads/
└── documents/ (à créer)
```

### Dépendances

Les dépendances suivantes doivent être présentes dans pom.xml:
- Spring Boot Starter Data JPA (déjà présente)
- Spring Boot Starter Web MVC (déjà présente)
- Lombok (déjà présente probablement)
- JUnit 5 (pour les tests)
- MockMvc (pour les tests d'intégration)

Si besoin d'une librairie pour les PDFs (ex: iText ou Apache PDFBox), l'ajouter pour la Tâche 5 (PDF Export).

### Configuration

Dans `application.properties` ou `application.yml`, ajouter:
```properties
# Upload configuration
app.upload.dir=uploads/documents/
app.upload.max-file-size=50MB
app.upload.allowed-types=pdf,jpg,jpeg,png
```

### Sécurité

- ✓ Vérifier le type de fichier (extension + MIME type)
- ✓ Générer noms uniques pour éviter les collisions
- ✓ Nettoyer les paths pour éviter les directory traversal
- ✓ Vérifier que l'utilisateur a droit d'accès à la demande
- ✓ Enregistrer les uploads en logs pour audit

---

## 📞 CONTACT & QUESTIONS

**TL:** ETU003350 / HarimalalaEricka
**Dev 1:** ETU003324 / Harenkantou (en charge de la validation finale et export PDF)

---

**Date création:** 2026-04-28
**Branche:** feature/sprint_3_1_scan_documents
**Priorité:** Haute (dépendance pour Dev 2)
