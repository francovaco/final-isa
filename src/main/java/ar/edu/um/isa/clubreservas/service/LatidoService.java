package ar.edu.um.isa.clubreservas.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

/**
 * Emite un log "LATIDO" cada minuto. La alerta "La app no envía logs" de Kibana se dispara
 * si no llega ningún latido en 3 minutos: así se detecta la app caída aunque no tenga tráfico.
 */
@Service
public class LatidoService {

    private static final Logger LOG = LoggerFactory.getLogger(LatidoService.class);

    @Scheduled(fixedRateString = "${application.latido.intervalo-ms:60000}", initialDelay = 10_000)
    public void latir() {
        LOG.info("Latido de la aplicación {}", kv("evento", "LATIDO"));
    }
}
