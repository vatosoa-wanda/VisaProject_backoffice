import { defineStore } from "pinia";
import api from "../services/api";

const normalizeListeDemandes = (data) => {
  if (Array.isArray(data)) {
    return data;
  }

  if (Array.isArray(data?.content)) {
    return data.content;
  }

  return [];
};

const getNumeroPasseport = (demande) => {
  return (
    demande?.numeroPasSeport ||
    demande?.numeroPasseport ||
    demande?.passeport?.numero ||
    ""
  );
};

export const useDemandeStore = defineStore("demande", {
  state: () => ({
    demandes: [],
    demande: null,
    loading: false,
    error: null
  }),

  actions: {
    async fetchDemandes() {
      this.loading = true;
      this.error = null;

      try {
        const res = await api.get("/demandes");
        console.log("Raw response du backend :", res.data);
        console.log("Type de réponse :", typeof res.data);
        this.demandes = normalizeListeDemandes(res.data);
        console.log("Demandes reçues :", this.demandes);
        if (this.demandes.length > 0) {
          console.log("Structure première demande :", this.demandes[0]);
        }
        return this.demandes;
      } catch (error) {
        this.error = error;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async rechercherParNumero(numero) {
      const valeurRecherchee = String(numero ?? "").trim().toLowerCase();

      if (!valeurRecherchee) {
        return this.fetchDemandes();
      }

      const demandes = this.demandes.length > 0 ? this.demandes : await this.fetchDemandes();

      return demandes.filter((demande) => {
        const numeroDemande = String(demande?.id ?? demande?.referenceVisa ?? "")
          .toLowerCase();
        return numeroDemande.includes(valeurRecherchee);
      });
    },

    async rechercherParPasseport(numero) {
      const valeurRecherchee = String(numero ?? "").trim().toLowerCase();

      if (!valeurRecherchee) {
        return this.fetchDemandes();
      }

      const demandes = this.demandes.length > 0 ? this.demandes : await this.fetchDemandes();

      const resultats = demandes.filter((demande) =>
        String(getNumeroPasseport(demande)).toLowerCase().includes(valeurRecherchee)
      );
      
      console.log("Recherche passeport pour :", numero);
      demandes.forEach(d => {
        console.log("  - Passeport trouvé :", getNumeroPasseport(d));
      });
      console.log("Résultats trouvés :", resultats.length);
      
      return resultats;
    },

    async getDemandeById(id) {
      this.loading = true;
      this.error = null;

      try {
        const res = await api.get(`/demandes/${id}`);
        this.demande = res.data;
        return this.demande;
      } catch (error) {
        this.error = error;
        throw error;
      } finally {
        this.loading = false;
      }
    },

    async creerDemande(data) {
      this.loading = true;
      this.error = null;

      try {
        const res = await api.post("/demandes", data);
        return res.data;
      } catch (error) {
        this.error = error;
        throw error;
      } finally {
        this.loading = false;
      }
    }
  }
});