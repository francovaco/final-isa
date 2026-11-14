import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDeporte } from '../deporte.model';
import { DeporteService } from '../service/deporte.service';
import { DeporteFormGroup, DeporteFormService } from './deporte-form.service';

@Component({
  selector: 'jhi-deporte-update',
  templateUrl: './deporte-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class DeporteUpdateComponent implements OnInit {
  isSaving = false;
  deporte: IDeporte | null = null;

  protected deporteService = inject(DeporteService);
  protected deporteFormService = inject(DeporteFormService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: DeporteFormGroup = this.deporteFormService.createDeporteFormGroup();

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ deporte }) => {
      this.deporte = deporte;
      if (deporte) {
        this.updateForm(deporte);
      }
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const deporte = this.deporteFormService.getDeporte(this.editForm);
    if (deporte.id !== null) {
      this.subscribeToSaveResponse(this.deporteService.update(deporte));
    } else {
      this.subscribeToSaveResponse(this.deporteService.create(deporte));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IDeporte>>): void {
    result.pipe(finalize(() => this.onSaveFinalize())).subscribe({
      next: () => this.onSaveSuccess(),
      error: () => this.onSaveError(),
    });
  }

  protected onSaveSuccess(): void {
    this.previousState();
  }

  protected onSaveError(): void {
    // Api for inheritance.
  }

  protected onSaveFinalize(): void {
    this.isSaving = false;
  }

  protected updateForm(deporte: IDeporte): void {
    this.deporte = deporte;
    this.deporteFormService.resetForm(this.editForm, deporte);
  }
}
