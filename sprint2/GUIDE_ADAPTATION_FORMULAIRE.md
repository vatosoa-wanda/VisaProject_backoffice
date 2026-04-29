════════════════════════════════════════════════════════════════════════════════
  GUIDE D'ADAPTATION DU FORMULAIRE.HTML (Sprint 1 → Sprint 2)
════════════════════════════════════════════════════════════════════════════════

RÉSUMÉ
────────────────────────────────────────────────────────────────────────────────
✅ Conserver 100% de la structure HTML actuelle
✅ Ajouter 2 sections optionnelles (ancien/nouveau passeport)
✅ Ajouter du Thymeleaf dynamique pour l'affichage conditionnel
✅ Ajouter un peu de JavaScript pour UX (affichage/masquage sections)
✅ DTOs enrichis pour passer les données de contexte


════════════════════════════════════════════════════════════════════════════════
1. MODIFICATIONS DANS DemandeCreateDTO
════════════════════════════════════════════════════════════════════════════════

Ajouter ces champs (tous optionnels = null pour NOUVELLE) :

```java
// ── Nouveau bloc optionnel pour TRANSFERT sans antériorité ───────
@Valid
private PasseportDTO passeportNouveauDTO;  // Pour TRANSFERT sans antécédent

// ── Contexte de la demande ──────────────────────────────────────
private Long idDemandeOrigine;              // Pour DUPLICATA/TRANSFERT avec antécédent
private String typeDemande;                 // "NOUVELLE" | "DUPLICATA" | "TRANSFERT"
private Boolean avecAntecedent;             // true = avec, false = sans
```

Utilisation :
- Demande NOUVELLE        : tous les champs de base, idDemandeOrigine=null, typeDemande="NOUVELLE"
- Duplicata avec antécédent : idDemandeOrigine=1, typeDemande="DUPLICATA", avecAntecedent=true
- Duplicata sans antécédent : tous les champs de base, typeDemande="DUPLICATA", avecAntecedent=false
- Transfert avec antécédent : idDemandeOrigine=1, passeportNouveauDTO, typeDemande="TRANSFERT", avecAntecedent=true
- Transfert sans antécédent : tous les champs + passeportNouveauDTO, typeDemande="TRANSFERT", avecAntecedent=false


════════════════════════════════════════════════════════════════════════════════
2. MODIFICATIONS DANS formulaire.html
════════════════════════════════════════════════════════════════════════════════

┌─ ÉTAPE 1 : Ajouter un Bloc TITRE DYNAMIQUE ──────────────────────────────────

Remplacer :
    <h1 class="mb-4" th:text="${pageTitle != null ? pageTitle : 'DEMANDE DE VISA TRANSFORMABLE'}">
        DEMANDE DE VISA TRANSFORMABLE
    </h1>

Par :
    <!-- Titre dynamique selon le type de demande -->
    <div th:if="${demandeForm != null && demandeForm.typeDemande != null}">
        <h1 class="mb-4">
            <span th:if="${demandeForm.typeDemande == 'NOUVELLE'}">NOUVELLE DEMANDE DE VISA</span>
            <span th:if="${demandeForm.typeDemande == 'DUPLICATA'}">DEMANDE DE DUPLICATA</span>
            <span th:if="${demandeForm.typeDemande == 'TRANSFERT'}">DEMANDE DE TRANSFERT</span>
        </h1>
        <!-- Bannière pour cas sans antécédent -->
        <div th:if="${demandeForm.avecAntecedent == false}"
             class="alert alert-info mb-4">
            <strong>Étape 1/2 :</strong> Votre dossier sera approuvé automatiquement, 
            puis <span th:if="${demandeForm.typeDemande == 'DUPLICATA'}">le duplicata</span>
            <span th:if="${demandeForm.typeDemande == 'TRANSFERT'}">le transfert</span> 
            sera créé.
        </div>
    </div>

    <!-- Fallback si pas de typeDemande (pour Sprint 1) -->
    <div th:if="${demandeForm == null || demandeForm.typeDemande == null}">
        <h1 class="mb-4" th:text="${pageTitle != null ? pageTitle : 'DEMANDE DE VISA TRANSFORMABLE'}">
            DEMANDE DE VISA TRANSFORMABLE
        </h1>
    </div>

┌─ ÉTAPE 2 : Bloc INFOS HÉRITÉES (lecture seule) si WITH ANTÉCÉDENT ────────────

Ajouter après le titre et avant le formulaire :

    <!-- Section INFO HÉRITÉES (lecture seule) si avec antécédent -->
    <div th:if="${demandeForm != null && demandeForm.avecAntecedent == true}"
         class="section-header bg-warning-light mb-4">
        <h2>Informations héritées du dossier existant</h2>
        <p class="text-muted">Ces informations ne peuvent pas être modifiées.</p>
    </div>

    <!-- Bloc ÉTAT CIVIL (READ-ONLY) -->
    <div th:if="${demandeForm != null && demandeForm.avecAntecedent == true}"
         class="mb-4 p-3 bg-light rounded">
        <h3>État Civil (lecture seule)</h3>
        <div class="row">
            <div class="col-md-6">
                <p><strong>Nom :</strong> <span th:text="${demandeForm.demandeurDTO?.nom}"></span></p>
            </div>
            <div class="col-md-6">
                <p><strong>Prénom :</strong> <span th:text="${demandeForm.demandeurDTO?.prenom}"></span></p>
            </div>
        </div>
        <div class="row">
            <div class="col-md-6">
                <p><strong>Date de naissance :</strong> 
                   <span th:text="${#temporals.format(demandeForm.demandeurDTO?.dateNaissance, 'dd/MM/yyyy')}"></span>
                </p>
            </div>
            <div class="col-md-6">
                <p><strong>Nationalité :</strong> 
                   <span th:text="${demandeForm.demandeurDTO?.nationaliteLibelle}"></span>
                </p>
            </div>
        </div>
    </div>

    <!-- Bloc ANCIEN PASSEPORT (READ-ONLY) -->
    <div th:if="${demandeForm != null && demandeForm.avecAntecedent == true}"
         class="mb-4 p-3 bg-light rounded">
        <h3>Passeport existant (lecture seule)</h3>
        <div class="row">
            <div class="col-md-6">
                <p><strong>Numéro :</strong> <span th:text="${demandeForm.passeportDTO?.numero}"></span></p>
            </div>
            <div class="col-md-6">
                <p><strong>Expiration :</strong> 
                   <span th:text="${#temporals.format(demandeForm.passeportDTO?.dateExpiration, 'dd/MM/yyyy')}"></span>
                </p>
            </div>
        </div>
    </div>

    <!-- Bloc ANCIEN VISA (READ-ONLY) si TRANSFERT -->
    <div th:if="${demandeForm != null && demandeForm.avecAntecedent == true && demandeForm.typeDemande == 'TRANSFERT'}"
         class="mb-4 p-3 bg-light rounded">
        <h3>Visa existant (lecture seule)</h3>
        <div class="row">
            <div class="col-md-6">
                <p><strong>Référence :</strong> <span th:text="${demandeForm.visaDTO?.referenceVisa}"></span></p>
            </div>
            <div class="col-md-6">
                <p><strong>Expiration :</strong> 
                   <span th:text="${#temporals.format(demandeForm.visaDTO?.dateExpiration, 'dd/MM/yyyy')}"></span>
                </p>
            </div>
        </div>
    </div>

┌─ ÉTAPE 3 : Bloc FORM REMPLIS si SANS ANTÉCÉDENT ou NOUVELLE ──────────────────

Garder les blocs existants (État civil, Passeport, Visa, etc.) mais les entourer avec :

    <!-- Blocs du formulaire — affichés si NOUVELLE ou (DUPLICATA/TRANSFERT sans antécédent) -->
    <div th:if="${demandeForm == null || demandeForm.avecAntecedent != true}">
        
        <!-- Bloc 🟦 ÉTAT CIVIL (Dev1) -->
        <div class="section-header">
            <h2>État Civil</h2>
        </div>
        <!-- ... contenu existant ... -->
        
        <!-- Bloc 🟩 PASSEPORT (Dev1) -->
        <div class="section-header">
            <h2>Passeport</h2>
        </div>
        <!-- ... contenu existant ... -->
        
        <!-- Bloc 🟨 VISA TRANSFORMABLE (Dev2) -->
        <div class="section-header">
            <h2>Visa Transformable</h2>
        </div>
        <!-- ... contenu existant ... -->
    </div>

┌─ ÉTAPE 4 : Bloc NOUVEAU PASSEPORT si TRANSFERT ─────────────────────────────

Ajouter dans la section formulaire (après passeport existant) :

    <!-- Bloc NOUVEAU PASSEPORT (visible si TRANSFERT) -->
    <div th:if="${demandeForm != null && demandeForm.typeDemande == 'TRANSFERT'}">
        <div class="section-header">
            <h2>Nouveau Passeport <span class="badge-obligatoire">*</span></h2>
            <p class="text-muted">Remplissez les informations du nouveau passeport.</p>
        </div>
        
        <div class="row">
            <div class="col-md-6 mb-3">
                <label for="numeroNouveauPasseport" class="form-label">
                    Numéro <span class="badge-obligatoire">*</span>
                </label>
                <input type="text" class="form-control" 
                       id="numeroNouveauPasseport" 
                       th:field="*{passeportNouveauDTO.numero}" 
                       th:required="${demandeForm.typeDemande == 'TRANSFERT'}"
                       placeholder="Doit être différent du passeport existant">
                <small class="form-text text-muted">
                    Ce numéro doit être différent du passeport existant.
                </small>
                <div th:errors="*{passeportNouveauDTO.numero}" 
                     class="alert-error text-danger"></div>
            </div>
            
            <div class="col-md-6 mb-3">
                <label for="dateDelivranceNouveauPasseport" class="form-label">
                    Date de délivrance <span class="badge-obligatoire">*</span>
                </label>
                <input type="date" class="form-control" 
                       id="dateDelivranceNouveauPasseport" 
                       name="passeportNouveauDTO.dateDelivrance"
                       th:value="${demandeForm != null && demandeForm.passeportNouveauDTO != null && demandeForm.passeportNouveauDTO.dateDelivrance != null ? #temporals.format(demandeForm.passeportNouveauDTO.dateDelivrance, 'yyyy-MM-dd') : ''}"
                       th:required="${demandeForm.typeDemande == 'TRANSFERT'}">
                <div th:errors="*{passeportNouveauDTO.dateDelivrance}" 
                     class="alert-error text-danger"></div>
            </div>
        </div>
        
        <div class="row">
            <div class="col-md-6 mb-3">
                <label for="dateExpirationNouveauPasseport" class="form-label">
                    Date d'expiration <span class="badge-obligatoire">*</span>
                </label>
                <input type="date" class="form-control" 
                       id="dateExpirationNouveauPasseport" 
                       name="passeportNouveauDTO.dateExpiration"
                       th:value="${demandeForm != null && demandeForm.passeportNouveauDTO != null && demandeForm.passeportNouveauDTO.dateExpiration != null ? #temporals.format(demandeForm.passeportNouveauDTO.dateExpiration, 'yyyy-MM-dd') : ''}"
                       th:required="${demandeForm.typeDemande == 'TRANSFERT'}">
                <div th:errors="*{passeportNouveauDTO.dateExpiration}" 
                     class="alert-error text-danger"></div>
            </div>
        </div>
    </div>

┌─ ÉTAPE 5 : Bloc DEMANDE et PIÈCES (conservé) ───────────────────────────────

Garder tel quel, mais entourer avec condition d'affichage :

    <!-- Bloc 🟥 DEMANDE (Dev2) — toujours affiché -->
    <div class="section-header">
        <h2>Demande</h2>
    </div>
    <!-- Type visa : Select (réutilisé pour tous les types) -->
    <!-- ... contenu existant ... -->

    <!-- Bloc 🟪 PIÈCES À FOURNIR (Dev2) — toujours affiché -->
    <!-- ... contenu existant ... -->


════════════════════════════════════════════════════════════════════════════════
3. DONNÉES PASSÉES PAR LE CONTRÔLEUR
════════════════════════════════════════════════════════════════════════════════

DemandeCreateDTO enrichi avec :
├─ typeDemande : "NOUVELLE" | "DUPLICATA" | "TRANSFERT"
├─ avecAntecedent : true | false (null pour NOUVELLE)
├─ idDemandeOrigine : Long (null pour NOUVELLE ou sans antécédent)
└─ passeportNouveauDTO : PasseportDTO (null sauf pour TRANSFERT)

Model Thymeleaf :
├─ demandeForm : DemandeCreateDTO enrichi
├─ typesVisa : List<TypeVisaDTO>
├─ situationsFamiliales : List<SituationFamilialeDTO>
├─ nationalites : List<NationaliteDTO>
├─ piecesCommunes : List<PieceDTO>
├─ piecesSpecifiques : List<PieceDTO>
├─ pageTitle : String (optionnel)
├─ formAction : String (URL du POST)
├─ submitLabel : String (optionnel)
└─ cancelUrl : String (optionnel)


════════════════════════════════════════════════════════════════════════════════
4. EXEMPLES D'APPELS CONTRÔLEUR
════════════════════════════════════════════════════════════════════════════════

EXEMPLE 1 : Nouvelle demande (Sprint 1 — inchangé)
───────────────────────────────────────────────────
GET /demandes/nouvelle

DemandeCreateDTO form = new DemandeCreateDTO();
form.setTypeDemande("NOUVELLE");
form.setAvecAntecedent(false);
model.addAttribute("demandeForm", form);
model.addAttribute("formAction", "/demandes/nouvelle");
// ... autres attributs ...
return "demande/formulaire";


EXEMPLE 2 : Duplicata avec antécédent
───────────────────────────────────────
GET /duplicata/formulaire?idDemandOrigine=1

Demande origine = demandeService.getDemande(1);
DemandeCreateDTO form = new DemandeCreateDTO();
form.setTypeDemande("DUPLICATA");
form.setAvecAntecedent(true);
form.setIdDemandeOrigine(1);
// Copier les données de l'origine
form.setDemandeurDTO(demandeurMapper.toDTO(origine.getDemandeur()));
form.setPasseportDTO(passeportMapper.toDTO(origine.getDemandeur().getPasseport()));
form.setVisaDTO(visaTransformableMapper.toDTO(origine.getVisaTransformable()));
// typeVisa aussi
model.addAttribute("demandeForm", form);
model.addAttribute("formAction", "/duplicata/formulaire");
// ... autres attributs ...
return "demande/formulaire";


EXEMPLE 3 : Duplicata sans antécédent
───────────────────────────────────────
GET /duplicata/formulaire

DemandeCreateDTO form = new DemandeCreateDTO();
form.setTypeDemande("DUPLICATA");
form.setAvecAntecedent(false);
form.setIdDemandeOrigine(null);
model.addAttribute("demandeForm", form);
model.addAttribute("formAction", "/duplicata/formulaire");
// ... autres attributs ...
return "demande/formulaire";


EXEMPLE 4 : Transfert avec antécédent
───────────────────────────────────────
GET /transfert/formulaire?idDemandOrigine=1

Demande origine = demandeService.getDemande(1);
DemandeCreateDTO form = new DemandeCreateDTO();
form.setTypeDemande("TRANSFERT");
form.setAvecAntecedent(true);
form.setIdDemandeOrigine(1);
// Copier les données de l'origine
form.setDemandeurDTO(...);
form.setPasseportDTO(...);
form.setVisaDTO(...);
// Nouveau passeport : initialisé à vide (l'utilisateur le remplira)
form.setPasseportNouveauDTO(new PasseportDTO());
model.addAttribute("demandeForm", form);
model.addAttribute("formAction", "/transfert/formulaire");
// ... autres attributs ...
return "demande/formulaire";


EXEMPLE 5 : Transfert sans antécédent
───────────────────────────────────────
GET /transfert/formulaire

DemandeCreateDTO form = new DemandeCreateDTO();
form.setTypeDemande("TRANSFERT");
form.setAvecAntecedent(false);
form.setIdDemandeOrigine(null);
form.setPasseportNouveauDTO(new PasseportDTO()); // Prêt à être rempli
model.addAttribute("demandeForm", form);
model.addAttribute("formAction", "/transfert/formulaire");
// ... autres attributs ...
return "demande/formulaire";


════════════════════════════════════════════════════════════════════════════════
5. VALIDATION CÔTÉ CLIENT (JavaScript optionnel)
════════════════════════════════════════════════════════════════════════════════

Ajouter dans formulaire.html avant la fermeture du body :

<script>
// Validation pour TRANSFERT : numéro nouveau passeport ≠ ancien
document.addEventListener('DOMContentLoaded', function() {
    const form = document.querySelector('form');
    if (!form) return;
    
    const typeDemande = form.querySelector('[name*="typeDemande"]')?.value || '';
    if (typeDemande !== 'TRANSFERT') return;
    
    const numeroAncien = form.querySelector('[id="numeroPasSeport"]')?.value || '';
    const form_submit = form.querySelector('button[type="submit"]');
    
    form.addEventListener('submit', function(e) {
        const numeroNouveau = form.querySelector('[name="passeportNouveauDTO.numero"]')?.value || '';
        
        if (numeroNouveau === numeroAncien && numeroNouveau !== '') {
            e.preventDefault();
            alert('Le numéro du nouveau passeport doit être différent de l\'ancien.');
            return false;
        }
    });
});
</script>

════════════════════════════════════════════════════════════════════════════════
6. RÉSUMÉ DES CHANGEMENTS
════════════════════════════════════════════════════════════════════════════════

✅ DemandeCreateDTO : +4 champs optionnels
✅ formulaire.html : 
   • +1 titre dynamique avec bannière
   • +4 sections read-only (état civil, passports, visa)
   • +1 bloc nouveau passeport
   • Affichages conditionnels avec th:if
   • ~50 lignes ajoutées, 0 supprimées, 100% réutilisable

✅ Contrôleur :
   • Pas de changements pour /demandes/nouvelle (Sprint 1 inchangé)
   • Nouvelles routes : /duplicata/formulaire, /transfert/formulaire
   • Logique partagée pour passer le contexte au formulaire

✅ Tests :
   • Unitaires : validation du DTO enrichi
   • E2E : tous les chemins (NOUVELLE, DUPLICATA ×2, TRANSFERT ×2)

════════════════════════════════════════════════════════════════════════════════
