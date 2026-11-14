import dayjs from 'dayjs/esm';

import { IReserva, NewReserva } from './reserva.model';

export const sampleWithRequiredData: IReserva = {
  id: 22571,
  inicio: dayjs('2026-11-14T07:59'),
  duracionMinutos: 77,
  estado: 'FINALIZADA',
};

export const sampleWithPartialData: IReserva = {
  id: 1905,
  inicio: dayjs('2026-11-13T21:04'),
  duracionMinutos: 60,
  estado: 'FINALIZADA',
  observaciones: 'aha granular',
};

export const sampleWithFullData: IReserva = {
  id: 8504,
  inicio: dayjs('2026-11-14T16:47'),
  duracionMinutos: 79,
  estado: 'CANCELADA',
  observaciones: 'and',
  creadaEn: dayjs('2026-11-14T13:14'),
};

export const sampleWithNewData: NewReserva = {
  inicio: dayjs('2026-11-14T13:30'),
  duracionMinutos: 83,
  estado: 'FINALIZADA',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
