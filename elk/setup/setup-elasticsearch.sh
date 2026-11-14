#!/usr/bin/env bash
# Prepara Elasticsearch para ClubReservas (corre una vez, antes de Kibana y Logstash):
#  - contraseña del usuario kibana_system
#  - rol logstash_writer y usuario logstash_internal (el que usa Logstash para escribir)
#  - plantilla de índice para logstash-clubreservas-* (campos keyword para filtrar y agrupar)
# Es idempotente: se puede correr todas las veces que se levante el compose.
set -euo pipefail

ES=http://elasticsearch:9200
AUTH="elastic:${ELASTIC_PASSWORD}"

echo "Esperando a Elasticsearch..."
until curl -s -u "$AUTH" "$ES/_cluster/health?wait_for_status=yellow&timeout=5s" | grep -q '"status"'; do
  sleep 3
done

llamar() { # metodo ruta datos descripcion
  local codigo
  codigo=$(curl -s -o /tmp/respuesta -w '%{http_code}' -u "$AUTH" -X "$1" "$ES$2" -H 'Content-Type: application/json' -d "$3")
  if [[ "$codigo" != 2* ]]; then
    echo "ERROR ($codigo) $4: $(cat /tmp/respuesta)"
    exit 1
  fi
  echo "OK  $4"
}

llamar POST /_security/user/kibana_system/_password "{\"password\":\"${KIBANA_SYSTEM_PASSWORD}\"}" "contraseña de kibana_system"
llamar PUT /_security/role/logstash_writer "$(cat /setup/rol-logstash-writer.json)" "rol logstash_writer"
llamar PUT /_security/user/logstash_internal "{\"password\":\"${LOGSTASH_INTERNAL_PASSWORD}\",\"roles\":[\"logstash_writer\"],\"full_name\":\"Logstash (ClubReservas)\"}" "usuario logstash_internal"
llamar PUT /_index_template/clubreservas-logs "$(cat /setup/plantilla-indice.json)" "plantilla de índice logstash-clubreservas-*"

echo "Elasticsearch listo."
