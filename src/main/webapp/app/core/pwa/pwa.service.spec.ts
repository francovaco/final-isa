import { TestBed } from '@angular/core/testing';

import { CACHES_DE_DATOS, CREAR_SERVICE_WORKER, PwaService, ServiceWorkerPwa, esCacheDeDatos } from './pwa.service';

/** Service worker falso: guarda los listeners para poder disparar los eventos de Workbox. */
function swFalso(): ServiceWorkerPwa & { disparar: (evento: string) => void } {
  const listeners: Record<string, (() => void)[]> = {};
  return {
    addEventListener: jest.fn((evento: string, listener: () => void) => {
      (listeners[evento] ??= []).push(listener);
    }) as unknown as ServiceWorkerPwa['addEventListener'],
    register: jest.fn().mockResolvedValue(undefined),
    messageSkipWaiting: jest.fn(),
    disparar: (evento: string) => (listeners[evento] ?? []).forEach(l => l()),
  };
}

describe('PwaService', () => {
  describe('esCacheDeDatos', () => {
    it.each(CACHES_DE_DATOS)('"%s" es una cache de datos', nombre => {
      expect(esCacheDeDatos(nombre)).toBe(true);
    });

    it.each(['workbox-precache-v2-http://localhost:8080/', 'otra-cache'])('"%s" no es una cache de datos', nombre => {
      expect(esCacheDeDatos(nombre)).toBe(false);
    });
  });

  describe('sin service worker (desarrollo, tests, navegadores sin soporte)', () => {
    let service: PwaService;

    beforeEach(() => {
      TestBed.configureTestingModule({ providers: [{ provide: CREAR_SERVICE_WORKER, useValue: () => null }] });
      service = TestBed.inject(PwaService);
    });

    it('sigue el estado de la conexión del navegador', () => {
      const onLine = jest.spyOn(navigator, 'onLine', 'get');

      onLine.mockReturnValue(false);
      window.dispatchEvent(new Event('offline'));
      expect(service.online()).toBe(false);

      onLine.mockReturnValue(true);
      window.dispatchEvent(new Event('online'));
      expect(service.online()).toBe(true);
    });

    it('al limpiar borra solo las caches de datos de la API, no la app', async () => {
      const cachesMock = {
        keys: jest.fn().mockResolvedValue(['workbox-precache-v2-http://localhost:8080/', 'api-reservas', 'api-catalogo']),
        delete: jest.fn().mockResolvedValue(true),
      };
      Object.defineProperty(window, 'caches', { value: cachesMock, configurable: true });

      await service.limpiarCacheDeDatos();

      expect(cachesMock.delete).toHaveBeenCalledTimes(2);
      expect(cachesMock.delete).toHaveBeenCalledWith('api-reservas');
      expect(cachesMock.delete).toHaveBeenCalledWith('api-catalogo');
      expect(cachesMock.delete).not.toHaveBeenCalledWith('workbox-precache-v2-http://localhost:8080/');
    });

    it('no hay versión nueva', () => {
      expect(service.hayNuevaVersion()).toBe(false);
    });
  });

  describe('con service worker (Workbox)', () => {
    let sw: ReturnType<typeof swFalso>;
    let service: PwaService;

    beforeEach(() => {
      sw = swFalso();
      TestBed.configureTestingModule({ providers: [{ provide: CREAR_SERVICE_WORKER, useValue: () => sw }] });
      service = TestBed.inject(PwaService);
    });

    it('registra el service worker cuando la página ya cargó', () => {
      // En jsdom document.readyState es "complete"
      expect(sw.register).toHaveBeenCalled();
    });

    it('avisa cuando hay una versión nueva esperando', () => {
      expect(service.hayNuevaVersion()).toBe(false);

      sw.disparar('waiting');

      expect(service.hayNuevaVersion()).toBe(true);
    });

    it('al actualizar le pide a la versión nueva que se active', () => {
      service.actualizar();

      expect(sw.messageSkipWaiting).toHaveBeenCalled();
      expect(sw.addEventListener).toHaveBeenCalledWith('controlling', expect.any(Function));
    });
  });
});
