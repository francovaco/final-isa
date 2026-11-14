import { IDeporte, NewDeporte } from './deporte.model';

export const sampleWithRequiredData: IDeporte = {
  id: 29723,
  nombre: 'across',
};

export const sampleWithPartialData: IDeporte = {
  id: 6212,
  nombre: 'wisely habit',
  descripcion: 'utilized suspiciously',
};

export const sampleWithFullData: IDeporte = {
  id: 6489,
  nombre: 'under brr',
  descripcion: 'jaunty anaesthetise feline',
  jugadoresPorEquipo: 7,
};

export const sampleWithNewData: NewDeporte = {
  nombre: 'convoke solution',
  id: null,
};

Object.freeze(sampleWithNewData);
Object.freeze(sampleWithRequiredData);
Object.freeze(sampleWithPartialData);
Object.freeze(sampleWithFullData);
