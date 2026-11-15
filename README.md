# ClubReservas

[![pipeline](https://github.com/francovaco/final-isa/actions/workflows/pipeline.yml/badge.svg)](https://github.com/francovaco/final-isa/actions/workflows/pipeline.yml)

Trabajo final de **Ingeniería de Software Aplicada** (Universidad de Mendoza).

Aplicación para **reservar canchas de un club deportivo**, hecha con **JHipster 8.11** (Spring Boot + Angular). Integra:

| Requisito                | Cómo está resuelto                                                                                        |
| ------------------------ | --------------------------------------------------------------------------------------------------------- |
| **PWA**                  | Service worker de Angular con caché por estrategia, funcionamiento offline, manifest e íconos instalables |
| **Tests de unidad**      | JUnit 5 + Mockito (backend) y Jest (frontend), sobre las reglas de negocio                                |
| **Tests E2E**            | Playwright: login, reservas, permisos, PWA offline y clima                                                |
| **Logs (ELK)**           | Logs estructurados a Logstash, dashboard en Kibana y 3 alertas                                            |
| **Integración continua** | GitHub Actions: tests, SCA, SAST, secretos, Trivy, E2E y DAST con ZAP en cada push                        |

## Inicio rápido

Solo hace falta **Docker** (no Java ni Node):

```bash
./start.sh
```

Levanta la app, PostgreSQL y ELK, y espera a que todo esté listo.

| Servicio  | URL                                                               | Usuario                            |
| --------- | ----------------------------------------------------------------- | ---------------------------------- |
| App (PWA) | http://localhost:8080                                             | `admin` / `admin`, `user` / `user` |
| Kibana    | http://localhost:5601                                             | `elastic` / `changeme`             |
| Dashboard | http://localhost:5601/app/dashboards#/view/clubreservas-operacion |                                    |

Los comandos básicos (levantar, apagar y correr los tests E2E) están en **[COMANDOS.md](COMANDOS.md)**.

## Funcionalidades

- **Socio** (`ROLE_USER`): reserva canchas, ve **solo sus reservas**, las cancela y consulta el **pronóstico del clima** del día elegido (API pública Open-Meteo).
- **Administrador** (`ROLE_ADMIN`): ABM de deportes y canchas, ve y cancela todas las reservas.
- **Reglas de negocio** (validadas en el backend):
  - sin superposición de horarios en la misma cancha;
  - horario del club de 08:00 a 23:00, turnos de 60, 90 o 120 minutos que empiezan en punto o y media;
  - ni en el pasado ni con más de 14 días de anticipación;
  - máximo 3 reservas activas por socio;
  - solo canchas activas;
  - el socio cancela con al menos 2 horas de anticipación (el admin, siempre).

## Documentación

| Documento                                    | Contenido                                                        |
| -------------------------------------------- | ---------------------------------------------------------------- |
| [docs/arquitectura.md](docs/arquitectura.md) | Componentes, modelo de datos, contenedores y estructura del repo |
| [docs/pwa.md](docs/pwa.md)                   | Service worker, estrategias de caché y funcionamiento offline    |
| [docs/tests.md](docs/tests.md)               | Tests de unidad, integración y E2E                               |
| [docs/ci-cd.md](docs/ci-cd.md)               | Pipeline de GitHub Actions, puertas de seguridad y excepciones   |
| [docs/elk.md](docs/elk.md)                   | Logs, dashboard de Kibana y alertas                              |
| [docs/decisiones.md](docs/decisiones.md)     | Decisiones técnicas y problemas encontrados                      |
| [COMANDOS.md](COMANDOS.md)                   | Comandos para levantar, apagar y correr los tests E2E            |

## Stack

- **Backend:** Java 21, Spring Boot 3.5, Spring Security (JWT), JPA/Hibernate, Liquibase, PostgreSQL 17
- **Frontend:** Angular 19, Bootstrap, Angular Service Worker
- **Logs:** logstash-logback-encoder, Elasticsearch / Logstash / Kibana 9.5.4
- **Tests:** JUnit 5, Mockito, AssertJ, ArchUnit, Testcontainers, Jest, Playwright 1.64
- **CI/CD:** GitHub Actions, audit-ci, Semgrep, Gitleaks, Trivy, OWASP ZAP
- **Infraestructura:** Docker (Dockerfile multi-etapa) y Docker Compose
