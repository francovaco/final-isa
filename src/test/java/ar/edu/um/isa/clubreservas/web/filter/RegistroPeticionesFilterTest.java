package ar.edu.um.isa.clubreservas.web.filter;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.servlet.FilterChain;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.slf4j.MDC;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests de unidad del filtro que registra las peticiones a la API para ELK.
 */
class RegistroPeticionesFilterTest {

    private final RegistroPeticionesFilter filtro = new RegistroPeticionesFilter();

    @AfterEach
    void limpiar() {
        SecurityContextHolder.clearContext();
        MDC.clear();
    }

    @Test
    void normalizaLosIdsDeLaRutaParaPoderAgrupar() {
        assertThat(RegistroPeticionesFilter.normalizarRuta("/api/reservas/15")).isEqualTo("/api/reservas/{id}");
        assertThat(RegistroPeticionesFilter.normalizarRuta("/api/reservas/15/cancelar")).isEqualTo("/api/reservas/{id}/cancelar");
        assertThat(RegistroPeticionesFilter.normalizarRuta("/api/reservas")).isEqualTo("/api/reservas");
        assertThat(RegistroPeticionesFilter.normalizarRuta("/api/v2/canchas")).isEqualTo("/api/v2/canchas");
    }

    @Test
    void soloRegistraLasPeticionesALaApi() {
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("GET", "/api/reservas"))).isFalse();
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("GET", "/index.html"))).isTrue();
        assertThat(filtro.shouldNotFilter(new MockHttpServletRequest("GET", "/management/health"))).isTrue();
    }

    @Test
    void ponElUsuarioEnElMdcDuranteLaPeticionYLoSacaAlTerminar() throws Exception {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken("socio", "x", List.of(new SimpleGrantedAuthority("ROLE_USER"))));
        AtomicReference<String> usuarioDuranteLaPeticion = new AtomicReference<>();
        FilterChain cadena = (req, res) -> usuarioDuranteLaPeticion.set(MDC.get(RegistroPeticionesFilter.MDC_USUARIO));

        filtro.doFilter(new MockHttpServletRequest("GET", "/api/reservas"), new MockHttpServletResponse(), cadena);

        assertThat(usuarioDuranteLaPeticion.get()).isEqualTo("socio");
        assertThat(MDC.get(RegistroPeticionesFilter.MDC_USUARIO)).isNull();
    }

    @Test
    void sinUsuarioLogueadoNoPoneNadaEnElMdc() throws Exception {
        AtomicReference<String> usuarioDuranteLaPeticion = new AtomicReference<>("sin-ejecutar");
        FilterChain cadena = (req, res) -> usuarioDuranteLaPeticion.set(MDC.get(RegistroPeticionesFilter.MDC_USUARIO));

        filtro.doFilter(new MockHttpServletRequest("POST", "/api/authenticate"), new MockHttpServletResponse(), cadena);

        assertThat(usuarioDuranteLaPeticion.get()).isNull();
    }
}
