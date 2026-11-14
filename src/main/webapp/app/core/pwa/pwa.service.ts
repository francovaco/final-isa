import { DestroyRef, Injectable, inject, signal } from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { SwUpdate, VersionReadyEvent } from '@angular/service-worker';
import { filter } from 'rxjs/operators';

/**
 * Estado de la PWA: conexión, versiones nuevas del service worker y limpieza de la caché de datos.
 */
@Injectable({ providedIn: 'root' })
export class PwaService {
  /** true si el navegador tiene conexión. */
  readonly online = signal(typeof navigator === 'undefined' ? true : navigator.onLine);

  /** true cuando el service worker descargó una versión nueva de la app. */
  readonly hayNuevaVersion = signal(false);

  // Opcional: en los tests (y en navegadores sin soporte) no hay service worker
  private readonly swUpdate = inject(SwUpdate, { optional: true });
  private readonly destroyRef = inject(DestroyRef);

  constructor() {
    const actualizar = (): void => this.online.set(navigator.onLine);
    window.addEventListener('online', actualizar);
    window.addEventListener('offline', actualizar);
    this.destroyRef.onDestroy(() => {
      window.removeEventListener('online', actualizar);
      window.removeEventListener('offline', actualizar);
    });

    if (this.swUpdate?.isEnabled) {
      this.swUpdate.versionUpdates
        .pipe(
          filter((evento): evento is VersionReadyEvent => evento.type === 'VERSION_READY'),
          takeUntilDestroyed(this.destroyRef),
        )
        .subscribe(() => this.hayNuevaVersion.set(true));
    }
  }

  /** Activa la versión nueva y recarga la página. */
  async actualizar(): Promise<void> {
    if (this.swUpdate?.isEnabled) {
      await this.swUpdate.activateUpdate();
    }
    document.location.reload();
  }

  /**
   * Borra las respuestas de la API que guardó el service worker (dataGroups).
   * El service worker cachea por URL, no por usuario: si no se borran al cerrar sesión,
   * otra persona en el mismo dispositivo podría ver offline las reservas del usuario anterior.
   * La app en sí (JS, CSS, íconos) queda cacheada.
   */
  async limpiarCacheDeDatos(): Promise<void> {
    if (typeof caches === 'undefined') {
      return;
    }
    const nombres = await caches.keys();
    await Promise.all(nombres.filter(nombre => esCacheDeDatos(nombre)).map(nombre => caches.delete(nombre)));
  }
}

/** Las caches de dataGroups de Angular se llaman "ngsw:<ruta>:<version>:data:<tipo>:<grupo>:cache". */
export function esCacheDeDatos(nombre: string): boolean {
  return nombre.startsWith('ngsw:') && nombre.includes(':data:');
}
