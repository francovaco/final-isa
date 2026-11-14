import { Component, inject } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { NgbActiveModal } from '@ng-bootstrap/ng-bootstrap';

import SharedModule from 'app/shared/shared.module';
import { ITEM_DELETED_EVENT } from 'app/config/navigation.constants';
import { IDeporte } from '../deporte.model';
import { DeporteService } from '../service/deporte.service';

@Component({
  templateUrl: './deporte-delete-dialog.component.html',
  imports: [SharedModule, FormsModule],
})
export class DeporteDeleteDialogComponent {
  deporte?: IDeporte;

  protected deporteService = inject(DeporteService);
  protected activeModal = inject(NgbActiveModal);

  cancel(): void {
    this.activeModal.dismiss();
  }

  confirmDelete(id: number): void {
    this.deporteService.delete(id).subscribe(() => {
      this.activeModal.close(ITEM_DELETED_EVENT);
    });
  }
}
