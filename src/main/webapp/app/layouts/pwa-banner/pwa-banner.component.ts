import { Component, inject } from '@angular/core';

import { PwaService } from 'app/core/pwa/pwa.service';

/** Avisos de la PWA: sin conexión y versión nueva disponible. */
@Component({
  selector: 'jhi-pwa-banner',
  template: `
    @if (!pwa.online()) {
      <div class="alert alert-warning text-center rounded-0 mb-0 py-2" role="status" data-cy="banner-offline">
        Sin conexión: estás viendo los últimos datos guardados. No se pueden crear ni cancelar reservas hasta que vuelva la conexión.
      </div>
    }
    @if (pwa.hayNuevaVersion()) {
      <div class="alert alert-info text-center rounded-0 mb-0 py-2" role="status" data-cy="banner-actualizacion">
        Hay una nueva versión de la app.
        <button type="button" class="btn btn-sm btn-primary ms-2" (click)="pwa.actualizar()">Actualizar</button>
      </div>
    }
  `,
})
export default class PwaBannerComponent {
  protected readonly pwa = inject(PwaService);
}
