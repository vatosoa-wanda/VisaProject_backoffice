<template>
  <form class="search-bar" @submit.prevent="handleSearch">
    <label class="search-bar__label" for="search-query">Recherche</label>
    <input
      id="search-query"
      class="search-bar__input"
      type="text"
      :value="modelValue"
      :placeholder="placeholder"
      @input="handleInput"
    />
    <button class="search-bar__button" type="submit">Rechercher</button>
  </form>
</template>

<script setup>
defineProps({
  modelValue: {
    type: String,
    default: ""
  },
  placeholder: {
    type: String,
    default: "Numéro de demande ou passeport"
  }
});

const emit = defineEmits(["update:modelValue", "search"]);

const handleInput = (event) => {
  emit("update:modelValue", event.target.value);
};

const handleSearch = () => {
  emit("search");
};
</script>

<style scoped>
.search-bar {
  display: flex;
  gap: 0.75rem;
  align-items: center;
  flex-wrap: wrap;
}

.search-bar__label {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

.search-bar__input {
  flex: 1 1 280px;
  min-width: 220px;
  padding: 0.85rem 1rem;
  border: 1px solid #d1d5db;
  border-radius: 0.75rem;
  font-size: 1rem;
  outline: none;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.search-bar__input:focus {
  border-color: #2563eb;
  box-shadow: 0 0 0 3px rgba(37, 99, 235, 0.12);
}

.search-bar__button {
  padding: 0.85rem 1.25rem;
  border: 0;
  border-radius: 0.75rem;
  background: #111827;
  color: #ffffff;
  font-weight: 600;
  cursor: pointer;
  transition: transform 0.15s ease, background 0.2s ease;
}

.search-bar__button:hover {
  background: #1f2937;
}

.search-bar__button:active {
  transform: translateY(1px);
}
</style>
