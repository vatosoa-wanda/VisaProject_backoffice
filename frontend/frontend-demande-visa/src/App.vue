<template>
  <div class="app-shell">
    <header class="app-shell__header">
      <router-link class="app-shell__brand" to="/">VisaProject</router-link>
      <nav class="app-shell__nav">
        <router-link to="/">Accueil</router-link>
        <router-link to="/create">Créer une demande</router-link>
      </nav>
    </header>

    <main class="app-shell__content">
      <router-view />
    </main>
  </div>
</template>

<style scoped>
.app-shell {
  min-height: 100vh;
  background: #f8fafc;
}

.app-shell__header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 1rem;
  padding: 1rem 1.5rem;
  background: #111827;
}

.app-shell__brand {
  color: #ffffff;
  text-decoration: none;
  font-weight: 700;
}

.app-shell__nav {
  display: flex;
  gap: 1rem;
  flex-wrap: wrap;
}

.app-shell__nav a {
  color: #e5e7eb;
  text-decoration: none;
}

.app-shell__content {
  padding: 2rem 1.5rem;
}
</style>
