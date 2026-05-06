# SPRINT 4 — FRONTEND VUE.JS (Application de consultation)

**TL    :** ETU003350 / HarimalalaEricka
**Dev 1 :** ETU003324 / Harenkantou
**Dev 2 :** ETU003366 / vatosoa-wanda

---

## OBJECTIF

Application Vue.js standalone qui appelle les API du backend Spring Boot.
- Recherche de demandes par numéro passeport ou numéro demande
- Affichage de la fiche complète d'une demande avec QR code
- Navigation depuis QR code scanné

---

## ÉTAPE 0 — INITIALISATION DU PROJET

### Tâche 0.1 — Création du projet Vue.js
**Responsable :** ETU003324

```bash
npm create vue@latest visa-frontend
# Options : Router ✅ | Pinia ✅ | Vitest ✅ | ESLint ✅
cd visa-frontend
npm install
npm install qrcode vue-qrcode-component axios
```

**Structure finale du projet :**
```
visa-frontend/
├── src/
│   ├── api/                  ← appels HTTP
│   │   ├── demandeApi.js
│   │   └── http.js
│   ├── components/
│   │   ├── DemandeCard.vue
│   │   ├── HistoriqueStatut.vue
│   │   ├── QRCodeBlock.vue
│   │   └── FicheDetail.vue
│   ├── views/
│   │   ├── RechercheView.vue
│   │   ├── ListeDemandesView.vue
│   │   └── FicheDemandeView.vue
│   ├── router/
│   │   └── index.js
│   ├── stores/
│   │   └── demandeStore.js
│   ├── App.vue
│   └── main.js
├── .env
├── .env.example
└── vite.config.js
```

### Tâche 0.2 — Configuration de base
**Responsable :** ETU003324

**Fichier : `.env`**
```
VITE_API_BASE_URL=http://localhost:8080
```

**Fichier : `.env.example`**
```
VITE_API_BASE_URL=http://localhost:8080
```

**Fichier : `src/api/http.js`**
```javascript
// ⚠️ REMPLACER BASE_URL par la vraie URL du backend après déploiement
import axios from 'axios'

const http = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL,
  timeout: 10000,
  headers: { 'Content-Type': 'application/json' }
})

export default http
```

---

## ÉTAPE 1 — COUCHE API (Mocks → Vrais endpoints)

### Tâche 1.1 — Définition des appels API
**Responsable :** ETU003324

**Fichier : `src/api/demandeApi.js`**

```javascript
import http from './http.js'

// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━
// ⚠️ ENDPOINTS À CONFIRMER AVEC LE BACKEND
// Remplacer les URLs mock par les vrais endpoints exposés
// ━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━━

/**
 * Recherche demandes par numéro de passeport
 * ⚠️ ENDPOINT À CONFIRMER : GET /demandes?numeroPasport={num}
 * Retourne : List<DemandeResumeeDTO> (ordre décroissant par date)
 */
export function getDemandesByPasseport(numeroPasseport) {
  return http.get('/demandes', { params: { numeroPasseport } })
}

/**
 * Récupère une demande par son ID
 * ⚠️ ENDPOINT À CONFIRMER : GET /demandes/{id}
 * Retourne : DemandeDetailDTO (avec demandeur, passeport, visa, pièces)
 */
export function getDemandeById(id) {
  return http.get(`/demandes/${id}`)
}

/**
 * Récupère l'historique des statuts d'une demande
 * ⚠️ ENDPOINT À CONFIRMER : GET /demandes/{id}/historique
 * Retourne : List<HistoriqueStatutDTO>
 */
export function getHistoriqueStatut(id) {
  return http.get(`/demandes/${id}/historique`)
}

/**
 * Récupère toutes les demandes liées au même demandeur
 * ⚠️ ENDPOINT À CONFIRMER : GET /demandeurs/{idDemandeur}/demandes
 * Retourne : List<DemandeResumeeDTO>
 */
export function getDemandesByDemandeur(idDemandeur) {
  return http.get(`/demandeurs/${idDemandeur}/demandes`)
}
```

---

## ÉTAPE 2 — STORE (ÉTAT GLOBAL)

### Tâche 2.1 — Store Pinia
**Responsable :** ETU003366

**Fichier : `src/stores/demandeStore.js`**

```javascript
import { defineStore } from 'pinia'
import { ref } from 'vue'
import * as api from '../api/demandeApi.js'

export const useDemandeStore = defineStore('demande', () => {
  const resultatsRecherche = ref([])    // liste demandes (recherche passeport)
  const demandeSelectionnee = ref(null) // demande en cours de consultation
  const autresDemandes = ref([])        // autres demandes du même demandeur
  const historique = ref([])            // historique statuts de la demande courante
  const loading = ref(false)
  const erreur = ref(null)

  async function rechercherParPasseport(numeroPasseport) {
    loading.value = true
    erreur.value = null
    try {
      const res = await api.getDemandesByPasseport(numeroPasseport)
      resultatsRecherche.value = res.data
    } catch (e) {
      erreur.value = 'Erreur lors de la recherche'
    } finally {
      loading.value = false
    }
  }

  async function chargerDemande(id) {
    loading.value = true
    erreur.value = null
    try {
      const [resDemande, resHistorique] = await Promise.all([
        api.getDemandeById(id),
        api.getHistoriqueStatut(id)
      ])
      demandeSelectionnee.value = resDemande.data
      historique.value = resHistorique.data

      // Charger les autres demandes du même demandeur
      const idDemandeur = resDemande.data.demandeur?.id
      if (idDemandeur) {
        const resAutres = await api.getDemandesByDemandeur(idDemandeur)
        autresDemandes.value = resAutres.data.filter(d => d.id !== Number(id))
      }
    } catch (e) {
      erreur.value = 'Demande introuvable'
    } finally {
      loading.value = false
    }
  }

  function reset() {
    resultatsRecherche.value = []
    demandeSelectionnee.value = null
    autresDemandes.value = []
    historique.value = []
    erreur.value = null
  }

  return {
    resultatsRecherche, demandeSelectionnee, autresDemandes,
    historique, loading, erreur,
    rechercherParPasseport, chargerDemande, reset
  }
})
```

---

## ÉTAPE 3 — ROUTER

### Tâche 3.1 — Configuration des routes
**Responsable :** ETU003324

**Fichier : `src/router/index.js`**

```javascript
import { createRouter, createWebHistory } from 'vue-router'
import RechercheView from '../views/RechercheView.vue'
import ListeDemandesView from '../views/ListeDemandesView.vue'
import FicheDemandeView from '../views/FicheDemandeView.vue'

const routes = [
  {
    path: '/',
    name: 'recherche',
    component: RechercheView
  },
  {
    // Résultats d'une recherche par passeport
    path: '/demandes',
    name: 'liste-demandes',
    component: ListeDemandesView
  },
  {
    // Fiche d'une demande (accès direct ou depuis liste ou QR code)
    path: '/demandes/:id',
    name: 'fiche-demande',
    component: FicheDemandeView
  }
]

export default createRouter({
  history: createWebHistory(),
  routes
})
```

**Routes accessibles :**
- `/` → page de recherche
- `/demandes?numeroPasseport=ABC123` → liste résultats
- `/demandes/:id` → fiche détaillée (accès direct QR code ou clic liste)

---

## ÉTAPE 4 — COMPOSANTS

### Tâche 4.1 — DemandeCard.vue
**Responsable :** ETU003366

**Fichier : `src/components/DemandeCard.vue`**

Props : `{ demande }` (DemandeResumeeDTO)

Affiche :
- ID demande
- Type demande (NOUVELLE / DUPLICATA / TRANSFERT)
- Type visa
- Date demande
- Statut courant (badge couleur : CREE=gris, SCAN_TERMINE=bleu, APPROUVEE=vert, REFUSEE=rouge)
- Émet : `@click` → navigation vers `/demandes/:id`

Fonctions internes :
- `badgeClass(statut)` → retourne la classe CSS selon le statut

---

### Tâche 4.2 — HistoriqueStatut.vue
**Responsable :** ETU003366

**Fichier : `src/components/HistoriqueStatut.vue`**

Props : `{ historique }` (List<HistoriqueStatutDTO>)

Affiche une liste ordonnée :
- Date changement (formatée)
- Statut
- Commentaire (si présent)

Fonctions internes :
- `formaterDate(dateString)` → retourne date lisible en français

---

### Tâche 4.3 — QRCodeBlock.vue
**Responsable :** ETU003324

**Fichier : `src/components/QRCodeBlock.vue`**

Props : `{ idDemande, numeroPasseport }`

Logique :
- Construit l'URL encodée dans le QR code :
  ```
  {VITE_APP_URL}/demandes/{idDemande}?numeroPasseport={numeroPasseport}
  ```
  ⚠️ `VITE_APP_URL` = URL publique du frontend (à configurer dans `.env`)
- Affiche le QR code via `qrcode` (canvas ou img)
- Bouton "Télécharger" : télécharge le QR code en PNG

Fonctions :
- `buildQrUrl(idDemande, numeroPasseport)` → construit l'URL
- `telechargerQr()` → déclenche le téléchargement du canvas en PNG

---

### Tâche 4.4 — FicheDetail.vue
**Responsable :** ETU003366

**Fichier : `src/components/FicheDetail.vue`**

Props : `{ demande, historique }`

Sections affichées (lecture seule) :
1. **En-tête** : référence demande, type, statut, date
2. **État civil** : nom, prénom, date naissance, situation familiale, nationalité
3. **Passeport** : numéro, date délivrance, date expiration
4. **Visa transformable** : référence, dates, lieu entrée (si présent)
5. **Type visa** : libellé
6. **Pièces justificatives** : liste avec statut FOURNI / NON FOURNI
7. **Historique des statuts** : composant `<HistoriqueStatut>`
8. **QR Code** : composant `<QRCodeBlock>`

---

## ÉTAPE 5 — VUES (PAGES)

### Tâche 5.1 — RechercheView.vue
**Responsable :** ETU003350

**Fichier : `src/views/RechercheView.vue`**

Logique :
- Deux inputs radio : `"Par numéro de passeport"` | `"Par numéro de demande"`
- Input texte selon choix
- Bouton "Rechercher"

Comportement selon choix :
- **Numéro demande** → `router.push('/demandes/' + idDemande)`
- **Numéro passeport** → `router.push({ name: 'liste-demandes', query: { numeroPasseport } })`

Fonctions :
- `onSubmit()` → valide input non vide, puis redirige
- `reset()` → vide les champs

Validation :
- Input vide → message d'erreur affiché sous le champ, pas d'appel API

---

### Tâche 5.2 — ListeDemandesView.vue
**Responsable :** ETU003350

**Fichier : `src/views/ListeDemandesView.vue`**

Logique :
- Au montage : lit `route.query.numeroPasseport` et appelle `store.rechercherParPasseport()`
- Affiche la liste triée par date décroissante (les plus récentes en premier)
- Chaque élément = `<DemandeCard>` cliquable → navigate vers `/demandes/:id`

États gérés :
- Loading : indicateur pendant la requête
- Erreur : message si la requête échoue
- Liste vide : message "Aucune demande trouvée pour ce passeport"

Fonctions :
- `charger()` → appelé à `onMounted` et si la query change

---

### Tâche 5.3 — FicheDemandeView.vue
**Responsable :** ETU003350

**Fichier : `src/views/FicheDemandeView.vue`**

Logique :
- Au montage : lit `route.params.id` et appelle `store.chargerDemande(id)`
- Affiche `<FicheDetail>` avec la demande sélectionnée

Sections supplémentaires (spécifiques à cette vue) :
- **Demande mise en évidence** (si accès par numéro demande) : badge "Demande sélectionnée"
- **Autres demandes du même demandeur** : liste de `<DemandeCard>` (cliquables)
  - Si aucune autre demande : masquer le bloc

États gérés :
- Loading, Erreur, Demande introuvable (affiche message + bouton retour)

Fonctions :
- `charger()` → appelé à `onMounted` et si `route.params.id` change (watch)

---

## ÉTAPE 6 — POINT D'ENTRÉE

### Tâche 6.1 — main.js et App.vue
**Responsable :** ETU003324

**Fichier : `src/main.js`**
```javascript
import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import router from './router'

const app = createApp(App)
app.use(createPinia())
app.use(router)
app.mount('#app')
```

**Fichier : `src/App.vue`**
- Header minimal : titre "Consultation Visa"
- Lien retour vers `/` (recherche)
- `<RouterView />`

---

## ÉTAPE 7 — TESTS UNITAIRES

### Tâche 7.1 — Tests store
**Responsable :** ETU003324

**Fichier : `src/stores/demandeStore.test.js`**

Tests à écrire avec Vitest + `@pinia/testing` :

```
✅ rechercherParPasseport() → résultats stockés dans resultatsRecherche
✅ rechercherParPasseport() erreur API → erreur définie, résultats vides
✅ chargerDemande(id) → demandeSelectionnee + historique + autresDemandes chargés
✅ chargerDemande(id) erreur API → erreur définie
✅ reset() → tous les champs remis à zéro
```

---

### Tâche 7.2 — Tests composants
**Responsable :** ETU003366

**Fichier : `src/components/DemandeCard.test.js`**
```
✅ Affiche le statut correct
✅ Applique la bonne classe CSS selon statut (CREE, APPROUVEE, REFUSEE, SCAN_TERMINE)
✅ Émet un événement click au clic
```

**Fichier : `src/components/QRCodeBlock.test.js`**
```
✅ buildQrUrl() retourne l'URL correcte avec idDemande et numeroPasseport
✅ Le QR code est rendu (canvas présent dans le DOM)
```

**Fichier : `src/components/HistoriqueStatut.test.js`**
```
✅ Affiche autant d'éléments que la liste historique reçue
✅ formaterDate() retourne une date lisible
```

---

### Tâche 7.3 — Tests vues
**Responsable :** ETU003350

**Fichier : `src/views/RechercheView.test.js`**
```
✅ onSubmit() avec input vide → erreur affichée, pas de navigation
✅ onSubmit() numéro demande → navigate vers /demandes/{id}
✅ onSubmit() numéro passeport → navigate vers /demandes?numeroPasseport=...
```

**Fichier : `src/views/ListeDemandesView.test.js`**
```
✅ Appelle rechercherParPasseport au montage avec la query
✅ Affiche le message vide si liste vide
✅ Affiche les DemandeCard si résultats présents
```

**Fichier : `src/views/FicheDemandeView.test.js`**
```
✅ Appelle chargerDemande au montage avec l'id de la route
✅ Affiche FicheDetail si demande chargée
✅ Affiche message erreur si demande introuvable
✅ Affiche les autres demandes du demandeur si présentes
```

---

## ÉTAPE 8 — INTÉGRATION BACKEND

### Tâche 8.1 — Checklist endpoints à confirmer avec le backend
**Responsable :** ETU003350 (coordination TL)

Avant de remplacer les mocks par les vrais appels, confirmer ces endpoints avec l'équipe backend :

| Fonction frontend | Endpoint attendu | Paramètres | Réponse attendue |
|---|---|---|---|
| `getDemandesByPasseport()` | `GET /demandes` | `?numeroPasseport=` | `List<DemandeResumeeDTO>` triée par date desc |
| `getDemandeById()` | `GET /demandes/{id}` | — | `DemandeDetailDTO` avec demandeur, passeport, visa, pièces |
| `getHistoriqueStatut()` | `GET /demandes/{id}/historique` | — | `List<HistoriqueStatutDTO>` |
| `getDemandesByDemandeur()` | `GET /demandeurs/{id}/demandes` | — | `List<DemandeResumeeDTO>` |

**DTOs minimum nécessaires côté backend :**

`DemandeResumeeDTO`
```
id, typeDemande, typeVisa, dateDemande, statut
```

`DemandeDetailDTO`
```
id, typeDemande, typeVisa, dateDemande, statut
demandeur: { id, nom, prenom, dateNaissance, situationFamiliale, nationalite }
passeport: { numero, dateDelivrance, dateExpiration }
visaTransformable: { referenceVisa, dateEntree, lieuEntree, dateExpiration }
pieces: [{ nom, fourni }]
```

`HistoriqueStatutDTO`
```
statut, dateChangement, commentaire
```

---

## ÉTAPE 9 — TEST GLOBAL DU SPRINT

```
1. Lancer backend Spring Boot (port 8080)
2. Lancer frontend Vue.js (port 5173)

Scénario A — Recherche par numéro passeport :
  ✅ Saisir numéro passeport → affiche liste demandes du demandeur
  ✅ Demandes triées par date décroissante
  ✅ Cliquer sur une demande → fiche détaillée
  ✅ Fiche affiche : état civil, passeport, visa, pièces, historique
  ✅ QR code généré et scannable

Scénario B — Recherche par numéro demande :
  ✅ Saisir numéro demande → fiche directe avec demande mise en évidence
  ✅ Autres demandes du demandeur affichées en dessous

Scénario C — Accès via QR code scanné :
  ✅ URL /demandes/{id}?numeroPasseport=... → fiche chargée directement
  ✅ Même rendu que l'accès via recherche

Scénario D — Cas d'erreur :
  ✅ Numéro demande inexistant → message d'erreur clair
  ✅ Numéro passeport sans demandes → "Aucune demande trouvée"
  ✅ Backend inaccessible → message d'erreur, pas de crash
```

---

## RÈGLES TECHNIQUES

- Pas de CSS framework externe (pas de Vuetify, Bootstrap, Tailwind) — style minimal inline ou CSS scoped uniquement
- Pas de dépendances inutiles
- Tous les endpoints backend marqués `⚠️ À CONFIRMER` dans `demandeApi.js` doivent être mis à jour dès validation avec l'équipe backend
- L'URL encodée dans le QR code doit pointer vers l'URL publique du frontend (configurable via `.env`)
- Aucune donnée sensible stockée en localStorage

---

## LIVRABLES DU SPRINT 4

```
visa-frontend/
├── .env.example
├── src/api/http.js
├── src/api/demandeApi.js
├── src/stores/demandeStore.js
├── src/router/index.js
├── src/components/DemandeCard.vue
├── src/components/HistoriqueStatut.vue
├── src/components/QRCodeBlock.vue
├── src/components/FicheDetail.vue
├── src/views/RechercheView.vue
├── src/views/ListeDemandesView.vue
├── src/views/FicheDemandeView.vue
├── src/App.vue
├── src/main.js
└── src/**/*.test.js
```
