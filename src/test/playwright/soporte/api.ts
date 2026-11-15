import { APIRequestContext, expect } from '@playwright/test';

/**
 * Ayudas para preparar y limpiar datos de prueba usando la API REST
 * (más rápido y estable que hacerlo desde la interfaz).
 */

export const USUARIOS = {
  admin: { usuario: 'admin', clave: 'admin' },
  socio: { usuario: 'user', clave: 'user' },
};

export const SESION = {
  admin: 'target/playwright/.auth/admin.json',
  socio: 'target/playwright/.auth/socio.json',
};

/** Sufijo único para que cada corrida use nombres distintos (los nombres son únicos en la base). */
export const unico = (): string => `${Date.now().toString(36)}${Math.floor(Math.random() * 1000)}`;

export async function token(request: APIRequestContext, quien: keyof typeof USUARIOS): Promise<string> {
  const { usuario, clave } = USUARIOS[quien];
  const respuesta = await request.post('/api/authenticate', { data: { username: usuario, password: clave } });
  expect(respuesta.ok()).toBeTruthy();
  return (await respuesta.json()).id_token;
}

export class ApiAdmin {
  private constructor(
    private readonly request: APIRequestContext,
    private readonly jwt: string,
  ) {}

  static async crear(request: APIRequestContext): Promise<ApiAdmin> {
    return new ApiAdmin(request, await token(request, 'admin'));
  }

  private get headers(): Record<string, string> {
    return { Authorization: `Bearer ${this.jwt}` };
  }

  async crearDeporte(nombre: string): Promise<number> {
    const r = await this.request.post('/api/deportes', { headers: this.headers, data: { nombre, jugadoresPorEquipo: 2 } });
    expect(r.status()).toBe(201);
    return (await r.json()).id;
  }

  async crearCancha(nombre: string, deporteId: number): Promise<number> {
    const r = await this.request.post('/api/canchas', {
      headers: this.headers,
      data: { nombre, superficie: 'CESPED_SINTETICO', techada: true, precioPorHora: 18000, activa: true, deporte: { id: deporteId } },
    });
    expect(r.status()).toBe(201);
    return (await r.json()).id;
  }

  async buscarIdPorNombre(entidad: 'deportes' | 'canchas', nombre: string): Promise<number | undefined> {
    const r = await this.request.get(`/api/${entidad}?size=1000`, { headers: this.headers });
    const lista: { id: number; nombre: string }[] = await r.json();
    return lista.find(x => x.nombre === nombre)?.id;
  }

  /** Cancela las reservas confirmadas del socio, para que no choque con el límite de 3 activas. */
  async cancelarReservasActivasDelSocio(): Promise<void> {
    const socio = await (await this.request.get('/api/admin/users/user', { headers: this.headers })).json();
    const r = await this.request.get(`/api/reservas?usuarioId.equals=${socio.id}&estado.equals=CONFIRMADA&size=100`, {
      headers: this.headers,
    });
    for (const reserva of await r.json()) {
      await this.request.post(`/api/reservas/${reserva.id}/cancelar`, { headers: this.headers });
    }
  }

  /** Borra una cancha con sus reservas, y el deporte si se indica. */
  async borrarCancha(canchaId: number | undefined, deporteId?: number): Promise<void> {
    if (canchaId) {
      const r = await this.request.get(`/api/reservas?canchaId.equals=${canchaId}&size=100`, { headers: this.headers });
      for (const reserva of await r.json()) {
        await this.request.delete(`/api/reservas/${reserva.id}`, { headers: this.headers });
      }
      await this.request.delete(`/api/canchas/${canchaId}`, { headers: this.headers });
    }
    if (deporteId) {
      await this.request.delete(`/api/deportes/${deporteId}`, { headers: this.headers });
    }
  }
}

/** Fecha "YYYY-MM-DD" a N días de hoy, en hora del club (para inputs datetime-local). */
export function fechaClub(diasDesdeHoy: number): string {
  const fecha = new Date(Date.now() + diasDesdeHoy * 24 * 60 * 60 * 1000);
  return new Intl.DateTimeFormat('en-CA', { timeZone: 'America/Argentina/Mendoza' }).format(fecha);
}
