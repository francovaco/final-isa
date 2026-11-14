#!/usr/bin/env bash
# Configura Kibana para ClubReservas (corre una vez, cuando Kibana está listo):
#  - data view "ClubReservas logs" (logstash-clubreservas-*)
#  - dashboard y visualizaciones (importados de /objetos/clubreservas.ndjson)
#  - conectores (log del servidor e índice alertas-clubreservas) y reglas de alerta
# Es idempotente: conectores y reglas se borran y se vuelven a crear con ids fijos.
set -euo pipefail

KB=http://kibana:5601
AUTH="elastic:${ELASTIC_PASSWORD}"

echo "Esperando a Kibana..."
until [ "$(curl -s -o /dev/null -w '%{http_code}' -u "$AUTH" "$KB/api/status")" = "200" ]; do
  sleep 5
done

kibana() { # metodo ruta [datos]
  curl -s -o /tmp/respuesta -w '%{http_code}' -u "$AUTH" -X "$1" "$KB$2" -H 'kbn-xsrf: true' -H 'Content-Type: application/json' ${3:+-d "$3"}
}

verificar() { # codigo descripcion
  if [[ "$1" != 2* ]]; then
    echo "ERROR ($1) $2: $(cat /tmp/respuesta)"
    exit 1
  fi
  echo "OK  $2"
}

# --- Data views -------------------------------------------------------------
verificar "$(kibana POST /api/data_views/data_view '{"override":true,"data_view":{"id":"clubreservas-logs","title":"logstash-clubreservas-*","name":"ClubReservas logs","timeFieldName":"@timestamp"}}')" "data view ClubReservas logs"
verificar "$(kibana POST /api/data_views/data_view '{"override":true,"data_view":{"id":"clubreservas-alertas","title":"alertas-clubreservas","name":"ClubReservas alertas","timeFieldName":"@timestamp"}}')" "data view ClubReservas alertas"

# --- Dashboard y visualizaciones -------------------------------------------
if [ -f /objetos/clubreservas.ndjson ]; then
  codigo=$(curl -s -o /tmp/respuesta -w '%{http_code}' -u "$AUTH" -X POST "$KB/api/saved_objects/_import?overwrite=true" -H 'kbn-xsrf: true' -F file=@/objetos/clubreservas.ndjson)
  verificar "$codigo" "dashboard y visualizaciones"
  grep -q '"success":true' /tmp/respuesta || { echo "ERROR importando objetos: $(cat /tmp/respuesta)"; exit 1; }
fi

# --- Conectores y reglas (se recrean) --------------------------------------
for regla in /setup/reglas/*.json; do
  kibana DELETE "/api/alerting/rule/$(basename "$regla" .json)" >/dev/null || true
done
for conector in cr-server-log cr-indice-alertas; do
  kibana DELETE "/api/actions/connector/$conector" >/dev/null || true
done

verificar "$(kibana POST /api/actions/connector/cr-server-log '{"name":"ClubReservas - Log de Kibana","connector_type_id":".server-log","config":{},"secrets":{}}')" "conector Server log"
verificar "$(kibana POST /api/actions/connector/cr-indice-alertas '{"name":"ClubReservas - Índice alertas-clubreservas","connector_type_id":".index","config":{"index":"alertas-clubreservas","refresh":true},"secrets":{}}')" "conector Index (alertas-clubreservas)"

for regla in /setup/reglas/*.json; do
  verificar "$(kibana POST "/api/alerting/rule/$(basename "$regla" .json)" "$(cat "$regla")")" "regla $(basename "$regla" .json)"
done

echo "Kibana listo: http://localhost:5601"
