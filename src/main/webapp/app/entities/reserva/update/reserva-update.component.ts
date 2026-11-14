import { Component, DestroyRef, OnInit, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { HttpResponse } from '@angular/common/http';
import { ActivatedRoute } from '@angular/router';
import { Observable, of } from 'rxjs';
import { debounceTime, distinctUntilChanged, finalize, map, startWith, switchMap } from 'rxjs/operators';
import dayjs from 'dayjs/esm';

import SharedModule from 'app/shared/shared.module';
import { FormsModule, ReactiveFormsModule } from '@angular/forms';

import { AccountService } from 'app/core/auth/account.service';
import { Authority } from 'app/config/authority.constants';
import { ClimaService, PronosticoDia } from 'app/shared/clima/clima.service';
import { ICancha } from 'app/entities/cancha/cancha.model';
import { CanchaService } from 'app/entities/cancha/service/cancha.service';
import { IUser } from 'app/entities/user/user.model';
import { UserService } from 'app/entities/user/service/user.service';
import { EstadoReserva } from 'app/entities/enumerations/estado-reserva.model';
import { ReservaService } from '../service/reserva.service';
import { IReserva } from '../reserva.model';
import { DURACIONES_PERMITIDAS } from '../reserva-reglas';
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
  duracionesPermitidas = DURACIONES_PERMITIDAS;

  canchasSharedCollection: ICancha[] = [];
  usersSharedCollection: IUser[] = [];

  /** Pronóstico del día elegido (null si no hay datos). */
  pronostico = signal<PronosticoDia | null>(null);

  protected reservaService = inject(ReservaService);
  protected reservaFormService = inject(ReservaFormService);
  protected canchaService = inject(CanchaService);
  protected userService = inject(UserService);
  protected accountService = inject(AccountService);
  protected climaService = inject(ClimaService);
  protected activatedRoute = inject(ActivatedRoute);
  protected destroyRef = inject(DestroyRef);

  // eslint-disable-next-line @typescript-eslint/member-ordering
  editForm: ReservaFormGroup = this.reservaFormService.createReservaFormGroup();

  compareCancha = (o1: ICancha | null, o2: ICancha | null): boolean => this.canchaService.compareCancha(o1, o2);

  compareUser = (o1: IUser | null, o2: IUser | null): boolean => this.userService.compareUser(o1, o2);

  get esAdmin(): boolean {
    return this.accountService.hasAnyAuthority(Authority.ADMIN);
  }

  /** Al crear, estado/usuario/fecha de creación los asigna el servidor: no se muestran. */
  get esNueva(): boolean {
    return this.editForm.controls.id.value === null;
  }

  ngOnInit(): void {
    this.activatedRoute.data.subscribe(({ reserva }) => {
      this.reserva = reserva;
      if (reserva) {
        this.updateForm(reserva);
      }

      this.loadRelationshipsOptions();
    });
    this.escucharCambiosDeFecha();
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
      // Solo se ofrecen canchas activas (el backend igual rechaza una inactiva)
      .pipe(map((res: HttpResponse<ICancha[]>) => (res.body ?? []).filter(cancha => cancha.activa)))
      .pipe(map((canchas: ICancha[]) => this.canchaService.addCanchaToCollectionIfMissing<ICancha>(canchas, this.reserva?.cancha)))
      .subscribe((canchas: ICancha[]) => (this.canchasSharedCollection = canchas));

    // La lista de usuarios solo la necesita el admin (al editar una reserva)
    if (this.esAdmin) {
      this.userService
        .query()
        .pipe(map((res: HttpResponse<IUser[]>) => res.body ?? []))
        .pipe(map((users: IUser[]) => this.userService.addUserToCollectionIfMissing<IUser>(users, this.reserva?.usuario)))
        .subscribe((users: IUser[]) => (this.usersSharedCollection = users));
    }
  }

  /** Cada vez que cambia el día elegido, consulta el pronóstico de ese día. */
  protected escucharCambiosDeFecha(): void {
    const inicio = this.editForm.controls.inicio;
    inicio.valueChanges
      .pipe(
        startWith(inicio.value),
        map(valor => (valor && dayjs(valor).isValid() ? dayjs(valor).format('YYYY-MM-DD') : null)),
        distinctUntilChanged(),
        debounceTime(300),
        switchMap(fecha => (fecha ? this.climaService.pronosticoDelDia(fecha) : of(null))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe(pronostico => this.pronostico.set(pronostico));
  }
}
