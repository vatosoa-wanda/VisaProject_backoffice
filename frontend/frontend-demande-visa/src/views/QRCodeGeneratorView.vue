<template>
  <div class="qr-generator">
    <router-link to="/" class="back-link">← Retour à la liste</router-link>

    <div class="card">
      <h2>Générateur de QR Code</h2>
      <p>QR code pour la demande #{{ id }}</p>

      <QRCodeDisplay :demandeId="id" :size="250" />

      <div class="qr-url">
        <input v-model="qrUrl" readonly class="qr-url__input" />
        <button @click="copyUrl" class="btn-copy">Copier l'URL</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from "vue";
import { useRoute, useRouter } from "vue-router";
import QRCodeDisplay from "../components/QRCodeDisplay.vue";

const route = useRoute();
const router = useRouter();
const id = route.params.id;

const qrUrl = computed(() => `http://localhost:5173/demandes/${id}`);

const copyUrl = async () => {
  try {
    await navigator.clipboard.writeText(qrUrl.value);
    // small feedback
    alert("URL copiée dans le presse-papiers");
  } catch (e) {
    console.error(e);
    alert("Impossible de copier l'URL");
  }
};
</script>

<style scoped>
.qr-generator {
  padding: 2rem;
  max-width: 900px;
  margin: 0 auto;
}

.back-link { color: #2563eb; text-decoration:none; font-weight:600 }

.card { background: white; padding: 2rem; border-radius: 0.5rem; box-shadow: 0 1px 3px rgba(0,0,0,0.08) }

.qr-url { margin-top: 1rem; display:flex; gap:0.5rem }
.qr-url__input { flex:1; padding:0.5rem; border-radius:0.375rem; border:1px solid #e5e7eb }
.btn-copy { padding:0.5rem 1rem; background:#2563eb; color:white; border-radius:0.375rem; border:none }
</style>