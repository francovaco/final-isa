import { Injectable } from '@angular/core';
import { FormControl, FormGroup, Validators } from '@angular/forms';

import dayjs from 'dayjs/esm';
import { DATE_TIME_FORMAT } from 'app/config/input.constants';
import { IReserva, NewReserva } from '../reserva.model';

/**
 * A partial Type with required key is used as form input.
 */
type PartialWithRequiredKeyOf<T extends { id: unknown }> = Partial<Omit<T, 'id'>> & { id: T['id'] };

/**
 * Type for createFormGroup and resetForm argument.
 * It accepts IReserva for edit and NewReservaFormGroupInput for create.
 */
type ReservaFormGroupInput = IReserva | PartialWithRequiredKeyOf<NewReserva>;

/**
 * Type that converts some properties for forms.
 */
type FormValueOf<T extends IReserva | NewReserva> = Omit<T, 'inicio' | 'creadaEn'> & {
  inicio?: string | null;
  creadaEn?: string | null;
};

type ReservaFormRawValue = FormValueOf<IReserva>;

type NewReservaFormRawValue = FormValueOf<NewReserva>;

type ReservaFormDefaults = Pick<NewReserva, 'id' | 'inicio' | 'creadaEn'>;

type ReservaFormGroupContent = {
  id: FormControl<ReservaFormRawValue['id'] | NewReserva['id']>;
  inicio: FormControl<ReservaFormRawValue['inicio']>;
  duracionMinutos: FormControl<ReservaFormRawValue['duracionMinutos']>;
  estado: FormControl<ReservaFormRawValue['estado']>;
  observaciones: FormControl<ReservaFormRawValue['observaciones']>;
  creadaEn: FormControl<ReservaFormRawValue['creadaEn']>;
  cancha: FormControl<ReservaFormRawValue['cancha']>;
  usuario: FormControl<ReservaFormRawValue['usuario']>;
};

export type ReservaFormGroup = FormGroup<ReservaFormGroupContent>;

@Injectable({ providedIn: 'root' })
export class ReservaFormService {
  createReservaFormGroup(reserva: ReservaFormGroupInput = { id: null }): ReservaFormGroup {
    const reservaRawValue = this.convertReservaToReservaRawValue({
      ...this.getFormDefaults(),
      ...reserva,
    });
    return new FormGroup<ReservaFormGroupContent>({
      id: new FormControl(
        { value: reservaRawValue.id, disabled: true },
        {
          nonNullable: true,
          validators: [Validators.required],
        },
      ),
      inicio: new FormControl(reservaRawValue.inicio, {
        validators: [Validators.required],
      }),
      duracionMinutos: new FormControl(reservaRawValue.duracionMinutos, {
        validators: [Validators.required, Validators.min(60), Validators.max(120)],
      }),
      estado: new FormControl(reservaRawValue.estado, {
        validators: [Validators.required],
      }),
      observaciones: new FormControl(reservaRawValue.observaciones, {
        validators: [Validators.maxLength(255)],
      }),
      creadaEn: new FormControl(reservaRawValue.creadaEn),
      cancha: new FormControl(reservaRawValue.cancha, {
        validators: [Validators.required],
      }),
      usuario: new FormControl(reservaRawValue.usuario),
    });
  }

  getReserva(form: ReservaFormGroup): IReserva | NewReserva {
    return this.convertReservaRawValueToReserva(form.getRawValue() as ReservaFormRawValue | NewReservaFormRawValue);
  }

  resetForm(form: ReservaFormGroup, reserva: ReservaFormGroupInput): void {
    const reservaRawValue = this.convertReservaToReservaRawValue({ ...this.getFormDefaults(), ...reserva });
    form.reset(
      {
        ...reservaRawValue,
        id: { value: reservaRawValue.id, disabled: true },
      } as any /* cast to workaround https://github.com/angular/angular/issues/46458 */,
    );
  }

  private getFormDefaults(): ReservaFormDefaults {
    const currentTime = dayjs();

    return {
      id: null,
      inicio: currentTime,
      creadaEn: currentTime,
    };
  }

  private convertReservaRawValueToReserva(rawReserva: ReservaFormRawValue | NewReservaFormRawValue): IReserva | NewReserva {
    return {
      ...rawReserva,
      inicio: dayjs(rawReserva.inicio, DATE_TIME_FORMAT),
      creadaEn: dayjs(rawReserva.creadaEn, DATE_TIME_FORMAT),
    };
  }

  private convertReservaToReservaRawValue(
    reserva: IReserva | (Partial<NewReserva> & ReservaFormDefaults),
  ): ReservaFormRawValue | PartialWithRequiredKeyOf<NewReservaFormRawValue> {
    return {
      ...reserva,
      inicio: reserva.inicio ? reserva.inicio.format(DATE_TIME_FORMAT) : undefined,
      creadaEn: reserva.creadaEn ? reserva.creadaEn.format(DATE_TIME_FORMAT) : undefined,
    };
  }
}
