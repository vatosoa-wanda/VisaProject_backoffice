<template>
  <div class="qr-code-display">
    <div class="qr-code-container">
      <qrcode-vue
        :value="qrUrl"
        :size="size"
        level="H"
        :margin="2"
        mode="image/png"
        ref="qrcodeRef"
      />
    </div>
    
    <button v-if="canDownload" @click="downloadQRCode" class="btn btn-download">
      📥 Télécharger le QR Code
    </button>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, nextTick } from "vue";
import QrcodeVue from "qrcode.vue";

const props = defineProps({
  demandeId: {
    type: [String, Number],
    required: true
  },
  size: {
    type: Number,
    default: 200
  }
});

const qrcodeRef = ref(null);
const canDownload = ref(false);

// Générer l'URL du QR code
const qrUrl = computed(() => {
  return `http://localhost:5173/demandes/${props.demandeId}`;
});

// Télécharger le QR code en PNG
const downloadQRCode = () => {
  if (qrcodeRef.value) {
    const canvas = qrcodeRef.value.$el.querySelector("canvas");
    if (canvas) {
      const link = document.createElement("a");
      link.href = canvas.toDataURL("image/png");
      link.download = `qr-code-demande-${props.demandeId}.png`;
      link.click();
    }
  }
};

// Vérifier si le canvas et toDataURL sont disponibles, sinon cacher le bouton
const checkDownloadAvailability = async () => {
  await nextTick();
  try {
    const el = qrcodeRef.value && qrcodeRef.value.$el;
    const canvas = el && el.querySelector && el.querySelector('canvas');
    if (!canvas) {
      canDownload.value = false;
      return;
    }
    // essayer toDataURL dans un try/catch : certaines politiques bloquent l'export
    canvas.toDataURL('image/png');
    canDownload.value = true;
  } catch (e) {
    canDownload.value = false;
  }
};

onMounted(() => {
  // vérifier immédiatement et après un court délai (HMR / rendu asynchrone)
  checkDownloadAvailability();
  setTimeout(checkDownloadAvailability, 150);
});
</script>

<style scoped>
.qr-code-display {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 1.5rem;
  padding: 2rem;
  background: #f9fafb;
  border-radius: 0.75rem;
  border: 1px solid #e5e7eb;
}

.qr-code-container {
  background: white;
  padding: 1rem;
  border-radius: 0.5rem;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.1);
}

.qr-code-container canvas {
  display: block;
}

.btn-download {
  padding: 0.75rem 1.5rem;
  background: #2563eb;
  color: white;
  border: none;
  border-radius: 0.5rem;
  font-size: 0.875rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.2s;
}

.btn-download:hover {
  background: #1d4ed8;
}

.btn-download:active {
  transform: scale(0.98);
}
</style>
