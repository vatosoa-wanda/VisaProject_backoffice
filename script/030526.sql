\c postgres;

DROP DATABASE IF EXISTS visa_db;
CREATE DATABASE visa_db;
\c visa_db;

CREATE TABLE situation_familiale (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) UNIQUE
);

INSERT INTO situation_familiale (libelle) VALUES
('Célibataire'),
('Marié'),
('Divorcé'),
('Veuf');

CREATE TABLE nationalite (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100) UNIQUE
);

INSERT INTO nationalite (libelle) VALUES
('Malagasy'),
('Française'),
('Chinoise'),
('Indienne'),
('Comorienne');

CREATE TABLE demandeur (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100) NOT NULL,
    prenom VARCHAR(100),
    nom_jeune_fille VARCHAR(100),
    date_naissance DATE NOT NULL,
    lieu_naissance VARCHAR(150),
    id_situation_familiale INT,
    id_nationalite INT NOT NULL,
    profession VARCHAR(100),
    adresse_madagascar TEXT NOT NULL,
    telephone VARCHAR(20) NOT NULL,
    email VARCHAR(150),
    FOREIGN KEY (id_situation_familiale) REFERENCES situation_familiale(id),
    FOREIGN KEY (id_nationalite) REFERENCES nationalite(id)
);

CREATE TABLE passeport (
    id SERIAL PRIMARY KEY,
    numero VARCHAR(50) UNIQUE,
    date_delivrance DATE,
    date_expiration DATE,
    id_demandeur INT, 
    FOREIGN KEY (id_demandeur) REFERENCES demandeur(id)
);

CREATE TABLE visa_transformable (
    id SERIAL PRIMARY KEY,
    reference_visa VARCHAR(100),
    date_entree DATE,
    lieu_entree VARCHAR(100),
    date_expiration DATE,
    id_demandeur INT,
    id_passeport INT, 
    FOREIGN KEY (id_demandeur) REFERENCES demandeur(id),
    FOREIGN KEY (id_passeport) REFERENCES passeport(id)
);

CREATE TABLE type_visa (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(100)
);

INSERT INTO type_visa (libelle) VALUES
('Travailleur'),
('Investisseur');

CREATE TABLE type_demande (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) UNIQUE
);

INSERT INTO type_demande (libelle) VALUES
('NOUVELLE'),
('DUPLICATA');

-- 24/04/2026
INSERT INTO type_demande (libelle) VALUES ('TRANSFERT');

CREATE TABLE statut_demande (
    id SERIAL PRIMARY KEY,
    libelle VARCHAR(50) UNIQUE
);

INSERT INTO statut_demande (libelle) VALUES
('CREE'),
('TERMINEE'),
('APPROUVEE'),
('REFUSEE');

-- Sprint 3
-- Important : insertion séparée pour ne pas décaler les IDs des statuts ci-dessus.
INSERT INTO statut_demande (libelle) VALUES ('SCAN_TERMINE');


CREATE TABLE demande (
    id SERIAL PRIMARY KEY,
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_demandeur INT,
    id_visa_transformable INT,
    id_type_visa INT,
    id_type_demande INT,
    id_statut_demande INT,

    FOREIGN KEY (id_type_demande) REFERENCES type_demande(id),
    FOREIGN KEY (id_statut_demande) REFERENCES statut_demande(id),
    FOREIGN KEY (id_demandeur) REFERENCES demandeur(id),
    FOREIGN KEY (id_visa_transformable) REFERENCES visa_transformable(id),
    FOREIGN KEY (id_type_visa) REFERENCES type_visa(id)
);

-- 24/04/2026
ALTER TABLE demande
ADD COLUMN id_demande_origine INT NULL;

ALTER TABLE demande
ADD CONSTRAINT fk_demande_origine
FOREIGN KEY (id_demande_origine) REFERENCES demande(id);


CREATE TABLE visa (
    id SERIAL PRIMARY KEY,
    reference_visa VARCHAR(100),
    date_debut DATE,
    date_fin DATE,
    id_passeport INT,
    id_demande INT,
    FOREIGN KEY (id_passeport) REFERENCES passeport(id),
    FOREIGN KEY (id_demande) REFERENCES demande(id)
);

CREATE TABLE carte_resident (
    id SERIAL PRIMARY KEY,
    numero_carte VARCHAR(50) UNIQUE,
    date_debut DATE,
    date_fin DATE,
    id_passeport INT,
    id_demande INT,
    FOREIGN KEY (id_passeport) REFERENCES passeport(id),
    FOREIGN KEY (id_demande) REFERENCES demande(id)
);


CREATE TABLE historique_statut (
    id SERIAL PRIMARY KEY,
    id_demande INT,
    id_statut_demande INT,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_statut_demande) REFERENCES statut_demande(id)
);

CREATE TABLE type_piece (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE
);

INSERT INTO type_piece (code) VALUES
('COMMUN'),
('TRAVAILLEUR'),
('INVESTISSEUR');

CREATE TABLE piece (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255),
    obligatoire BOOLEAN DEFAULT FALSE,
    id_type_piece INT,
    FOREIGN KEY (id_type_piece) REFERENCES type_piece(id)
);

-- COMMUN
INSERT INTO piece (nom, obligatoire, id_type_piece) VALUES
('02 photos identité', TRUE, 1),
('Notice de renseignement', TRUE, 1),
('Lettre au ministère', TRUE, 1),
('Photocopie visa valide', TRUE, 1),
('Photocopie passeport', TRUE, 1),
('Carte résident valide', TRUE, 1),
('Certificat de résidence', TRUE, 1),
('Casier judiciaire < 3 mois', TRUE, 1);


-- INVESTISSEUR
INSERT INTO piece (nom, obligatoire, id_type_piece) VALUES
('Statut société', TRUE, 3),
('Registre commerce', TRUE, 3),
('Carte fiscale', TRUE, 3);


-- TRAVAILLEUR
INSERT INTO piece (nom, obligatoire, id_type_piece) VALUES
('Autorisation emploi', TRUE, 2),
('Attestation employeur', TRUE, 2);


CREATE TABLE demande_piece (
    id SERIAL PRIMARY KEY,
    id_demande INT,
    id_piece INT,
    fourni BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (id_demande) REFERENCES demande(id),
    FOREIGN KEY (id_piece) REFERENCES piece(id)
);

-- Table pour la gestion des documents uploadés
CREATE TABLE document (
    id BIGINT PRIMARY KEY GENERATED ALWAYS AS IDENTITY,
    id_demande BIGINT NOT NULL,
    id_piece BIGINT NOT NULL,
    nom_original VARCHAR(255) NOT NULL,
    chemin_fichier VARCHAR(500) NOT NULL,
    taille_fichier BIGINT,
    type_mime VARCHAR(100),
    date_creation TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_demande) REFERENCES demande(id) ON DELETE CASCADE,
    FOREIGN KEY (id_piece) REFERENCES piece(id)
);

CREATE INDEX idx_document_demande ON document(id_demande);
CREATE INDEX idx_document_piece ON document(id_piece);


-- 03/05/2026
-- Refactorisation de la table visa et création d'une table de relation
-- car un visa peut appartenir à plusieurs passeports

-- Créer la nouvelle table de relation visa_passeport AVANT de supprimer les colonnes
CREATE TABLE IF NOT EXISTS visa_passeport (
    id SERIAL PRIMARY KEY,
    id_visa INT NOT NULL,
    id_passeport INT NOT NULL,
    id_demande INT NOT NULL,
    date_association TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (id_visa) REFERENCES visa(id) ON DELETE CASCADE,
    FOREIGN KEY (id_passeport) REFERENCES passeport(id) ON DELETE CASCADE,
    FOREIGN KEY (id_demande) REFERENCES demande(id) ON DELETE CASCADE,
    UNIQUE(id_visa, id_passeport, id_demande)
);

-- Migrer les données existantes de visa vers visa_passeport
-- (avant de supprimer les colonnes id_passeport et id_demande)
INSERT INTO visa_passeport (id_visa, id_passeport, id_demande, date_association)
SELECT id, id_passeport, id_demande, CURRENT_TIMESTAMP
FROM visa
WHERE id_passeport IS NOT NULL AND id_demande IS NOT NULL
ON CONFLICT (id_visa, id_passeport, id_demande) DO NOTHING;

-- Supprimer les contraintes de clés étrangères de la table visa existante
ALTER TABLE visa DROP CONSTRAINT IF EXISTS visa_id_passeport_fkey;
ALTER TABLE visa DROP CONSTRAINT IF EXISTS visa_id_demande_fkey;

-- Supprimer les colonnes id_passeport et id_demande de la table visa
ALTER TABLE visa DROP COLUMN IF EXISTS id_passeport;
ALTER TABLE visa DROP COLUMN IF EXISTS id_demande;

-- Créer les index pour améliorer les performances
CREATE INDEX IF NOT EXISTS idx_visa_passeport_visa ON visa_passeport(id_visa);
CREATE INDEX IF NOT EXISTS idx_visa_passeport_passeport ON visa_passeport(id_passeport);
CREATE INDEX IF NOT EXISTS idx_visa_passeport_demande ON visa_passeport(id_demande);