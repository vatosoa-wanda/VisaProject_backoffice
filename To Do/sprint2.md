Voici les **versions finales corrigées** des scénarios et flux pour chaque cas, en intégrant toutes les règles métier définies précédemment.

---

# RÉCAPITULATIF DES RÈGLES MÉTIER FINALES

| Règle | Description |
|-------|-------------|
| **RG-01** | Une demande NOUVELLE approuvée crée automatiquement un `visa` ET une `carte_resident` |
| **RG-02** | Le **DUPLICATA** concerne uniquement la `carte_resident` (perte, vol, détérioration) |
| **RG-03** | Le **TRANSFERT** concerne uniquement le `visa` (passeport perdu, volé, expiré) |
| **RG-04** | Avec données antérieures : le DUPLICATA / TRANSFERT référence directement une demande NOUVELLE APPROUVEE existante |
| **RG-05** | Sans données antérieures : on crée d'abord une demande NOUVELLE → approbation **automatique** → puis DUPLICATA / TRANSFERT |
| **RG-06** | Le DUPLICATA crée une **nouvelle** `carte_resident` (pas de nouveau `visa`) |
| **RG-07** | Le TRANSFERT crée un **nouveau** `visa` (pas de nouvelle `carte_resident`) |
| **RG-08** | `id_demande_origine` pointe toujours vers une demande de type NOUVELLE avec statut APPROUVEE |

---

# PARTIE 1 : DUPLICATA (Carte résident)

## 1.1 DUPLICATA AVEC DONNÉES ANTÉRIEURES

### Contexte
- Demandeur déjà en base avec une demande NOUVELLE APPROUVEE (id=1)
- Carte résident perdue
- Passeport et visa encore valables

### Flux

```
Recherche du demandeur existant (par nom / passeport)
    ↓
Sélection de la demande NOUVELLE APPROUVEE (id=1)
    ↓
Création Demande type=DUPLICATA, statut=CREE
    avec id_demande_origine = 1
    ↓
(Les informations : demandeur, passeport, visa_transformable sont héritées)
    ↓
Création d'une NOUVELLE carte_resident
    (liée à la demande DUPLICATA)
    ↓
(Le visa reste inchangé, toujours lié à la demande origine id=1)
    ↓
Approbation du duplicata → statut=APPROUVEE
    ↓
Transaction terminée : nouvelle carte résident délivrée
```

### Schéma des liens après transaction

```
demande (id=1) NOUVELLE APPROUVEE
    ├── visa (id=1)
    └── carte_resident (id=1) ← ancienne carte (perdue)

demande (id=2) DUPLICATA APPROUVEE (id_demande_origine=1)
    └── carte_resident (id=2) ← nouvelle carte délivrée
```

## 1.2 DUPLICATA SANS DONNÉES ANTÉRIEURES

### Contexte
- Demandeur inconnu du système
- Carte résident perdue
- Possède encore passeport valide et visa valable

### Flux

```
Saisie complète (demandeur + passeport + visa existant sur passeport)
    ↓
Création Demande type=NOUVELLE, statut=CREE
    ↓
Approbation AUTOMATIQUE → statut=APPROUVEE
    (l'agent a constaté la perte, le visa existe déjà)
    ↓
Création AUTOMATIQUE du visa ET de la carte_resident
    (liés à la demande NOUVELLE)
    ↓
Création Demande type=DUPLICATA, statut=CREE
    avec id_demande_origine = id de la demande NOUVELLE APPROUVEE
    ↓
Création d'une NOUVELLE carte_resident
    (liée à la demande DUPLICATA)
    ↓
(Le visa reste inchangé, lié à la demande NOUVELLE)
    ↓
Approbation du duplicata → statut=APPROUVEE
    ↓
Transaction terminée : nouvelle carte résident délivrée
```

### Schéma des liens après transaction

```
demande (id=1) NOUVELLE APPROUVEE
    ├── visa (id=1)
    └── carte_resident (id=1) ← première carte (créée mais remplacée)

demande (id=2) DUPLICATA APPROUVEE (id_demande_origine=1)
    └── carte_resident (id=2) ← nouvelle carte délivrée
```


# PARTIE 2 : TRANSFERT (Visa)

## 2.1 TRANSFERT AVEC DONNÉES ANTÉRIEURES

### Contexte
- Demandeur déjà en base avec une demande NOUVELLE APPROUVEE (id=1)
- Passeport expiré (ou perdu/volé)
- Obtention d'un nouveau passeport
- Carte résident toujours valide

### Flux

```
Recherche du demandeur existant (par nom / passeport)
    ↓
Sélection de la demande NOUVELLE APPROUVEE (id=1)
    ↓
Saisie du nouveau passeport
    ↓
Création Demande type=TRANSFERT, statut=CREE
    avec id_demande_origine = 1
    ↓
Création d'un NOUVEAU visa
    (lié au nouveau passeport ET à la demande TRANSFERT)
    ↓
(La carte résident reste inchangée, toujours liée à la demande origine id=1)
    ↓
(L'ancien visa peut être marqué inactif)
    ↓
Approbation du transfert → statut=APPROUVEE
    ↓
Transaction terminée : nouveau visa délivré sur le nouveau passeport
```

### Schéma des liens après transaction

```
demande (id=1) NOUVELLE APPROUVEE
    ├── visa (id=1) ← ancien visa (inactif, lié à ancien passeport)
    └── carte_resident (id=1) ← inchangée

demande (id=2) TRANSFERT APPROUVEE (id_demande_origine=1)
    └── visa (id=2) ← nouveau visa (actif, lié au nouveau passeport)
```

## 2.2 TRANSFERT SANS DONNÉES ANTÉRIEURES

### Contexte
- Demandeur inconnu du système
- Passeport endommagé (ou perdu/volé/expiré)
- Visa encore valable
- Obtention d'un nouveau passeport

### Flux

```
Saisie complète (demandeur + ancien passeport + nouveau passeport + visa existant)
    ↓
Création Demande type=NOUVELLE, statut=CREE
    ↓
Approbation AUTOMATIQUE → statut=APPROUVEE
    (l'agent a constaté que le visa existe, seul le passeport change)
    ↓
Création AUTOMATIQUE du visa ET de la carte_resident
    (liés à la demande NOUVELLE)
    ↓
Création Demande type=TRANSFERT, statut=CREE
    avec id_demande_origine = id de la demande NOUVELLE APPROUVEE
    ↓
Création d'un NOUVEAU visa
    (lié au nouveau passeport ET à la demande TRANSFERT)
    ↓
(La carte résident reste liée à la demande NOUVELLE)
    ↓
(L'ancien visa peut être marqué inactif)
    ↓
Approbation du transfert → statut=APPROUVEE
    ↓
Transaction terminée : nouveau visa délivré sur le nouveau passeport
```

### Schéma des liens après transaction

```
demande (id=1) NOUVELLE APPROUVEE
    ├── visa (id=1) ← ancien visa (inactif, lié à ancien passeport)
    └── carte_resident (id=1) ← inchangée

demande (id=2) TRANSFERT APPROUVEE (id_demande_origine=1)
    └── visa (id=2) ← nouveau visa (actif, lié au nouveau passeport)
```

---

# TABLEAU RÉCAPITULATIF FINAL DES FLUX
(N/A : Non Automatique)
| Cas | Type | Données antérieures | 1ʳᵉ demande | Approbation 1ʳᵉ | 2ᵉ demande | Création |
|-----|------|---------------------|-------------|-----------------|------------|----------|
| 1.1 | DUPLICATA | ✅ OUI | (existe déjà) | N/A | DUPLICATA | Nouvelle carte |
| 1.2 | DUPLICATA | ❌ NON | NOUVELLE | Automatique | DUPLICATA | Nouvelle carte |
| 2.1 | TRANSFERT | ✅ OUI | (existe déjà) | N/A | TRANSFERT | Nouveau visa |
| 2.2 | TRANSFERT | ❌ NON | NOUVELLE | Automatique | TRANSFERT | Nouveau visa |

---

# RÉSUMÉ DES CRÉATIONS PAR TYPE DE DEMANDE APPROUVÉE

| Type demande | Crée un `visa` | Crée une `carte_resident` |
|--------------|----------------|---------------------------|
| NOUVELLE (classique) | ✅ OUI | ✅ OUI |
| NOUVELLE (pour duplicata/transfert sans antériorité) | ✅ OUI | ✅ OUI |
| DUPLICATA | ❌ NON | ✅ OUI (nouvelle) |
| TRANSFERT | ✅ OUI (nouveau) | ❌ NON |