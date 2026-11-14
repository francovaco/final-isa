import { FormControl } from '@angular/forms';
import dayjs from 'dayjs/esm';

import { EstadoReserva } from 'app/entities/enumerations/estado-reserva.model';
import { IReserva } from './reserva.model';
import { duracionPermitidaValidator, fechaFuturaValidator, puedeCancelar } from './reserva-reglas';

describe('Reglas de reserva (cliente)', () => {
  const AHORA = dayjs('2026-11-16T09:00');

  describe('duracionPermitidaValidator', () => {
    it.each([60, 90, 120])('acepta %i minutos', minutos => {
      expect(duracionPermitidaValidator(new FormControl(minutos))).toBeNull();
    });

    it.each([30, 75, 150])('rechaza %i minutos', minutos => {
      expect(duracionPermitidaValidator(new FormControl(minutos))).toEqual({ duracionPermitida: true });
    });

    it('no marca error si está vacío (eso lo valida required)', () => {
      expect(duracionPermitidaValidator(new FormControl(null))).toBeNull();
    });
  });

  describe('fechaFuturaValidator', () => {
    const validador = fechaFuturaValidator(() => AHORA);

    it('acepta una fecha futura', () => {
      expect(validador(new FormControl('2026-11-17T10:00'))).toBeNull();
    });

    it('rechaza una fecha pasada', () => {
      expect(validador(new FormControl('2026-11-15T10:00'))).toEqual({ fechaFutura: true });
    });

    it('rechaza exactamente "ahora"', () => {
      expect(validador(new FormControl('2026-11-16T09:00'))).toEqual({ fechaFutura: true });
    });

    it('no marca error si está vacío', () => {
      expect(validador(new FormControl(null))).toBeNull();
    });
  });

  describe('puedeCancelar', () => {
    const reserva = (inicio: string, estado: keyof typeof EstadoReserva = 'CONFIRMADA'): IReserva => ({
      id: 1,
      inicio: dayjs(inicio),
      estado,
    });

    it('el socio puede cancelar con más de 2 horas de anticipación', () => {
      expect(puedeCancelar(reserva('2026-11-16T12:00'), false, AHORA)).toBe(true);
    });

    it('el socio puede cancelar con exactamente 2 horas de anticipación', () => {
      expect(puedeCancelar(reserva('2026-11-16T11:00'), false, AHORA)).toBe(true);
    });

    it('el socio no puede cancelar con menos de 2 horas', () => {
      expect(puedeCancelar(reserva('2026-11-16T10:30'), false, AHORA)).toBe(false);
    });

    it('el admin puede cancelar aunque falten menos de 2 horas', () => {
      expect(puedeCancelar(reserva('2026-11-16T10:30'), true, AHORA)).toBe(true);
    });

    it('nadie puede cancelar una reserva ya cancelada o finalizada', () => {
      expect(puedeCancelar(reserva('2026-11-20T10:00', 'CANCELADA'), true, AHORA)).toBe(false);
      expect(puedeCancelar(reserva('2026-11-20T10:00', 'FINALIZADA'), true, AHORA)).toBe(false);
    });
  });
});
