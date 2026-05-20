-- Seed de test ETU003350 - cas AVEC antécédent
-- Objectif : fournir un dossier complet déjà APPROUVÉ pour tester la recherche et la création d'un duplicata.
-- Ce script simule le cas "AVEC antécédent" du sprint 2.
-- En le chargeant après script/030526.sql, tu obtiens une base avec :
--   - 1 demandeur
--   - 1 passeport
--   - 1 visa_transformable
--   - 1 demande de type NOUVELLE et statut APPROUVEE
--   - 1 historique_statut APPROUVEE
--   - 1 visa actif lié via la table visa_passeport
--   - 1 carte_resident active lié via la table carte_resident_passeport
-- Ces données servent à tester la recherche d'une demande approuvée, puis la création d'un duplicata.
--
-- IMPORTANT: Exécuter dans cet ordre:
--   1. script/030526.sql (création de la structure complète avec les tables de relation)
--   2. script/270426_Ericka.sql (données de test)

-- TABLE demandeur : identité du titulaire du dossier
INSERT INTO demandeur (id, nom, prenom, nom_jeune_fille, date_naissance, lieu_naissance, id_situation_familiale, id_nationalite, profession, adresse_madagascar, telephone, email)
VALUES (1001, 'Rasoa', 'Aina', NULL, '1992-03-14', 'Antananarivo', 1, 1, 'Comptable', 'Lot II A 12 Ankadifotsy, Antananarivo', '0340000001', 'aina.rasoa@example.com');

-- TABLE passeport : passeport associé au demandeur
INSERT INTO passeport (id, numero, date_delivrance, date_expiration, id_demandeur)
VALUES (2001, 'P-TEST-001', '2024-01-10', '2029-01-10', 1001);

-- TABLE visa_transformable : visa de base lié au passeport
INSERT INTO visa_transformable (id, reference_visa, date_entree, lieu_entree, date_expiration, id_demandeur, id_passeport)
VALUES (3001, 'VISA-T-001', '2024-01-15', 'Ivato', '2028-01-15', 1001, 2001);

-- Référentiels attendus depuis script/030526.sql :
--   type_visa id=1  -> Travailleur
--   type_demande id=1 -> NOUVELLE
--   statut_demande id=3 -> APPROUVEE

-- TABLE demande : dossier principal déjà approuvé
INSERT INTO demande (id, date_demande, id_demandeur, id_visa_transformable, id_type_visa, id_type_demande, id_statut_demande, id_demande_origine)
VALUES (4001, CURRENT_TIMESTAMP, 1001, 3001, 1, 1, 3, NULL);

-- TABLE historique_statut : traçabilité de l'approbation
INSERT INTO historique_statut (id, id_demande, id_statut_demande, date_changement, commentaire)
VALUES (5001, 4001, 3, CURRENT_TIMESTAMP, 'Approbation de test');

-- TABLE visa : visa actif (sans références directes à passeport et demande)
INSERT INTO visa (id, reference_visa, date_debut, date_fin)
VALUES (6001, 'VISA-ACTIF-001', CURRENT_DATE, NULL);

-- TABLE visa_passeport : association entre visa, passeport et demande
INSERT INTO visa_passeport (id_visa, id_passeport, id_demande, date_association)
VALUES (6001, 2001, 4001, CURRENT_TIMESTAMP);

-- TABLE carte_resident : carte initiale (sans références directes à passeport et demande)
INSERT INTO carte_resident (id, numero_carte, date_debut, date_fin)
VALUES (7001, 'CRD-TEST-001', CURRENT_DATE, NULL);

-- TABLE carte_resident_passeport : association entre carte_resident, passeport et demande
INSERT INTO carte_resident_passeport (id_carte_resident, id_passeport, id_demande, date_association)
VALUES (7001, 2001, 4001, CURRENT_TIMESTAMP);

