<template>
  <div class="home">
    <h1>Bienvenue sur le portail de demande de visa</h1>
    <p>Sélectionnez une action ci-dessous :</p>

    <SearchBar v-model="searchQuery" @search="handleSearch" />

    <div v-if="store.loading" class="loading">Chargement en cours...</div>

    <div v-if="store.error" class="error">
      Erreur lors de la recherche : {{ store.error.message }}
    </div>

    <DemandeList v-if="!store.loading" :demandes="resultats" />

    <div class="actions">
      <router-link to="/create" class="btn btn-primary">
        Créer une nouvelle demande
      </router-link>
      <router-link to="/create" class="btn btn-secondary">
        Créer un transfert
      </router-link>
    </div>
  </div>
</template>

<script setup>
import { ref } from "vue";
import SearchBar from "../components/SearchBar.vue";
import DemandeList from "../components/DemandeList.vue";
import { useDemandeStore } from "../stores/demandeStore";

const searchQuery = ref("");
const resultats = ref([]);
const store = useDemandeStore();

const handleSearch = async () => {
  try {
    const query = searchQuery.value.trim();
    if (!query) {
      resultats.value = await store.fetchDemandes();
    } else {
      // Chercher par numéro (ID, referenceVisa)
      let results = await store.rechercherParNumero(query);
      
      // Si pas de résultat, chercher par passeport
      if (results.length === 0) {
        results = await store.rechercherParPasseport(query);
      }
      
      resultats.value = results;
    }
  } catch (error) {
    console.error("Erreur lors de la recherche :", error);
  }
};
</script>

<style scoped>
.home {
  max-width: 900px;
  margin: 0 auto;
  padding: 2rem;
}

.loading {
  margin-top: 1rem;
  padding: 1rem;
  background: #e0e7ff;
  color: #3730a3;
  border-radius: 0.75rem;
  text-align: center;
}

.error {
  margin-top: 1rem;
  padding: 1rem;
  background: #fee2e2;
  color: #991b1b;
  border-radius: 0.75rem;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
  flex-wrap: wrap;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 0.75rem;
  text-decoration: none;
  color: white;
  font-weight: 600;
  cursor: pointer;
  border: none;
  transition: background 0.2s ease;
}

.btn-primary {
  background-color: #007bff;
}

.btn-primary:hover {
  background-color: #0056b3;
}

.btn-secondary {
  background-color: #6c757d;
}

.btn-secondary:hover {
  background-color: #5a6268;
}
</style>
