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
- [ ] Verify statut=CREE, type=NOUVELLE
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
