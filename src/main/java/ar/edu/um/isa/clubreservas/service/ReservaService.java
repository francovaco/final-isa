package ar.edu.um.isa.clubreservas.service;

import static net.logstash.logback.argument.StructuredArguments.kv;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import ar.edu.um.isa.clubreservas.repository.CanchaRepository;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.repository.UserRepository;
import ar.edu.um.isa.clubreservas.security.AuthoritiesConstants;
import ar.edu.um.isa.clubreservas.security.SecurityUtils;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.ReservaMapper;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.isa.clubreservas.domain.Reserva}.
 *
 * <p>Contiene las reglas de negocio del club para crear y cancelar reservas.
 */
@Service
@Transactional
public class ReservaService {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaService.class);

    /** Zona horaria del club: los horarios de apertura se evalúan en hora local. */
    public static final ZoneId ZONA_CLUB = ZoneId.of("America/Argentina/Mendoza");
    public static final LocalTime APERTURA = LocalTime.of(8, 0);
    public static final LocalTime CIERRE = LocalTime.of(23, 0);
    public static final Set<Integer> DURACIONES_PERMITIDAS = Set.of(60, 90, 120);
    public static final int MAX_DIAS_ANTICIPACION = 14;
    public static final int MAX_RESERVAS_ACTIVAS = 3;
    public static final Duration ANTICIPACION_MINIMA_CANCELACION = Duration.ofHours(2);
    private static final Duration DURACION_MAXIMA = Duration.ofMinutes(120);

    private final ReservaRepository reservaRepository;

    private final ReservaMapper reservaMapper;

    private final CanchaRepository canchaRepository;

    private final UserRepository userRepository;

    private final Clock clock;

    public ReservaService(
        ReservaRepository reservaRepository,
        ReservaMapper reservaMapper,
        CanchaRepository canchaRepository,
        UserRepository userRepository,
        Clock clock
    ) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
        this.canchaRepository = canchaRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    /**
     * Crea una reserva para el usuario logueado, validando las reglas de negocio.
     * El usuario, el estado (CONFIRMADA) y la fecha de creación los asigna el servidor:
     * se ignora lo que mande el cliente.
     *
     * @param reservaDTO la reserva a crear.
     * @return la reserva creada.
     * @throws ReglaReservaException si no cumple alguna regla.
     */
    public ReservaDTO save(ReservaDTO reservaDTO) {
        LOG.debug("Request to save Reserva : {}", reservaDTO);
        Reserva reserva = reservaMapper.toEntity(reservaDTO);

        String login = SecurityUtils.getCurrentUserLogin()
            .orElseThrow(() -> new ReglaReservaException("sinusuario", "No hay un usuario autenticado"));
        User usuario = userRepository
            .findOneByLogin(login)
            .orElseThrow(() -> new ReglaReservaException("sinusuario", "El usuario autenticado no existe"));
        Cancha cancha = canchaRepository
            .findById(reservaDTO.getCancha().getId())
            .orElseThrow(() -> new ReglaReservaException("canchainexistente", "La cancha no existe"));

        reserva.setCancha(cancha);
        reserva.setUsuario(usuario);
        reserva.setEstado(EstadoReserva.CONFIRMADA);
        reserva.setCreadaEn(Instant.now(clock));

        try {
            validarNuevaReserva(reserva);
        } catch (ReglaReservaException e) {
            LOG.warn(
                "Reserva rechazada {} {} {}",
                kv("evento", "RESERVA_RECHAZADA"),
                kv("motivo", e.getCodigo()),
                kv("cancha", cancha.getNombre())
            );
            throw e;
        }

        reserva = reservaRepository.save(reserva);
        LOG.info(
            "Reserva creada {} {} {} {}",
            kv("evento", "RESERVA_CREADA"),
            kv("reserva_id", reserva.getId()),
            kv("cancha", cancha.getNombre()),
            kv("duracion", reserva.getDuracionMinutos())
        );
        return reservaMapper.toDto(reserva);
    }

    /**
     * Aplica las reglas de negocio a una reserva nueva (ya con cancha y usuario asignados).
     */
    void validarNuevaReserva(Reserva reserva) {
        Instant ahora = Instant.now(clock);
        Instant inicio = reserva.getInicio();
        Integer duracion = reserva.getDuracionMinutos();

        // Regla: la cancha tiene que estar activa
        if (!Boolean.TRUE.equals(reserva.getCancha().getActiva())) {
            throw new ReglaReservaException("canchainactiva", "La cancha no está disponible para reservas");
        }

        // Regla: bloques de 60, 90 o 120 minutos
        if (!DURACIONES_PERMITIDAS.contains(duracion)) {
            throw new ReglaReservaException("duracioninvalida", "La duración debe ser de 60, 90 o 120 minutos");
        }

        // Regla: ni en el pasado ni con más de 14 días de anticipación
        if (!inicio.isAfter(ahora)) {
            throw new ReglaReservaException("enelpasado", "No se puede reservar en el pasado");
        }
        if (inicio.isAfter(ahora.plus(Duration.ofDays(MAX_DIAS_ANTICIPACION)))) {
            throw new ReglaReservaException("muyanticipada", "Solo se puede reservar con hasta 14 días de anticipación");
        }

        // Regla: dentro del horario del club, empezando en punto o y media
        ZonedDateTime inicioLocal = inicio.atZone(ZONA_CLUB);
        ZonedDateTime finLocal = inicioLocal.plusMinutes(duracion);
        boolean empiezaEnHorario = !inicioLocal.toLocalTime().isBefore(APERTURA);
        boolean terminaEnHorario = finLocal.toLocalDate().equals(inicioLocal.toLocalDate()) && !finLocal.toLocalTime().isAfter(CIERRE);
        boolean horaRedonda = inicioLocal.getSecond() == 0 && inicioLocal.getNano() == 0 && inicioLocal.getMinute() % 30 == 0;
        if (!empiezaEnHorario || !terminaEnHorario || !horaRedonda) {
            throw new ReglaReservaException("fueradehorario", "El club abre de 08:00 a 23:00 y las reservas empiezan en punto o y media");
        }

        // Regla: máximo de reservas activas por socio
        long activas = reservaRepository.countByUsuarioLoginAndEstadoAndInicioAfter(
            reserva.getUsuario().getLogin(),
            EstadoReserva.CONFIRMADA,
            ahora
        );
        if (activas >= MAX_RESERVAS_ACTIVAS) {
            throw new ReglaReservaException("limitereservas", "Ya tenés el máximo de 3 reservas activas");
        }

        // Regla: no se puede superponer con otra reserva confirmada de la misma cancha
        Instant fin = inicio.plus(Duration.ofMinutes(duracion));
        boolean haySuperposicion = reservaRepository
            .findByCanchaIdAndEstadoAndInicioBetween(
                reserva.getCancha().getId(),
                EstadoReserva.CONFIRMADA,
                inicio.minus(DURACION_MAXIMA),
                fin
            )
            .stream()
            .anyMatch(otra -> seSuperponen(inicio, fin, otra));
        if (haySuperposicion) {
            throw new ReglaReservaException("superposicion", "La cancha ya está reservada en ese horario");
        }
    }

    /**
     * Dos intervalos [inicio, fin) se superponen si cada uno empieza antes de que termine el otro.
     * Una reserva que termina justo cuando empieza otra NO se superpone.
     */
    static boolean seSuperponen(Instant inicio, Instant fin, Reserva otra) {
        Instant otraInicio = otra.getInicio();
        Instant otraFin = otraInicio.plus(Duration.ofMinutes(otra.getDuracionMinutos()));
        return inicio.isBefore(otraFin) && otraInicio.isBefore(fin);
    }

    /**
     * Cancela una reserva. El dueño puede cancelar con al menos 2 horas de anticipación;
     * un administrador puede cancelar cualquier reserva en cualquier momento.
     *
     * @param id el id de la reserva.
     * @return la reserva cancelada, o vacío si no existe o el usuario no puede verla.
     * @throws ReglaReservaException si no se puede cancelar.
     */
    public Optional<ReservaDTO> cancelar(Long id) {
        LOG.debug("Request to cancel Reserva : {}", id);
        boolean esAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        return reservaRepository
            .findOneWithEagerRelationships(id)
            .filter(reserva -> esAdmin || esDelUsuarioActual(reserva))
            .map(reserva -> {
                if (reserva.getEstado() != EstadoReserva.CONFIRMADA) {
                    throw new ReglaReservaException("nocancelable", "Solo se pueden cancelar reservas confirmadas");
                }
                Instant limite = reserva.getInicio().minus(ANTICIPACION_MINIMA_CANCELACION);
                if (!esAdmin && Instant.now(clock).isAfter(limite)) {
                    throw new ReglaReservaException("cancelaciontardia", "Las reservas se cancelan con al menos 2 horas de anticipación");
                }
                reserva.setEstado(EstadoReserva.CANCELADA);
                Reserva cancelada = reservaRepository.save(reserva);
                LOG.info(
                    "Reserva cancelada {} {} {} {}",
                    kv("evento", "RESERVA_CANCELADA"),
                    kv("reserva_id", cancelada.getId()),
                    kv("cancha", cancelada.getCancha().getNombre()),
                    kv("por", esAdmin ? "admin" : "socio")
                );
                return reservaMapper.toDto(cancelada);
            });
    }

    private static boolean esDelUsuarioActual(Reserva reserva) {
        Optional<String> login = SecurityUtils.getCurrentUserLogin();
        return reserva.getUsuario() != null && login.isPresent() && login.orElseThrow().equals(reserva.getUsuario().getLogin());
    }

    /**
     * Update a reserva (solo administradores; no aplica las reglas de creación).
     *
     * @param reservaDTO the entity to save.
     * @return the persisted entity.
     */
    public ReservaDTO update(ReservaDTO reservaDTO) {
        LOG.debug("Request to update Reserva : {}", reservaDTO);
        Reserva reserva = reservaMapper.toEntity(reservaDTO);
        reserva = reservaRepository.save(reserva);
        return reservaMapper.toDto(reserva);
    }

    /**
     * Partially update a reserva (solo administradores).
     *
     * @param reservaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<ReservaDTO> partialUpdate(ReservaDTO reservaDTO) {
        LOG.debug("Request to partially update Reserva : {}", reservaDTO);

        return reservaRepository
            .findById(reservaDTO.getId())
            .map(existingReserva -> {
                reservaMapper.partialUpdate(existingReserva, reservaDTO);

                return existingReserva;
            })
            .map(reservaRepository::save)
            .map(reservaMapper::toDto);
    }

    /**
     * Get all the reservas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<ReservaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return reservaRepository.findAllWithEagerRelationships(pageable).map(reservaMapper::toDto);
    }

    /**
     * Get one reserva by id. Un socio solo puede ver sus propias reservas; el admin ve todas.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<ReservaDTO> findOne(Long id) {
        LOG.debug("Request to get Reserva : {}", id);
        boolean esAdmin = SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN);
        return reservaRepository
            .findOneWithEagerRelationships(id)
            .filter(reserva -> esAdmin || esDelUsuarioActual(reserva))
            .map(reservaMapper::toDto);
    }

    /**
     * Delete the reserva by id (solo administradores).
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Reserva : {}", id);
        reservaRepository.deleteById(id);
    }
}
