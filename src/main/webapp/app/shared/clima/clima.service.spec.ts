import { TestBed } from '@angular/core/testing';
import { provideHttpClient } from '@angular/common/http';
import { HttpTestingController, provideHttpClientTesting } from '@angular/common/http/testing';

import { ClimaService, OPEN_METEO_URL, PronosticoDia, RespuestaOpenMeteo, describirCodigoClima } from './clima.service';

describe('ClimaService', () => {
  let service: ClimaService;
  let httpMock: HttpTestingController;

  const respuesta: RespuestaOpenMeteo = {
    daily: {
      time: ['2026-11-16', '2026-11-17'],
      weather_code: [0, 63],
      temperature_2m_max: [30.6, 24.2],
      temperature_2m_min: [15.4, 12.8],
      precipitation_probability_max: [0, null],
    },
  };

  beforeEach(() => {
    TestBed.configureTestingModule({
      providers: [provideHttpClient(), provideHttpClientTesting()],
    });
    service = TestBed.inject(ClimaService);
    httpMock = TestBed.inject(HttpTestingController);
  });

  afterEach(() => {
    httpMock.verify();
  });

  it('consulta Open-Meteo con la ubicación del club y devuelve el pronóstico del día pedido', () => {
    let resultado: PronosticoDia | null | undefined;
    service.pronosticoDelDia('2026-11-17').subscribe(r => (resultado = r));

    const req = httpMock.expectOne(r => r.url === OPEN_METEO_URL);
    expect(req.request.method).toBe('GET');
    expect(req.request.params.get('latitude')).toBe('-32.89');
    expect(req.request.params.get('timezone')).toBe('America/Argentina/Mendoza');
    // No se manda el token de la app a una API externa
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush(respuesta);

    expect(resultado).toEqual({
      fecha: '2026-11-17',
      descripcion: 'Lluvia',
      temperaturaMax: 24,
      temperaturaMin: 13,
      probabilidadLluvia: 0,
    });
  });

  it('devuelve null si la fecha está fuera del pronóstico', () => {
    let resultado: PronosticoDia | null | undefined;
    service.pronosticoDelDia('2026-12-31').subscribe(r => (resultado = r));

    httpMock.expectOne(r => r.url === OPEN_METEO_URL).flush(respuesta);

    expect(resultado).toBeNull();
  });

  it('devuelve null si la API falla (el clima nunca impide reservar)', () => {
    let resultado: PronosticoDia | null | undefined;
    service.pronosticoDelDia('2026-11-16').subscribe(r => (resultado = r));

    httpMock.expectOne(r => r.url === OPEN_METEO_URL).flush('error', { status: 500, statusText: 'Server Error' });

    expect(resultado).toBeNull();
  });

  describe('describirCodigoClima', () => {
    it.each([
      [0, 'Despejado'],
      [2, 'Parcialmente nublado'],
      [45, 'Niebla'],
      [53, 'Llovizna'],
      [81, 'Lluvia'],
      [73, 'Nieve'],
      [95, 'Tormenta'],
      [42, 'Sin datos'],
    ])('el código %i es "%s"', (codigo, descripcion) => {
      expect(describirCodigoClima(codigo)).toBe(descripcion);
    });
  });
});
