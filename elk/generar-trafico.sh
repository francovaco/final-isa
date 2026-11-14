#!/usr/bin/env bash
# Genera tráfico en ClubReservas para llenar el dashboard de Kibana y disparar las alertas.
#
#   ./elk/generar-trafico.sh                 (app en http://localhost:8080)
#   APP_URL=http://otra:8080 ./elk/generar-trafico.sh
#
# Hace: logins fallidos (dispara la alerta de fuerza bruta), reservas válidas,
# reservas rechazadas por cada regla de negocio y cancelaciones.
# Requiere curl y python3 (para leer el JSON de las respuestas).
set -euo pipefail

APP_URL="${APP_URL:-http://localhost:8080}"
JSON='Content-Type: application/json'

# Lee un valor del JSON que llega por stdin con una expresión de Python sobre "d"
json() { python3 -c "import sys,json; d=json.load(sys.stdin); print($1)"; }

token() { # usuario contraseña
  curl -s -X POST "$APP_URL/api/authenticate" -H "$JSON" -d "{\"username\":\"$1\",\"password\":\"$2\"}" | json 'd.get("id_token","")'
}

reservar() { # token cancha_id fecha(YYYY-MM-DD) hora(HH:MM) duracion
  curl -s -o /dev/null -w '%{http_code}' -X POST "$APP_URL/api/reservas" -H "$JSON" -H "Authorization: Bearer $1" \
    -d "{\"inicio\":\"$3T$4:00-03:00\",\"duracionMinutos\":$5,\"estado\":\"CONFIRMADA\",\"cancha\":{\"id\":$2}}"
}

fecha() { # días desde hoy, en la zona horaria del club
  python3 -c "import datetime,zoneinfo; print((datetime.datetime.now(zoneinfo.ZoneInfo('America/Argentina/Mendoza')) + datetime.timedelta(days=$1)).date())"
}

canchas_activas() {
  curl -s "$APP_URL/api/canchas?size=100" -H "Authorization: Bearer $ADMIN" | json '" ".join(str(c["id"]) for c in d if c.get("activa"))'
}

echo "== Logins fallidos (7 en pocos segundos: dispara 'Posible fuerza bruta')"
for _ in $(seq 1 7); do
  curl -s -o /dev/null -w '%{http_code} ' -X POST "$APP_URL/api/authenticate" -H "$JSON" -d '{"username":"user","password":"clave-incorrecta"}'
done
echo

ADMIN=$(token admin admin)
SOCIO=$(token user user)
if [ -z "$ADMIN" ] || [ -z "$SOCIO" ]; then
  echo "No se pudo iniciar sesión en $APP_URL"
  exit 1
fi

CANCHAS=$(canchas_activas)
if [ -z "$CANCHAS" ]; then
  echo "== No hay canchas activas: se crean un deporte y dos canchas"
  SUFIJO=$RANDOM
  DEP=$(curl -s -X POST "$APP_URL/api/deportes" -H "$JSON" -H "Authorization: Bearer $ADMIN" \
    -d "{\"nombre\":\"Pádel $SUFIJO\",\"jugadoresPorEquipo\":2}" | json 'd["id"]')
  for n in 1 2; do
    curl -s -o /dev/null -X POST "$APP_URL/api/canchas" -H "$JSON" -H "Authorization: Bearer $ADMIN" \
      -d "{\"nombre\":\"Cancha $n-$SUFIJO\",\"superficie\":\"CESPED_SINTETICO\",\"techada\":true,\"precioPorHora\":18000,\"activa\":true,\"deporte\":{\"id\":$DEP}}"
  done
  CANCHAS=$(canchas_activas)
fi
CANCHAS=$(echo "$CANCHAS" | tr ' ' '\n' | head -3 | tr '\n' ' ')
echo "== Canchas activas usadas: $CANCHAS"

echo "== Reservas válidas y rechazadas"
DIA=$(((RANDOM % 10) + 2))
HORA=$(printf "%02d" $(((RANDOM % 6) + 9))) # con cero adelante: 09, 10, ...
for C in $CANCHAS; do
  echo "admin, válida (cancha $C):  $(reservar "$ADMIN" "$C" "$(fecha $DIA)" "$HORA:00" 60)"
  echo "socio, superpuesta:        $(reservar "$SOCIO" "$C" "$(fecha $DIA)" "$HORA:30" 60)"
done
C=${CANCHAS%% *}
echo "socio, fuera de horario:   $(reservar "$SOCIO" "$C" "$(fecha 3)" "07:00" 60)"
echo "socio, duración inválida:  $(reservar "$SOCIO" "$C" "$(fecha 3)" "16:00" 75)"
echo "socio, en el pasado:       $(reservar "$SOCIO" "$C" "$(fecha -1)" "16:00" 60)"
echo "socio, muy anticipada:     $(reservar "$SOCIO" "$C" "$(fecha 20)" "16:00" 60)"

echo "== Cancelaciones (el admin cancela las 2 reservas confirmadas más viejas)"
IDS=$(curl -s "$APP_URL/api/reservas?estado.equals=CONFIRMADA&sort=id,asc&size=2" -H "Authorization: Bearer $ADMIN" | json '" ".join(str(r["id"]) for r in d)')
for ID in $IDS; do
  echo "cancelar reserva $ID:      $(curl -s -o /dev/null -w '%{http_code}' -X POST "$APP_URL/api/reservas/$ID/cancelar" -H "Authorization: Bearer $ADMIN")"
done

echo "== Listo. Dashboard: http://localhost:5601/app/dashboards#/view/clubreservas-operacion"
