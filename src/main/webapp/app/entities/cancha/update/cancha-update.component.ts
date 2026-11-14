import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { IDeporte } from 'app/entities/deporte/deporte.model';
import { DeporteService } from 'app/entities/deporte/service/deporte.service';
import { Superficie } from 'app/entities/enumerations/superficie.model';
import { CanchaService } from '../service/cancha.service';
import { ICancha } from '../cancha.model';
import { CanchaFormGroup, CanchaFormService } from './cancha-form.service';

@Component({
  selector: 'jhi-cancha-update',
  templateUrl: './cancha-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class CanchaUpdateComponent implements OnInit {
  isSaving = false;
  cancha: ICancha | null = null;
  superficieValues = Object.keys(Superficie);

  deportesSharedCollection: IDeporte[] = [];

  protected canchaService = inject(CanchaService);
  protected canchaFormService = inject(CanchaFormService);
  protected deporteService = inject(DeporteService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: CanchaFormGroup = this.canchaFormService.createCanchaFormGroup();

  compareDeporte = (o1: IDeporte | null, o2: IDeporte | null): boolean => this.deporteService.compareDeporte(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ cancha }) => {
      this.cancha = cancha;
      if (cancha) {
        this.updateForm(cancha);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const cancha = this.canchaFormService.getCancha(this.editForm);
    if (cancha.id !== null) {
      this.subscribeToSaveResponse(this.canchaService.update(cancha));
    } else {
      this.subscribeToSaveResponse(this.canchaService.create(cancha));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<ICancha>>): void {
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

  protected updateForm(cancha: ICancha): void {
    this.cancha = cancha;
    this.canchaFormService.resetForm(this.editForm, cancha);

    this.deportesSharedCollection = this.deporteService.addDeporteToCollectionIfMissing<IDeporte>(
      this.deportesSharedCollection,
      cancha.deporte,
    );
  }

  protected loadRelationshipsOptions(): void {
    this.deporteService
      .query()
      .pipe(map((res: HttpResponse<IDeporte[]>) => res.body ?? []))
      .pipe(map((deportes: IDeporte[]) => this.deporteService.addDeporteToCollectionIfMissing<IDeporte>(deportes, this.cancha?.deporte)))
      .subscribe((deportes: IDeporte[]) => (this.deportesSharedCollection = deportes));
  }
}
