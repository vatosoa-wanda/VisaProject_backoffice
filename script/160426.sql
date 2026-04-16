\c postgres;

DROP DATABASE IF EXISTS visa_db;
CREATE DATABASE visa_db;
\c visa_db;

CREATE TABLE personne (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(100),
    prenom VARCHAR(100),
    nom_jeune_fille VARCHAR(100),
    date_naissance DATE,
    lieu_naissance VARCHAR(150),
    situation_familiale VARCHAR(50),
    nationalite VARCHAR(100),
    profession VARCHAR(100),
    adresse_madagascar TEXT,
    telephone VARCHAR(20),
    email VARCHAR(150)
);


CREATE TABLE passeport (
    id SERIAL PRIMARY KEY,
    numero VARCHAR(50) UNIQUE,
    date_delivrance DATE,
    date_expiration DATE,
    id_personne INT,
    FOREIGN KEY (id_personne) REFERENCES personne(id)
);

CREATE TABLE visa_actuel (
    id SERIAL PRIMARY KEY,
    numero_visa VARCHAR(50),
    date_entree DATE,
    lieu_entree VARCHAR(100),
    date_expiration DATE,
    reference_visa VARCHAR(100),
    id_personne INT,
    FOREIGN KEY (id_personne) REFERENCES personne(id)
);


CREATE TABLE type_visa (
    id SERIAL PRIMARY KEY,
    code VARCHAR(20), -- TRAVAILLEUR / INVESTISSEUR
    libelle VARCHAR(100)
);


INSERT INTO type_visa (code, libelle) VALUES
('TRAVAILLEUR', 'Visa Travailleur'),
('INVESTISSEUR', 'Visa Investisseur');

ALTER TABLE demande_visa
ADD COLUMN type_demande VARCHAR(50) DEFAULT 'NOUVELLE';

CREATE TABLE demande_visa (
    id SERIAL PRIMARY KEY,
    date_demande TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    id_personne INT,
    id_passeport INT,
    id_visa_actuel INT,
    id_type_visa INT,
    statut VARCHAR(50) DEFAULT 'EN_ATTENTE',

    FOREIGN KEY (id_personne) REFERENCES personne(id),
    FOREIGN KEY (id_passeport) REFERENCES passeport(id),
    FOREIGN KEY (id_visa_actuel) REFERENCES visa_actuel(id),
    FOREIGN KEY (id_type_visa) REFERENCES type_visa(id)
);

ALTER TABLE demande_visa
ADD COLUMN id_demande_originale INT;

ALTER TABLE demande_visa
ADD FOREIGN KEY (id_demande_originale) REFERENCES demande_visa(id);

CREATE TABLE statut (
    id SERIAL PRIMARY KEY,
    code VARCHAR(50) UNIQUE,
    libelle VARCHAR(100)
);

INSERT INTO statut (code, libelle) VALUES
('EN_ATTENTE', 'En attente'),
('EN_COURS', 'En cours de traitement'),
('VALIDEE', 'Validée'),
('REFUSEE', 'Refusée');

ALTER TABLE demande_visa
ADD COLUMN id_statut INT;

ALTER TABLE demande_visa
ADD FOREIGN KEY (id_statut) REFERENCES statut(id);

CREATE TABLE historique_statut (
    id SERIAL PRIMARY KEY,
    id_demande INT,
    id_statut INT,
    date_changement TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    commentaire TEXT,

    FOREIGN KEY (id_demande) REFERENCES demande_visa(id),
    FOREIGN KEY (id_statut) REFERENCES statut(id)
);


CREATE TABLE piece (
    id SERIAL PRIMARY KEY,
    nom VARCHAR(255),
    type VARCHAR(50) -- COMMUN / INVESTISSEUR / TRAVAILLEUR
);

-- Commun
INSERT INTO piece (nom, type) VALUES
('02 photos identité', 'COMMUN'),
('Notice de renseignement', 'COMMUN'),
('Lettre au ministère', 'COMMUN'),
('Photocopie visa valide', 'COMMUN'),
('Photocopie passeport', 'COMMUN'),
('Carte résident valide', 'COMMUN'),
('Certificat de résidence', 'COMMUN'),
('Casier judiciaire < 3 mois', 'COMMUN');

-- Investisseur
INSERT INTO piece (nom, type) VALUES
('Statut de la société', 'INVESTISSEUR'),
('Registre de commerce', 'INVESTISSEUR'),
('Carte fiscale', 'INVESTISSEUR');

-- Travailleur
INSERT INTO piece (nom, type) VALUES
('Autorisation emploi', 'TRAVAILLEUR'),
('Attestation employeur', 'TRAVAILLEUR');


CREATE TABLE demande_piece (
    id SERIAL PRIMARY KEY,
    id_demande INT,
    id_piece INT,
    fourni BOOLEAN DEFAULT FALSE,

    FOREIGN KEY (id_demande) REFERENCES demande_visa(id),
    FOREIGN KEY (id_piece) REFERENCES piece(id)
);

