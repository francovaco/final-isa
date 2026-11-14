import { Routes } from '@angular/router';

import { UserRouteAccessService } from 'app/core/auth/user-route-access.service';
import { Authority } from 'app/config/authority.constants';
import { ASC } from 'app/config/navigation.constants';
import DeporteResolve from './route/deporte-routing-resolve.service';

const deporteRoute: Routes = [
  {
    path: '',
    loadComponent: () => import('./list/deporte.component').then(m => m.DeporteComponent),
    data: {
      defaultSort: `id,${ASC}`,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/view',
    loadComponent: () => import('./detail/deporte-detail.component').then(m => m.DeporteDetailComponent),
    resolve: {
      deporte: DeporteResolve,
    },
    canActivate: [UserRouteAccessService],
  },
  {
    path: 'new',
    loadComponent: () => import('./update/deporte-update.component').then(m => m.DeporteUpdateComponent),
    resolve: {
      deporte: DeporteResolve,
    },
    data: { authorities: [Authority.ADMIN] },
    canActivate: [UserRouteAccessService],
  },
  {
    path: ':id/edit',
    loadComponent: () => import('./update/deporte-update.component').then(m => m.DeporteUpdateComponent),
    resolve: {
      deporte: DeporteResolve,
    },
    data: { authorities: [Authority.ADMIN] },
    canActivate: [UserRouteAccessService],
  },
];

export default deporteRoute;
