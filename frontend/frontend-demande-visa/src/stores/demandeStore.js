import { defineStore } from "pinia";
import api from "../services/api";

export const useDemandeStore = defineStore("demande", {
  state: () => ({
    demandes: []
  }),

  actions: {
    async fetchDemandes() {
      const res = await api.get("/demandes");
      this.demandes = res.data;
    }
  }
});