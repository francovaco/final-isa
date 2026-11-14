package ar.edu.um.isa.clubreservas.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import ar.edu.um.isa.clubreservas.repository.CanchaRepository;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.repository.UserRepository;
import ar.edu.um.isa.clubreservas.security.AuthoritiesConstants;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.ReservaMapperImpl;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Tests de unidad de las reglas de negocio de {@link ReservaService}.
 *
 * <p>No levantan Spring ni base de datos: los repositorios se simulan con Mockito
 * y el reloj está fijo, así los resultados no dependen de la fecha real.
 */
@ExtendWith(MockitoExtension.class)
class ReservaServiceTest {

    /** "Ahora" en los tests: lunes 16/11/2026 a las 09:00 hora de Mendoza (12:00 UTC). */
    private static final Instant AHORA = Instant.parse("2026-11-16T12:00:00Z");
    private static final LocalDate HOY = LocalDate.of(2026, 11, 16);
    private static final Long CANCHA_ID = 10L;

    @Mock
    private ReservaRepository reservaRepository;

    @Mock
    private CanchaRepository canchaRepository;

    @Mock
    private UserRepository userRepository;

    private ReservaService reservaService;

    private Cancha cancha;
    private User socio;

    @BeforeEach
    void setUp() {
        Clock reloj = Clock.fixed(AHORA, ZoneOffset.UTC);
        reservaService = new ReservaService(reservaRepository, new ReservaMapperImpl(), canchaRepository, userRepository, reloj);

        cancha = new Cancha().nombre("Cancha 1").activa(true);
        cancha.setId(CANCHA_ID);
        socio = new User();
        socio.setId(1L);
        socio.setLogin("socio");

        loguearComo("socio", AuthoritiesConstants.USER);
        lenient().when(userRepository.findOneByLogin("socio")).thenReturn(Optional.of(socio));
        lenient().when(canchaRepository.findById(CANCHA_ID)).thenReturn(Optional.of(cancha));
        lenient().when(reservaRepository.save(any(Reserva.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ------------------------------------------------------------------ helpers

    private static void loguearComo(String login, String rol) {
        SecurityContextHolder.getContext()
            .setAuthentication(new UsernamePasswordAuthenticationToken(login, "x", List.of(new SimpleGrantedAuthority(rol))));
    }

    /** Instante para un día (relativo a hoy) y una hora local del club. */
    private static Instant horaClub(int diasDesdeHoy, int hora, int minuto) {
        return HOY.plusDays(diasDesdeHoy).atTime(LocalTime.of(hora, minuto)).atZone(ReservaService.ZONA_CLUB).toInstant();
    }

    private static ReservaDTO pedido(Instant inicio, int duracion) {
        CanchaDTO canchaDTO = new CanchaDTO();
        canchaDTO.setId(CANCHA_ID);
        ReservaDTO dto = new ReservaDTO();
        dto.setInicio(inicio);
        dto.setDuracionMinutos(duracion);
        dto.setEstado(EstadoReserva.CONFIRMADA);
        dto.setCancha(canchaDTO);
        return dto;
    }

    private Reserva existente(Instant inicio, int duracion) {
        Reserva r = new Reserva().inicio(inicio).duracionMinutos(duracion).estado(EstadoReserva.CONFIRMADA).cancha(cancha);
        r.setUsuario(socio);
        return r;
    }

    private void assertRechazada(ReservaDTO dto, String codigo) {
        assertThatThrownBy(() -> reservaService.save(dto))
            .isInstanceOf(ReglaReservaException.class)
            .extracting(e -> ((ReglaReservaException) e).getCodigo())
            .isEqualTo(codigo);
        verify(reservaRepository, never()).save(any());
    }

    // ------------------------------------------------------------------ crear reserva

    @Nested
    @DisplayName("Crear reserva")
    class Crear {

        @Test
        @DisplayName("una reserva válida se guarda con el usuario logueado, estado CONFIRMADA y fecha de creación")
        void reservaValidaSeGuarda() {
            ReservaDTO creada = reservaService.save(pedido(horaClub(1, 10, 0), 60));

            assertThat(creada.getUsuario().getLogin()).isEqualTo("socio");
            assertThat(creada.getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
            assertThat(creada.getCreadaEn()).isEqualTo(AHORA);
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("ignora el estado que manda el cliente: siempre se crea CONFIRMADA")
        void ignoraEstadoDelCliente() {
            ReservaDTO dto = pedido(horaClub(1, 10, 0), 60);
            dto.setEstado(EstadoReserva.FINALIZADA);

            assertThat(reservaService.save(dto).getEstado()).isEqualTo(EstadoReserva.CONFIRMADA);
        }

        @Test
        @DisplayName("rechaza una cancha inactiva")
        void rechazaCanchaInactiva() {
            cancha.setActiva(false);
            assertRechazada(pedido(horaClub(1, 10, 0), 60), "canchainactiva");
        }

        @Test
        @DisplayName("rechaza una duración que no es 60, 90 o 120 minutos")
        void rechazaDuracionInvalida() {
            assertRechazada(pedido(horaClub(1, 10, 0), 75), "duracioninvalida");
        }

        @Test
        @DisplayName("acepta las duraciones de 90 y 120 minutos")
        void aceptaDuracionesPermitidas() {
            reservaService.save(pedido(horaClub(1, 10, 0), 90));
            reservaService.save(pedido(horaClub(2, 10, 0), 120));
            verify(reservaRepository, org.mockito.Mockito.times(2)).save(any(Reserva.class));
        }

        @Test
        @DisplayName("rechaza una reserva en el pasado")
        void rechazaPasado() {
            assertRechazada(pedido(horaClub(-1, 10, 0), 60), "enelpasado");
        }

        @Test
        @DisplayName("rechaza una reserva con más de 14 días de anticipación")
        void rechazaMuyAnticipada() {
            assertRechazada(pedido(horaClub(15, 10, 0), 60), "muyanticipada");
        }

        @Test
        @DisplayName("acepta una reserva con 13 días de anticipación")
        void aceptaDentroDeLas2Semanas() {
            reservaService.save(pedido(horaClub(13, 10, 0), 60));
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("rechaza una reserva antes de la apertura (08:00)")
        void rechazaAntesDeApertura() {
            assertRechazada(pedido(horaClub(1, 7, 30), 60), "fueradehorario");
        }

        @Test
        @DisplayName("rechaza una reserva que termina después del cierre (23:00)")
        void rechazaDespuesDelCierre() {
            assertRechazada(pedido(horaClub(1, 22, 30), 60), "fueradehorario");
        }

        @Test
        @DisplayName("acepta una reserva que termina justo a las 23:00")
        void aceptaTerminarAlCierre() {
            reservaService.save(pedido(horaClub(1, 21, 0), 120));
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("rechaza un horario que no empieza en punto o y media")
        void rechazaHoraNoRedonda() {
            assertRechazada(pedido(horaClub(1, 10, 15), 60), "fueradehorario");
        }

        @Test
        @DisplayName("rechaza la cuarta reserva activa del socio")
        void rechazaLimiteDeReservas() {
            when(
                reservaRepository.countByUsuarioLoginAndEstadoAndInicioAfter(eq("socio"), eq(EstadoReserva.CONFIRMADA), eq(AHORA))
            ).thenReturn(3L);
            assertRechazada(pedido(horaClub(1, 10, 0), 60), "limitereservas");
        }

        @Test
        @DisplayName("acepta la tercera reserva activa del socio")
        void aceptaTerceraReserva() {
            when(reservaRepository.countByUsuarioLoginAndEstadoAndInicioAfter(anyString(), any(), any())).thenReturn(2L);
            reservaService.save(pedido(horaClub(1, 10, 0), 60));
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("rechaza una reserva que se superpone parcialmente con otra de la misma cancha")
        void rechazaSuperposicion() {
            // Ya existe 10:00-11:30; se pide 11:00-12:00
            when(reservaRepository.findByCanchaIdAndEstadoAndInicioBetween(anyLong(), any(), any(), any())).thenReturn(
                List.of(existente(horaClub(1, 10, 0), 90))
            );
            assertRechazada(pedido(horaClub(1, 11, 0), 60), "superposicion");
        }

        @Test
        @DisplayName("acepta una reserva que empieza justo cuando termina otra")
        void aceptaReservaContigua() {
            // Ya existe 10:00-11:00; se pide 11:00-12:00
            when(reservaRepository.findByCanchaIdAndEstadoAndInicioBetween(anyLong(), any(), any(), any())).thenReturn(
                List.of(existente(horaClub(1, 10, 0), 60))
            );
            reservaService.save(pedido(horaClub(1, 11, 0), 60));
            verify(reservaRepository).save(any(Reserva.class));
        }

        @Test
        @DisplayName("busca superposiciones solo entre reservas CONFIRMADAS de la misma cancha")
        void buscaSoloConfirmadasDeLaCancha() {
            Instant inicio = horaClub(1, 10, 0);
            reservaService.save(pedido(inicio, 60));
            verify(reservaRepository).findByCanchaIdAndEstadoAndInicioBetween(
                CANCHA_ID,
                EstadoReserva.CONFIRMADA,
                inicio.minusSeconds(120 * 60),
                inicio.plusSeconds(60 * 60)
            );
        }
    }

    // ------------------------------------------------------------------ superposición

    @Nested
    @DisplayName("Superposición de horarios")
    class Superposicion {

        private final Instant diez = horaClub(1, 10, 0);
        private final Instant once = horaClub(1, 11, 0);

        @Test
        @DisplayName("el mismo horario exacto se superpone")
        void mismoHorario() {
            assertThat(ReservaService.seSuperponen(diez, once, existente(diez, 60))).isTrue();
        }

        @Test
        @DisplayName("una reserva contenida dentro de otra se superpone")
        void contenida() {
            assertThat(ReservaService.seSuperponen(diez, horaClub(1, 12, 0), existente(horaClub(1, 10, 30), 60))).isTrue();
        }

        @Test
        @DisplayName("reservas contiguas no se superponen")
        void contiguas() {
            assertThat(ReservaService.seSuperponen(once, horaClub(1, 12, 0), existente(diez, 60))).isFalse();
        }
    }

    // ------------------------------------------------------------------ cancelar

    @Nested
    @DisplayName("Cancelar reserva")
    class Cancelar {

        @Test
        @DisplayName("el socio cancela su reserva con más de 2 horas de anticipación")
        void socioCancelaATiempo() {
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(0, 12, 0), 60)));

            Optional<ReservaDTO> cancelada = reservaService.cancelar(5L);

            assertThat(cancelada).isPresent();
            assertThat(cancelada.orElseThrow().getEstado()).isEqualTo(EstadoReserva.CANCELADA);
        }

        @Test
        @DisplayName("el socio no puede cancelar con menos de 2 horas de anticipación")
        void socioNoCancelaTarde() {
            // Son las 09:00 y la reserva es a las 10:30
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(0, 10, 30), 60)));

            assertThatThrownBy(() -> reservaService.cancelar(5L))
                .isInstanceOf(ReglaReservaException.class)
                .extracting(e -> ((ReglaReservaException) e).getCodigo())
                .isEqualTo("cancelaciontardia");
        }

        @Test
        @DisplayName("el admin puede cancelar aunque falten menos de 2 horas")
        void adminCancelaSiempre() {
            loguearComo("admin", AuthoritiesConstants.ADMIN);
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(0, 10, 0), 60)));

            assertThat(reservaService.cancelar(5L)).get().extracting(ReservaDTO::getEstado).isEqualTo(EstadoReserva.CANCELADA);
        }

        @Test
        @DisplayName("un socio no puede cancelar la reserva de otro (se responde como si no existiera)")
        void socioNoCancelaAjena() {
            loguearComo("otro", AuthoritiesConstants.USER);
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(1, 10, 0), 60)));

            assertThat(reservaService.cancelar(5L)).isEmpty();
            verify(reservaRepository, never()).save(any());
        }

        @Test
        @DisplayName("no se puede cancelar una reserva que ya está cancelada")
        void noCancelaDosVeces() {
            Reserva cancelada = existente(horaClub(1, 10, 0), 60).estado(EstadoReserva.CANCELADA);
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(cancelada));

            assertThatThrownBy(() -> reservaService.cancelar(5L))
                .isInstanceOf(ReglaReservaException.class)
                .extracting(e -> ((ReglaReservaException) e).getCodigo())
                .isEqualTo("nocancelable");
        }
    }

    // ------------------------------------------------------------------ ver reserva

    @Nested
    @DisplayName("Ver reserva")
    class Ver {

        @Test
        @DisplayName("el socio ve su propia reserva")
        void socioVeLaSuya() {
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(1, 10, 0), 60)));
            assertThat(reservaService.findOne(5L)).isPresent();
        }

        @Test
        @DisplayName("el socio no ve la reserva de otro")
        void socioNoVeAjena() {
            loguearComo("otro", AuthoritiesConstants.USER);
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(1, 10, 0), 60)));
            assertThat(reservaService.findOne(5L)).isEmpty();
        }

        @Test
        @DisplayName("el admin ve cualquier reserva")
        void adminVeTodas() {
            loguearComo("admin", AuthoritiesConstants.ADMIN);
            when(reservaRepository.findOneWithEagerRelationships(5L)).thenReturn(Optional.of(existente(horaClub(1, 10, 0), 60)));
            assertThat(reservaService.findOne(5L)).isPresent();
        }
    }
}
