import { createRouter, createWebHistory } from "vue-router";
import HomeView from "../views/HomeView.vue";
import CreateDemandeView from "../views/CreateDemandeView.vue";
import SuiviDemandeView from "../views/SuiviDemandeView.vue";

const routes = [
  { path: "/", component: HomeView },
  { path: "/create", component: CreateDemandeView },
  { path: "/demandes/:id", component: SuiviDemandeView }
];

const router = createRouter({
  history: createWebHistory(),
  routes
});

export default router;