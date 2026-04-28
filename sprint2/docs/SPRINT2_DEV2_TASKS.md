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
