<template>
  <div class="suivi-demande">
    <div class="container">
      <router-link to="/" class="back-link">← Retour à la liste</router-link>

      <div v-if="store.loading" class="loading">
        Chargement des détails de la demande...
      </div>

      <div v-else-if="store.error" class="error">
        Erreur : {{ store.error.message }}
      </div>

      <div v-else-if="demande" class="demande-details">
        <div class="demande-header">
          <h1>Demande #{{ demande.id }}</h1>
          <div class="status-badge" :class="getStatutClass(demande.statutDemande)">
            {{ demande.statutDemande }}
          </div>
        </div>

        <div class="demande-grid">
          <!-- Section Gauche: Informations -->
          <div class="demande-info">
            <h2>Informations de la demande</h2>
            
            <div class="info-section">
              <h3>Demandeur</h3>
              <p><strong>Nom :</strong> {{ demande.nomDemandeur }} {{ demande.prenomDemandeur }}</p>
              <p><strong>Type de visa :</strong> {{ demande.typeVisa }}</p>
              <p><strong>Référence :</strong> {{ demande.referenceVisa }}</p>
              <p><strong>Type de demande :</strong> {{ demande.typeDemande || 'N/A' }}</p>
            </div>

            <div class="info-section">
              <h3>Passeport</h3>
              <p><strong>Numéro :</strong> {{ demande.numeroPasSeport || "Non disponible" }}</p>
              <p><strong>Date de demande :</strong> {{ formatDate(demande.dateDemande) }}</p>
            </div>
            
            <div class="info-section">
              <h3>Pièces fournies</h3>
              <div v-if="demande.pieces && demande.pieces.length">
                <ul>
                  <li v-for="piece in demande.pieces" :key="piece.idPiece">
                    {{ piece.nomPiece }}
                    <span v-if="piece.obligatoire">(obligatoire)</span>
                    - <strong>{{ piece.fourni ? 'Fourni' : 'Non fourni' }}</strong>
                  </li>
                </ul>
              </div>
              <div v-else>
                <p>Aucune pièce listée.</p>
              </div>
            </div>

            <div class="info-section">
              <h3>Dates</h3>
              <p v-if="demande.dateCreation">
                <strong>Créée le :</strong> {{ formatDate(demande.dateCreation) }}
              </p>
              <p v-if="demande.dateModification">
                <strong>Modifiée le :</strong> {{ formatDate(demande.dateModification) }}
              </p>
            </div>

            <div class="actions">
              <router-link to="/" class="btn btn-primary">
                Retour à la liste
              </router-link>
            </div>
          </div>

          <!-- Section Droite: QR Code -->
          <div class="demande-qr">
            <QRCodeDisplay :demandeId="demande.id" :size="250" />
          </div>
        </div>
      </div>

      <div v-else class="not-found">
        Demande introuvable
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from "vue";
import { useRoute } from "vue-router";
import { useDemandeStore } from "../stores/demandeStore";
import QRCodeDisplay from "../components/QRCodeDisplay.vue";

const route = useRoute();
const store = useDemandeStore();
const demande = ref(null);

onMounted(async () => {
  try {
    const id = route.params.id;
    demande.value = await store.getDemandeById(id);
  } catch (error) {
    console.error("Erreur lors du chargement de la demande :", error);
  }
});

const formatDate = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("fr-FR");
};

const getStatutClass = (statut) => {
  const classMap = {
    CREE: "status-badge--cree",
    APPROUVEE: "status-badge--approuvee",
    REJETEE: "status-badge--rejetee",
    SCAN_TERMINE: "status-badge--scan-termine"
  };
  return classMap[statut] || "";
};
</script>

<style scoped>
.suivi-demande {
  min-height: 100vh;
  background: #f3f4f6;
  padding: 2rem 0;
}

.container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 1rem;
}

.back-link {
  display: inline-block;
  color: #2563eb;
  text-decoration: none;
  margin-bottom: 1rem;
  font-weight: 600;
}

.back-link:hover {
  text-decoration: underline;
}

.loading,
.error,
.not-found {
  text-align: center;
  padding: 2rem;
  background: white;
  border-radius: 0.75rem;
  margin: 2rem 0;
}

.error {
  background: #fee2e2;
  color: #7f1d1d;
}

.not-found {
  background: #dbeafe;
  color: #0c4a6e;
}

.demande-details {
  background: white;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.demande-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 2rem;
  border-bottom: 1px solid #e5e7eb;
  background: #f9fafb;
}

.demande-header h1 {
  margin: 0;
  color: #111827;
}

.status-badge {
  display: inline-block;
  padding: 0.5rem 1rem;
  border-radius: 9999px;
  font-weight: 600;
  font-size: 0.875rem;
}

.status-badge--cree {
  background: #dbeafe;
  color: #0c4a6e;
}

.status-badge--approuvee {
  background: #dcfce7;
  color: #14532d;
}

.status-badge--rejetee {
  background: #fee2e2;
  color: #7f1d1d;
}

.status-badge--scan-termine {
  background: #fce7f3;
  color: #831843;
}

.demande-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  padding: 2rem;
}

@media (max-width: 768px) {
  .demande-grid {
    grid-template-columns: 1fr;
  }
}

.demande-info h2 {
  color: #111827;
  margin-top: 0;
  border-bottom: 2px solid #2563eb;
  padding-bottom: 0.5rem;
}

.info-section {
  margin: 1.5rem 0;
}

.info-section h3 {
  color: #374151;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 1rem 0 0.5rem 0;
}

.info-section p {
  margin: 0.5rem 0;
  color: #6b7280;
  line-height: 1.6;
}

.info-section strong {
  color: #111827;
}

.demande-qr {
  display: flex;
  align-items: center;
  justify-content: center;
}

.actions {
  margin-top: 2rem;
  display: flex;
  gap: 1rem;
}

.btn {
  display: inline-block;
  padding: 0.75rem 1.5rem;
  border-radius: 0.5rem;
  text-decoration: none;
  font-weight: 600;
  text-align: center;
  transition: all 0.2s;
}

.btn-primary {
  background: #2563eb;
  color: white;
}

.btn-primary:hover {
  background: #1d4ed8;
}
</style>
