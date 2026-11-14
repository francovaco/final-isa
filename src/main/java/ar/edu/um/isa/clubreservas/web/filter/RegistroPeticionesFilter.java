package ar.edu.um.isa.clubreservas.web.filter;

import static net.logstash.logback.argument.StructuredArguments.kv;

import ar.edu.um.isa.clubreservas.security.SecurityUtils;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Registra cada petición a la API (método, ruta, status, duración y usuario) para ELK.
 *
 * <p>También pone el usuario en el MDC: así todos los logs que se generan durante la petición
 * llevan el campo "usuario". Corre después de Spring Security, cuando ya se sabe quién es.
 */
@Component
public class RegistroPeticionesFilter extends OncePerRequestFilter {

    private static final Logger LOG = LoggerFactory.getLogger(RegistroPeticionesFilter.class);
    static final String MDC_USUARIO = "usuario";

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith("/api/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
        throws ServletException, IOException {
        long inicio = System.nanoTime();
        SecurityUtils.getCurrentUserLogin().ifPresent(login -> MDC.put(MDC_USUARIO, login));
        try {
            chain.doFilter(request, response);
        } finally {
            long duracionMs = (System.nanoTime() - inicio) / 1_000_000;
            LOG.info(
                "{} {} -> {} ({} ms) {}",
                request.getMethod(),
                request.getRequestURI(),
                response.getStatus(),
                duracionMs,
                kv("evento", "HTTP"),
                kv("metodo", request.getMethod()),
                kv("ruta", normalizarRuta(request.getRequestURI())),
                kv("status", response.getStatus()),
                kv("duracion_ms", duracionMs)
            );
            MDC.remove(MDC_USUARIO);
        }
    }

    /**
     * Reemplaza los ids numéricos por {id} para poder agrupar en Kibana
     * (ej. /api/reservas/15/cancelar -> /api/reservas/{id}/cancelar).
     */
    static String normalizarRuta(String ruta) {
        return ruta.replaceAll("/\\d+(?=/|$)", "/{id}");
    }
}
