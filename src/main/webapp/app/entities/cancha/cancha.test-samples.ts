import { ICancha, NewCancha } from './cancha.model';

export const sampleWithRequiredData: ICancha = {
  id: 10630,
  nombre: 'dapper advertisement',
  superficie: 'CEMENTO',
  techada: true,
  precioPorHora: 26202.99,
  activa: true,
};

export const sampleWithPartialData: ICancha = {
  id: 25590,
  nombre: 'yowza fen provided',
  superficie: 'PARQUET',
  techada: false,
  precioPorHora: 24188.58,
  activa: false,
};

export const sampleWithFullData: ICancha = {
  id: 27337,
  nombre: 'calmly till',
  superficie: 'CEMENTO',
  techada: false,
  precioPorHora: 522.52,
  activa: false,
};

export const sampleWithNewData: NewCancha = {
  nombre: 'lest below',
  superficie: 'PARQUET',
  techada: false,
  precioPorHora: 27773.62,
  activa: true,
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
