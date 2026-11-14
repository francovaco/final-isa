import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError, map } from 'rxjs/operators';

/** Pronóstico resumido de un día. */
export interface PronosticoDia {
  fecha: string; // YYYY-MM-DD
  descripcion: string;
  temperaturaMax: number;
  temperaturaMin: number;
  probabilidadLluvia: number; // %
}

/** Respuesta de Open-Meteo (solo los campos que usamos). */
export interface RespuestaOpenMeteo {
  daily?: {
    time: string[];
    weather_code: number[];
    temperature_2m_max: number[];
    temperature_2m_min: number[];
    precipitation_probability_max: (number | null)[];
  };
}

/** Ubicación del club (Mendoza). */
export const CLUB_LATITUD = -32.89;
export const CLUB_LONGITUD = -68.83;
export const OPEN_METEO_URL = 'https://api.open-meteo.com/v1/forecast';

/**
 * Traduce un código de clima WMO (el que usa Open-Meteo) a una descripción en español.
 * https://open-meteo.com/en/docs#weather_variable_documentation
 */
export function describirCodigoClima(codigo: number): string {
  if (codigo === 0) return 'Despejado';
  if (codigo <= 3) return 'Parcialmente nublado';
  if (codigo === 45 || codigo === 48) return 'Niebla';
  if (codigo >= 51 && codigo <= 57) return 'Llovizna';
  if ((codigo >= 61 && codigo <= 67) || (codigo >= 80 && codigo <= 82)) return 'Lluvia';
  if ((codigo >= 71 && codigo <= 77) || codigo === 85 || codigo === 86) return 'Nieve';
  if (codigo >= 95) return 'Tormenta';
  return 'Sin datos';
}

/** Toma de la respuesta de Open-Meteo el pronóstico de una fecha, o null si no está. */
export function extraerPronostico(respuesta: RespuestaOpenMeteo, fecha: string): PronosticoDia | null {
  const daily = respuesta.daily;
  const indice = daily ? daily.time.indexOf(fecha) : -1;
  if (!daily || indice < 0) {
    return null;
  }
  return {
    fecha,
    descripcion: describirCodigoClima(daily.weather_code[indice]),
    temperaturaMax: Math.round(daily.temperature_2m_max[indice]),
    temperaturaMin: Math.round(daily.temperature_2m_min[indice]),
    probabilidadLluvia: daily.precipitation_probability_max[indice] ?? 0,
  };
}

/**
 * Consulta el pronóstico en la API pública Open-Meteo (no requiere API key).
 * El service worker cachea estas respuestas (estrategia performance, Fase 4).
 */
@Injectable({ providedIn: 'root' })
export class ClimaService {
  private readonly http = inject(HttpClient);

  /**
   * Pronóstico para una fecha (YYYY-MM-DD) en la ubicación del club.
   * Devuelve null si la fecha está fuera del rango del pronóstico o si la API falla:
   * el clima es informativo y nunca debe impedir reservar.
   */
  pronosticoDelDia(fecha: string): Observable<PronosticoDia | null> {
    const params = {
      latitude: CLUB_LATITUD,
      longitude: CLUB_LONGITUD,
      daily: 'weather_code,temperature_2m_max,temperature_2m_min,precipitation_probability_max',
      timezone: 'America/Argentina/Mendoza',
      forecast_days: 16,
    };
    return this.http.get<RespuestaOpenMeteo>(OPEN_METEO_URL, { params }).pipe(
      map(respuesta => extraerPronostico(respuesta, fecha)),
      catchError(() => of(null)),
    );
  }
}
