import { DestroyRef, Injectable, InjectionToken, inject, isDevMode, signal } from '@angular/core';
import { Workbox } from 'workbox-window';

/** Lo mínimo que usa la app de workbox-window (se reemplaza en los tests). */
export type ServiceWorkerPwa = Pick<Workbox, 'addEventListener' | 'register' | 'messageSkipWaiting'>;

/**
 * Crea el registro del service worker generado por Workbox (sw.js).
 * Devuelve null en desarrollo (npm start) y en navegadores sin soporte.
 */
export const CREAR_SERVICE_WORKER = new InjectionToken<() => ServiceWorkerPwa | null>('CREAR_SERVICE_WORKER', {
  providedIn: 'root',
  factory: () => () => (!isDevMode() && typeof navigator !== 'undefined' && 'serviceWorker' in navigator ? new Workbox('/sw.js') : null),
});

/** Nombres de las caches de datos (runtimeCaching de workbox.config.cjs). */
export const CACHES_DE_DATOS = ['api-cuenta', 'api-catalogo', 'api-reservas', 'clima-open-meteo'];

/**
 * Estado de la PWA: conexión, versiones nuevas del service worker y limpieza de la caché de datos.
 */
@Injectable({ providedIn: 'root' })
export class PwaService {
  /** true si el navegador tiene conexión. */
  readonly online = signal(typeof navigator === 'undefined' ? true : navigator.onLine);

  /** true cuando hay una versión nueva del service worker esperando para activarse. */
  readonly hayNuevaVersion = signal(false);

  private readonly destroyRef = inject(DestroyRef);
  private readonly serviceWorker = inject(CREAR_SERVICE_WORKER)();

  constructor() {
    const actualizarConexion = (): void => this.online.set(navigator.onLine);
    window.addEventListener('online', actualizarConexion);
    window.addEventListener('offline', actualizarConexion);
    this.destroyRef.onDestroy(() => {
      window.removeEventListener('online', actualizarConexion);
      window.removeEventListener('offline', actualizarConexion);
    });

    const sw = this.serviceWorker;
    if (sw) {
      // Hay una versión nueva instalada que espera: se ofrece "Actualizar"
      sw.addEventListener('waiting', () => this.hayNuevaVersion.set(true));
      // Se registra cuando terminó de cargar la página, para no competir con la carga inicial
      if (document.readyState === 'complete') {
        void sw.register();
      } else {
        window.addEventListener('load', () => void sw.register(), { once: true });
      }
    }
  }

  /** Activa la versión nueva del service worker y recarga la página cuando toma el control. */
  actualizar(): void {
    const sw = this.serviceWorker;
    if (!sw) {
      document.location.reload();
      return;
    }
    sw.addEventListener('controlling', () => document.location.reload());
    sw.messageSkipWaiting();
  }

  /**
   * Borra las respuestas de la API que guardó el service worker.
   * El service worker cachea por URL, no por usuario: si no se borran al cerrar sesión,
   * otra persona en el mismo dispositivo podría ver offline las reservas del usuario anterior.
   * La app en sí (precache de Workbox: JS, CSS, íconos) queda cacheada.
   */
  async limpiarCacheDeDatos(): Promise<void> {
    if (typeof caches === 'undefined') {
      return;
    }
    const nombres = await caches.keys();
    await Promise.all(nombres.filter(nombre => esCacheDeDatos(nombre)).map(nombre => caches.delete(nombre)));
  }
}

/** Las caches de datos son las de runtimeCaching; el precache de la app se llama "workbox-precache-...". */
export function esCacheDeDatos(nombre: string): boolean {
  return CACHES_DE_DATOS.includes(nombre);
}
