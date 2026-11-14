import { TestBed } from '@angular/core/testing';
import { SwUpdate, VersionEvent } from '@angular/service-worker';
import { Subject } from 'rxjs';

import { PwaService, esCacheDeDatos } from './pwa.service';

describe('PwaService', () => {
  describe('esCacheDeDatos', () => {
    it.each([
      ['ngsw:/:1:data:dynamic:api-reservas:cache', true],
      ['ngsw:/:1:data:dynamic:api-catalogo:cache', true],
      ['ngsw:/:1:data:dynamic:clima-open-meteo:cache', true],
      ['ngsw:/:1:assets:app:cache', false],
      ['ngsw:/:db:control', false],
      ['otra-cache', false],
    ])('"%s" -> %s', (nombre, esperado) => {
      expect(esCacheDeDatos(nombre)).toBe(esperado);
    });
  });

  describe('sin service worker (tests, navegadores sin soporte)', () => {
    let service: PwaService;

    beforeEach(() => {
      TestBed.configureTestingModule({});
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
        keys: jest
          .fn()
          .mockResolvedValue([
            'ngsw:/:1:assets:app:cache',
            'ngsw:/:1:data:dynamic:api-reservas:cache',
            'ngsw:/:1:data:dynamic:api-catalogo:cache',
          ]),
        delete: jest.fn().mockResolvedValue(true),
      };
      Object.defineProperty(window, 'caches', { value: cachesMock, configurable: true });

      await service.limpiarCacheDeDatos();

      expect(cachesMock.delete).toHaveBeenCalledTimes(2);
      expect(cachesMock.delete).toHaveBeenCalledWith('ngsw:/:1:data:dynamic:api-reservas:cache');
      expect(cachesMock.delete).toHaveBeenCalledWith('ngsw:/:1:data:dynamic:api-catalogo:cache');
      expect(cachesMock.delete).not.toHaveBeenCalledWith('ngsw:/:1:assets:app:cache');
    });

    it('no hay versión nueva', () => {
      expect(service.hayNuevaVersion()).toBe(false);
    });
  });

  describe('con service worker', () => {
    it('avisa cuando hay una versión nueva lista', () => {
      const versionUpdates = new Subject<VersionEvent>();
      TestBed.configureTestingModule({
        providers: [{ provide: SwUpdate, useValue: { isEnabled: true, versionUpdates, activateUpdate: jest.fn() } }],
      });
      const service = TestBed.inject(PwaService);

      versionUpdates.next({ type: 'VERSION_DETECTED', version: { hash: 'nueva' } });
      expect(service.hayNuevaVersion()).toBe(false);

      versionUpdates.next({ type: 'VERSION_READY', currentVersion: { hash: 'vieja' }, latestVersion: { hash: 'nueva' } });
      expect(service.hayNuevaVersion()).toBe(true);
    });
  });
});
