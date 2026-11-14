import { inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRouteSnapshot, Router } from '@angular/router';
import { EMPTY, Observable, of } from 'rxjs';
import { mergeMap } from 'rxjs/operators';

import { IDeporte } from '../deporte.model';
import { DeporteService } from '../service/deporte.service';

const deporteResolve = (route: ActivatedRouteSnapshot): Observable<null | IDeporte> => {
  const id = route.params.id;
  if (id) {
    return inject(DeporteService)
      .find(id)
      .pipe(
        mergeMap((deporte: HttpResponse<IDeporte>) => {
          if (deporte.body) {
            return of(deporte.body);
          }
          inject(Router).navigate(['404']);
          return EMPTY;
        }),
      );
  }
  return of(null);
};

export default deporteResolve;
