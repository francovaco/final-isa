import { Component, OnInit, inject } from '@angular/core';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable } from 'rxjs';
import { finalize, map } from 'rxjs/operators';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { ICancha } from 'app/entities/cancha/cancha.model';
import { CanchaService } from 'app/entities/cancha/service/cancha.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { EstadoReserva } from 'app/entities/enumerations/estado-reserva.model';
import { ReservaService } from '../service/reserva.service';
import { IReserva } from '../reserva.model';
import { ReservaFormGroup, ReservaFormService } from './reserva-form.service';

@Component({
  selector: 'jhi-reserva-update',
  templateUrl: './reserva-update.component.html',
  imports: [SharedModule, FormsModule, ReactiveFormsModule],
})
export class ReservaUpdateComponent implements OnInit {
  isSaving = false;
  reserva: IReserva | null = null;
  estadoReservaValues = Object.keys(EstadoReserva);

  canchasSharedCollection: ICancha[] = [];
  usersSharedCollection: IUser[] = [];

  protected reservaService = inject(ReservaService);
  protected reservaFormService = inject(ReservaFormService);
  protected canchaService = inject(CanchaService);
  protected userService = inject(UserService);
  protected activatedRoute = inject(ActivatedRoute);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReservaFormGroup = this.reservaFormService.createReservaFormGroup();

  compareCancha = (o1: ICancha | null, o2: ICancha | null): boolean => this.canchaService.compareCancha(o1, o2);

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ reserva }) => {
      this.reserva = reserva;
      if (reserva) {
        this.updateForm(reserva);
      }

      this.loadRelationshipsOptions();
    });
  }

  previousState(): void {
    window.history.back();
  }

  save(): void {
    this.isSaving = true;
    const reserva = this.reservaFormService.getReserva(this.editForm);
    if (reserva.id !== null) {
      this.subscribeToSaveResponse(this.reservaService.update(reserva));
    } else {
      this.subscribeToSaveResponse(this.reservaService.create(reserva));
    }
  }

  protected subscribeToSaveResponse(result: Observable<HttpResponse<IReserva>>): void {
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

  protected updateForm(reserva: IReserva): void {
    this.reserva = reserva;
    this.reservaFormService.resetForm(this.editForm, reserva);

    this.canchasSharedCollection = this.canchaService.addCanchaToCollectionIfMissing<ICancha>(this.canchasSharedCollection, reserva.cancha);
    this.usersSharedCollection = this.userService.addUserToCollectionIfMissing<IUser>(this.usersSharedCollection, reserva.usuario);
  }

  protected loadRelationshipsOptions(): void {
    this.canchaService
      .query()
      .pipe(map((res: HttpResponse<ICancha[]>) => res.body ?? []))
      .pipe(map((canchas: ICancha[]) => this.canchaService.addCanchaToCollectionIfMissing<ICancha>(canchas, this.reserva?.cancha)))
      .subscribe((canchas: ICancha[]) => (this.canchasSharedCollection = canchas));

    this.userService
      .query()
      .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
      .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.reserva?.usuario)))
      .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
  }
}
