export interface IDeporte {
  id: number;
  nombre?: string | null;
  descripcion?: string | null;
  jugadoresPorEquipo?: number | null;
}

export type NewDeporte = Omit<IDeporte, 'id'> & { id: null };
