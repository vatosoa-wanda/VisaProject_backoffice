<template>
  <div class="historique-statut-list">
    <h3>Historique des statuts</h3>

    <div v-if="loading" class="loading">
      Chargement de l'historique...
    </div>

    <div v-else-if="error" class="error">
      Erreur : {{ error }}
    </div>

    <div v-else-if="historique && historique.length > 0" class="table-wrapper">
      <table class="historique-table">
        <thead>
          <tr>
            <th>Date</th>
            <th>Statut</th>
            <th>Commentaire</th>
          </tr>
        </thead>
        <tbody>
          <tr v-for="(entry, index) in historiqueAffiche" :key="entry.id" :class="{ 'even': index % 2 === 0 }">
            <td class="date-cell">{{ formatDate(entry.dateChangement) }}</td>
            <td class="statut-cell">
              <span v-if="index > 0" class="transition">
                {{ historique[index - 1].statutDemande }} → {{ entry.statutDemande }}
              </span>
              <span v-else class="transition initial">
                → {{ entry.statutDemande }}
              </span>
            </td>
            <td class="commentaire-cell">{{ entry.commentaire || 'N/A' }}</td>
          </tr>
        </tbody>
      </table>
    </div>

    <div v-else class="no-data">
      Aucun historique disponible.
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from "vue";
import axios from "axios";

const props = defineProps({
  demandeId: {
    type: [String, Number],
    required: true
  }
});

const historique = ref([]);
const loading = ref(false);
const error = ref(null);

onMounted(async () => {
  await fetchHistorique();
});

const fetchHistorique = async () => {
  loading.value = true;
  error.value = null;
  try {
    const response = await axios.get(`/api/demandes/${props.demandeId}/historique`);
    historique.value = response.data || [];
  } catch (err) {
    error.value = err.message || "Erreur lors du chargement de l'historique";
    console.error("Erreur historique:", err);
  } finally {
    loading.value = false;
  }
};

const historiqueAffiche = computed(() => {
  return historique.value.slice().reverse();
});

const formatDate = (dateString) => {
  if (!dateString) return "";
  const date = new Date(dateString);
  return date.toLocaleDateString("fr-FR", {
    year: "numeric",
    month: "long",
    day: "numeric",
    hour: "2-digit",
    minute: "2-digit"
  });
};
</script>

<style scoped>
.historique-statut-list {
  margin-top: 2rem;
  padding-top: 1.5rem;
  border-top: 2px solid #e5e7eb;
}

.historique-statut-list h3 {
  color: #374151;
  font-size: 0.875rem;
  text-transform: uppercase;
  letter-spacing: 0.05em;
  margin: 0 0 1rem 0;
}

.loading,
.error,
.no-data {
  padding: 1rem;
  text-align: center;
  color: #6b7280;
  font-size: 0.875rem;
}

.error {
  background: #fee2e2;
  color: #7f1d1d;
  border-radius: 0.5rem;
  padding: 1rem;
}

.table-wrapper {
  overflow-x: auto;
}

.historique-table {
  width: 100%;
  border-collapse: collapse;
  font-size: 0.875rem;
}

.historique-table thead {
  background: #f9fafb;
  border-bottom: 2px solid #e5e7eb;
}

.historique-table th {
  padding: 0.75rem;
  text-align: left;
  font-weight: 600;
  color: #374151;
}

.historique-table td {
  padding: 0.75rem;
  border-bottom: 1px solid #e5e7eb;
  color: #6b7280;
}

.historique-table tbody tr {
  transition: background-color 0.2s;
}

.historique-table tbody tr:hover {
  background-color: #f3f4f6;
}

.historique-table tbody tr.even {
  background-color: #fafbfc;
}

.date-cell {
  font-weight: 500;
  color: #111827;
  min-width: 180px;
}

.statut-cell {
  font-weight: 600;
  color: #2563eb;
  min-width: 150px;
}

.transition {
  display: inline-block;
  padding: 0.25rem 0.5rem;
  background: #dbeafe;
  border-radius: 0.25rem;
  white-space: nowrap;
}

.transition.initial {
  background: #dcfce7;
}

.commentaire-cell {
  color: #6b7280;
  max-width: 300px;
  word-wrap: break-word;
}
</style>
