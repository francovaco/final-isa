import { AbstractControl, ValidationErrors, ValidatorFn } from '@angular/forms';
import dayjs from 'dayjs/esm';

import { EstadoReserva } from 'app/entities/enumerations/estado-reserva.model';
import { IReserva } from './reserva.model';

/**
 * Reglas de negocio de reservas del lado del cliente. Son una ayuda para el usuario
 * (avisar antes de enviar); la validación definitiva la hace el backend.
 */

export const DURACIONES_PERMITIDAS = [60, 90, 120];
export const HORAS_MINIMAS_CANCELACION = 2;

/** La duración tiene que ser 60, 90 o 120 minutos. */
export function duracionPermitidaValidator(control: AbstractControl): ValidationErrors | null {
  const valor = control.value;
  if (valor === null || valor === undefined || valor === '') {
    return null; // de esto se encarga Validators.required
  }
  return DURACIONES_PERMITIDAS.includes(Number(valor)) ? null : { duracionPermitida: true };
}

/**
 * El inicio tiene que ser posterior a "ahora".
 * Recibe una función que devuelve la hora actual, para poder fijarla en los tests.
 */
export function fechaFuturaValidator(ahora: () => dayjs.Dayjs = () => dayjs()): ValidatorFn {
  return (control: AbstractControl): ValidationErrors | null => {
    if (!control.value) {
      return null;
    }
    const fecha = dayjs(control.value);
    return fecha.isValid() && fecha.isAfter(ahora()) ? null : { fechaFutura: true };
  };
}

/**
 * Indica si el usuario puede cancelar la reserva:
 * tiene que estar CONFIRMADA y, salvo que sea admin, faltar al menos 2 horas.
 */
export function puedeCancelar(reserva: IReserva, esAdmin: boolean, ahora: dayjs.Dayjs = dayjs()): boolean {
  if (reserva.estado !== EstadoReserva.CONFIRMADA || !reserva.inicio) {
    return false;
  }
  if (esAdmin) {
    return true;
  }
  return !ahora.add(HORAS_MINIMAS_CANCELACION, 'hour').isAfter(reserva.inicio);
}
