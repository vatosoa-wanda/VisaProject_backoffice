<template>
  <div class="home">
    <h1>Bienvenue sur le portail de demande de visa</h1>
    <p>Sélectionnez une action ci-dessous :</p>
    <div class="actions">
      <router-link to="/create" class="btn btn-primary">
        Créer une nouvelle demande
      </router-link>
      <router-link to="/suivi" class="btn btn-secondary">
        Suivre une demande
      </router-link>
    </div>
  </div>
</template>

<script>
export default {
  name: 'HomeView'
}
</script>

<style scoped>
.home {
  max-width: 800px;
  margin: 0 auto;
  padding: 2rem;
}

.actions {
  display: flex;
  gap: 1rem;
  margin-top: 2rem;
}

.btn {
  padding: 0.75rem 1.5rem;
  border-radius: 0.25rem;
  text-decoration: none;
  color: white;
}

.btn-primary {
  background-color: #007bff;
}

.btn-secondary {
  background-color: #6c757d;
}
</style>
