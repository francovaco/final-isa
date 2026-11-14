import { IDeporte } from 'app/entities/deporte/deporte.model';
import { Superficie } from 'app/entities/enumerations/superficie.model';

export interface ICancha {
  id: number;
  nombre?: string | null;
  superficie?: keyof typeof Superficie | null;
  techada?: boolean | null;
  precioPorHora?: number | null;
  activa?: boolean | null;
  deporte?: Pick<IDeporte, 'id' | 'nombre'> | null;
}

export type NewCancha = Omit<ICancha, 'id'> & { id: null };
