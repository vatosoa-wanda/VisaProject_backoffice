<template>
  <div class="demande-list">
    <p v-if="sortedDemandes.length === 0" class="demande-list__empty">
      Aucun résultat trouvé
    </p>

    <table v-else class="demande-list__table">
      <thead class="demande-list__thead">
        <tr>
          <th>Numéro</th>
          <th>Nom</th>
          <th>Type</th>
          <th>Numero Passeport</th>
          <th>Statut</th>
          <th>Date</th>
          <th>QR Code</th>
        </tr>
      </thead>
      <tbody class="demande-list__tbody">
        <tr v-for="demande in sortedDemandes" :key="demande.id" class="demande-list__row">
          <td>
            <router-link :to="`/demandes/${demande.id}`"> #{{ demande.id }} </router-link>
          </td>
          <td>{{ demande.nomDemandeur }} {{ demande.prenomDemandeur }}</td>
          <td>{{ demande.typeVisa }}</td>
          <td>{{ demande.numeroPasSeport }}</td>
          <td>
            <span :class="getStatutClass(demande.statutDemande)">
              {{ demande.statutDemande }}
            </span>
          </td>
          <td>{{ formatDate(demande.dateDemande) }}</td>
          <td>
            <router-link :to="`/demandes/${demande.id}/qrcode`" class="btn-qr-code">
              📱 Voir QR
            </router-link>
          </td>
        </tr>
      </tbody>
    </table>
  </div>
</template>

<script setup>
import { computed } from "vue";

const props = defineProps({
  demandes: {
    type: Array,
    default: () => [],
  },
});

const sortedDemandes = computed(() => {
  return [...props.demandes].sort((a, b) => {
    const dateA = new Date(a.dateDemande || 0);
    const dateB = new Date(b.dateDemande || 0);
    return dateB - dateA;
  });
});

const formatDate = (dateString) => {
  if (!dateString) return "";
  return new Date(dateString).toLocaleDateString("fr-FR");
};

const getStatutClass = (statut) => {
  const classMap = {
    CREE: "statut-badge statut-badge--cree",
    APPROUVEE: "statut-badge statut-badge--approuvee",
    REJETEE: "statut-badge statut-badge--rejetee",
    SCAN_TERMINE: "statut-badge statut-badge--scan-termine",
  };
  return classMap[statut] || "statut-badge";
};
</script>

<style scoped>
.demande-list {
  margin-top: 2rem;
}

.demande-list__empty {
  text-align: center;
  color: #6b7280;
  padding: 2rem 1rem;
  font-style: italic;
}

.demande-list__table {
  width: 100%;
  border-collapse: collapse;
  background: white;
  border-radius: 0.75rem;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.demande-list__thead {
  background: #f3f4f6;
  border-bottom: 1px solid #e5e7eb;
}

.demande-list__thead th {
  padding: 0.75rem 1rem;
  text-align: left;
  font-weight: 600;
  color: #111827;
  font-size: 0.875rem;
}

.demande-list__tbody tr:hover {
  background: #f9fafb;
}

.demande-list__row td {
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #e5e7eb;
  font-size: 0.875rem;
  color: #374151;
}

.demande-list__row:last-child td {
  border-bottom: none;
}

.demande-list__row a {
  color: #2563eb;
  text-decoration: none;
  font-weight: 600;
}

.demande-list__row a:hover {
  text-decoration: underline;
}

.statut-badge {
  display: inline-block;
  padding: 0.25rem 0.75rem;
  border-radius: 9999px;
  font-size: 0.75rem;
  font-weight: 600;
}

.statut-badge--cree {
  background: #dbeafe;
  color: #0c4a6e;
}

.statut-badge--approuvee {
  background: #dcfce7;
  color: #14532d;
}

.statut-badge--rejetee {
  background: #fee2e2;
  color: #7f1d1d;
}

.statut-badge--scan-termine {
  background: #fce7f3;
  color: #831843;
}

.btn-qr-code {
  display: inline-block;
  padding: 0.5rem 1rem;
  background: #8b5cf6;
  color: white;
  text-decoration: none;
  border-radius: 0.375rem;
  font-size: 0.75rem;
  font-weight: 600;
  transition: background 0.2s;
}

.btn-qr-code:hover {
  background: #7c3aed;
}
</style>
