#!/usr/bin/env bash
# Levanta ClubReservas completo (app + PostgreSQL + ELK) con docker compose.
#
#   ./start.sh            levanta todo (la primera vez construye la imagen de la app)
#   ./start.sh --build    vuelve a construir la imagen de la app (después de cambiar el código)
#
# Solo necesita Docker (no hace falta Java ni Node).
# Para apagar:  docker compose stop      (los datos quedan en los volúmenes)
# Para borrar todo, datos incluidos:  docker compose down -v
set -euo pipefail
cd "$(dirname "$0")"

if ! docker info >/dev/null 2>&1; then
  echo "Docker no está corriendo. Abrí Docker Desktop y volvé a ejecutar el script."
  exit 1
fi

# --- .env con contraseñas al azar (solo la primera vez) ------------------------
if [ ! -f .env ]; then
  echo "==> Creando .env con contraseñas generadas al azar"
  aleatorio() { openssl rand -hex "$1"; }
  cat > .env <<EOF
# Generado por start.sh; no se sube al repo (ver .env.example)
JWT_BASE64_SECRET=$(openssl rand -base64 64 | tr -d '\n')
POSTGRES_PASSWORD=$(aleatorio 12)
ELASTIC_PASSWORD=$(aleatorio 12)
KIBANA_SYSTEM_PASSWORD=$(aleatorio 12)
LOGSTASH_INTERNAL_PASSWORD=$(aleatorio 12)
KIBANA_ENCRYPTION_KEY=$(aleatorio 32)
EOF
fi

# --- Levantar ----------------------------------------------------------------
echo "==> Levantando app, PostgreSQL y ELK (la primera vez tarda varios minutos)"
docker compose up -d "$@"

esperar() { # descripcion comando
  printf '==> Esperando %s ' "$1"
  for _ in $(seq 1 120); do
    if eval "$2" >/dev/null 2>&1; then
      echo "OK"
      return 0
    fi
    printf '.'
    sleep 5
  done
  echo " no respondió a tiempo (ver: docker compose logs)"
  return 1
}

esperar "a la app" 'curl -sf http://localhost:8080/management/health'
esperar "a que Kibana quede configurado" '[ "$(docker inspect -f "{{.State.Status}} {{.State.ExitCode}}" clubreservas-kibana-setup-1)" = "exited 0" ]'

ELASTIC_PASSWORD=$(grep '^ELASTIC_PASSWORD=' .env | cut -d= -f2-)
cat <<EOF

ClubReservas está listo
  App (PWA):  http://localhost:8080          usuarios: admin/admin, user/user
  Kibana:     http://localhost:5601          usuario: elastic / ${ELASTIC_PASSWORD}
  Dashboard:  http://localhost:5601/app/dashboards#/view/clubreservas-operacion

  Generar tráfico de prueba:  ./elk/generar-trafico.sh
  Apagar:                     docker compose stop
EOF
