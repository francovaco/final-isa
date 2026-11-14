package ar.edu.um.isa.clubreservas.web.rest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.isa.clubreservas.IntegrationTest;
import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import ar.edu.um.isa.clubreservas.domain.enumeration.Superficie;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.repository.UserRepository;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Tests de integración de las reglas de negocio y permisos de reservas, a través de la API REST
 * (con base de datos real y Spring Security). Usa los usuarios "user" (socio) y "admin" que carga Liquibase.
 */
@IntegrationTest
@AutoConfigureMockMvc
@Transactional
class ReservaReglasIT {

    private static final String API_RESERVAS = "/api/reservas";

    @Autowired
    private ObjectMapper om;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    private Cancha cancha;

    @BeforeEach
    void crearCancha() {
        Deporte deporte = new Deporte().nombre("Padel IT").jugadoresPorEquipo(2);
        em.persist(deporte);
        cancha = new Cancha()
            .nombre("Cancha IT")
            .superficie(Superficie.CEMENTO)
            .techada(true)
            .precioPorHora(new BigDecimal("15000"))
            .activa(true)
            .deporte(deporte);
        em.persist(cancha);
        em.flush();
    }

    private ReservaDTO pedido(Instant inicio) {
        CanchaDTO canchaDTO = new CanchaDTO();
        canchaDTO.setId(cancha.getId());
        ReservaDTO dto = new ReservaDTO();
        dto.setInicio(inicio);
        dto.setDuracionMinutos(60);
        dto.setEstado(EstadoReserva.CONFIRMADA);
        dto.setCancha(canchaDTO);
        return dto;
    }

    private Reserva reservaDe(String login, Instant inicio) {
        User usuario = userRepository.findOneByLogin(login).orElseThrow();
        Reserva reserva = new Reserva().inicio(inicio).duracionMinutos(60).estado(EstadoReserva.CONFIRMADA).cancha(cancha);
        reserva.setUsuario(usuario);
        return reservaRepository.saveAndFlush(reserva);
    }

    private static Instant manianaALas(int hora) {
        return ReservaResourceIT.proximoTurnoValido().plus(hora - 10L, ChronoUnit.HOURS);
    }

    @Test
    @WithMockUser(username = "user")
    void socioCreaReservaYQuedaComoDuenio() throws Exception {
        mvc
            .perform(post(API_RESERVAS).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pedido(manianaALas(10)))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.usuario.login").value("user"))
            .andExpect(jsonPath("$.estado").value("CONFIRMADA"))
            .andExpect(jsonPath("$.creadaEn").isNotEmpty());
    }

    @Test
    @WithMockUser(username = "user")
    void reservaSuperpuestaDevuelve400ConElMotivo() throws Exception {
        reservaDe("admin", manianaALas(10));

        mvc
            .perform(post(API_RESERVAS).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(pedido(manianaALas(10)))))
            .andExpect(status().isBadRequest())
            .andExpect(jsonPath("$.message").value("error.superposicion"))
            .andExpect(jsonPath("$.detail").value("La cancha ya está reservada en ese horario"));
    }

    @Test
    @WithMockUser(username = "user")
    void socioSoloVeSusReservas() throws Exception {
        Reserva propia = reservaDe("user", manianaALas(10));
        Reserva ajena = reservaDe("admin", manianaALas(12));

        mvc
            .perform(get(API_RESERVAS + "?sort=id,asc"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$", hasSize(1)))
            .andExpect(jsonPath("$[0].id").value(propia.getId()));

        mvc.perform(get(API_RESERVAS + "/{id}", ajena.getId())).andExpect(status().isNotFound());
        mvc.perform(get(API_RESERVAS + "/count")).andExpect(status().isOk()).andExpect(content().string("1"));
    }

    @Test
    @WithMockUser(username = "admin", authorities = "ROLE_ADMIN")
    void adminVeTodasLasReservas() throws Exception {
        reservaDe("user", manianaALas(10));
        reservaDe("admin", manianaALas(12));

        mvc.perform(get(API_RESERVAS)).andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @WithMockUser(username = "user")
    void socioCancelaSuReserva() throws Exception {
        Reserva propia = reservaDe("user", manianaALas(10));

        mvc
            .perform(post(API_RESERVAS + "/{id}/cancelar", propia.getId()))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADA"));

        assertThat(reservaRepository.findById(propia.getId())).get().extracting(Reserva::getEstado).isEqualTo(EstadoReserva.CANCELADA);
    }

    @Test
    @WithMockUser(username = "user")
    void socioNoPuedeCancelarReservaAjena() throws Exception {
        Reserva ajena = reservaDe("admin", manianaALas(10));

        mvc.perform(post(API_RESERVAS + "/{id}/cancelar", ajena.getId())).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "user")
    void socioNoPuedeEditarNiBorrarReservas() throws Exception {
        Reserva propia = reservaDe("user", manianaALas(10));

        mvc.perform(delete(API_RESERVAS + "/{id}", propia.getId())).andExpect(status().isForbidden());
        mvc
            .perform(
                put(API_RESERVAS + "/{id}", propia.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(pedido(manianaALas(11))))
            )
            .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "user")
    void socioPuedeVerCanchasPeroNoCrearDeportes() throws Exception {
        mvc.perform(get("/api/canchas")).andExpect(status().isOk());

        DeporteDTO deporte = new DeporteDTO();
        deporte.setNombre("Tenis");
        mvc
            .perform(post("/api/deportes").contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporte)))
            .andExpect(status().isForbidden());
    }
}
