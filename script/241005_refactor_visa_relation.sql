\c visa_db;

-- 05/05/2026
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
