import dayjs from 'dayjs/esm';
import { ICancha } from 'app/entities/cancha/cancha.model';
import { IUser } from 'app/entities/user/user.model';
import { EstadoReserva } from 'app/entities/enumerations/estado-reserva.model';

export interface IReserva {
  id: number;
  inicio?: dayjs.Dayjs | null;
  duracionMinutos?: number | null;
  estado?: keyof typeof EstadoReserva | null;
  observaciones?: string | null;
  creadaEn?: dayjs.Dayjs | null;
  cancha?: Pick<ICancha, 'id' | 'nombre'> | null;
  usuario?: Pick<IUser, 'id' | 'login'> | null;
}

export type NewReserva = Omit<IReserva, 'id'> & { id: null };
