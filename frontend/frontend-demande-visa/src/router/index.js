import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import CreateDemandeView from "../views/CreateDemandeView.vue";
import SuiviDemandeView from "../views/SuiviDemandeView.vue";
import QRCodeGeneratorView from "../views/QRCodeGeneratorView.vue";

const routes = [
  { path: "/", name: "home", component: HomeView, alias: "/home" },
  { path: "/create", name: "create-demande", component: CreateDemandeView },
  { path: "/demandes/:id", name: "suivi-demande", component: SuiviDemandeView },
  { path: "/demandes/:id/qrcode", name: "demande-qrcode", component: QRCodeGeneratorView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;