import { Routes } from '@angular/router';

const routes: Routes = [
  {
    path: 'authority',
    data: { pageTitle: 'Authorities' },
    loadChildren: () => import('./admin/authority/authority.routes'),
  },
  {
    path: 'deporte',
    data: { pageTitle: 'Deportes' },
    loadChildren: () => import('./deporte/deporte.routes'),
  },
  {
    path: 'cancha',
    data: { pageTitle: 'Canchas' },
    loadChildren: () => import('./cancha/cancha.routes'),
  },
  {
    path: 'reserva',
    data: { pageTitle: 'Reservas' },
    loadChildren: () => import('./reserva/reserva.routes'),
  },
  /* jhipster-needle-add-entity-route - JHipster will add entity modules routes here */
];

export default routes;
