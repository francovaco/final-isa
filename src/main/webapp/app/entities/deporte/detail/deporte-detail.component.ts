import { Component, input } from '@angular/core';
import { RouterModule } from '@angular/router';

import SharedModule from 'app/shared/shared.module';
import { IDeporte } from '../deporte.model';

@Component({
  selector: 'jhi-deporte-detail',
  templateUrl: './deporte-detail.component.html',
  imports: [SharedModule, RouterModule],
})
export class DeporteDetailComponent {
  deporte = input<IDeporte | null>(null);

  previousState(): void {
    window.history.back();
  }
}
