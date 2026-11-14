package ar.edu.um.isa.clubreservas.web.rest;

import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.repository.UserRepository;
import ar.edu.um.isa.clubreservas.security.AuthoritiesConstants;
import ar.edu.um.isa.clubreservas.security.SecurityUtils;
import ar.edu.um.isa.clubreservas.service.ReglaReservaException;
import ar.edu.um.isa.clubreservas.service.ReservaQueryService;
import ar.edu.um.isa.clubreservas.service.ReservaService;
import ar.edu.um.isa.clubreservas.service.criteria.ReservaCriteria;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.web.rest.errors.BadRequestAlertException;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import tech.jhipster.service.filter.LongFilter;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.PaginationUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.isa.clubreservas.domain.Reserva}.
 */
@RestController
@RequestMapping("/api/reservas")
public class ReservaResource {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaResource.class);

    private static final String ENTITY_NAME = "reserva";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final ReservaService reservaService;

    private final ReservaRepository reservaRepository;

    private final ReservaQueryService reservaQueryService;

    private final UserRepository userRepository;

    public ReservaResource(
        ReservaService reservaService,
        ReservaRepository reservaRepository,
        ReservaQueryService reservaQueryService,
        UserRepository userRepository
    ) {
        this.reservaService = reservaService;
        this.reservaRepository = reservaRepository;
        this.reservaQueryService = reservaQueryService;
        this.userRepository = userRepository;
    }

    /**
     * {@code POST  /reservas} : Create a new reserva.
     *
     * @param reservaDTO the reservaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new reservaDTO, or with status {@code 400 (Bad Request)} if the reserva has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<ReservaDTO> createReserva(@Valid @RequestBody ReservaDTO reservaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Reserva : {}", reservaDTO);
        if (reservaDTO.getId() != null) {
            throw new BadRequestAlertException("A new reserva cannot already have an ID", ENTITY_NAME, "idexists");
        }
        try {
            reservaDTO = reservaService.save(reservaDTO);
        } catch (ReglaReservaException e) {
            throw errorDeRegla(e);
        }
        return ResponseEntity.created(new URI("/api/reservas/" + reservaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, reservaDTO.getId().toString()))
            .body(reservaDTO);
    }

    /**
     * {@code PUT  /reservas/:id} : Updates an existing reserva.
     *
     * @param id the id of the reservaDTO to save.
     * @param reservaDTO the reservaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservaDTO,
     * or with status {@code 400 (Bad Request)} if the reservaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the reservaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ReservaDTO> updateReserva(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody ReservaDTO reservaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Reserva : {}, {}", id, reservaDTO);
        if (reservaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        reservaDTO = reservaService.update(reservaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reservaDTO.getId().toString()))
            .body(reservaDTO);
    }

    /**
     * {@code PATCH  /reservas/:id} : Partial updates given fields of an existing reserva, field will ignore if it is null
     *
     * @param id the id of the reservaDTO to save.
     * @param reservaDTO the reservaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated reservaDTO,
     * or with status {@code 400 (Bad Request)} if the reservaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the reservaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the reservaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<ReservaDTO> partialUpdateReserva(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody ReservaDTO reservaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Reserva partially : {}, {}", id, reservaDTO);
        if (reservaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, reservaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!reservaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<ReservaDTO> result = reservaService.partialUpdate(reservaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, reservaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /reservas} : get all the reservas.
     *
     * @param pageable the pagination information.
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of reservas in body.
     */
    @GetMapping("")
    public ResponseEntity<List<ReservaDTO>> getAllReservas(
        ReservaCriteria criteria,
        @org.springdoc.core.annotations.ParameterObject Pageable pageable
    ) {
        LOG.debug("REST request to get Reservas by criteria: {}", criteria);
        restringirAlUsuarioActual(criteria);

        Page<ReservaDTO> page = reservaQueryService.findByCriteria(criteria, pageable);
        HttpHeaders headers = PaginationUtil.generatePaginationHttpHeaders(ServletUriComponentsBuilder.fromCurrentRequest(), page);
        return ResponseEntity.ok().headers(headers).body(page.getContent());
    }

    /**
     * {@code GET  /reservas/count} : count all the reservas.
     *
     * @param criteria the criteria which the requested entities should match.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the count in body.
     */
    @GetMapping("/count")
    public ResponseEntity<Long> countReservas(ReservaCriteria criteria) {
        LOG.debug("REST request to count Reservas by criteria: {}", criteria);
        restringirAlUsuarioActual(criteria);
        return ResponseEntity.ok().body(reservaQueryService.countByCriteria(criteria));
    }

    /**
     * {@code GET  /reservas/:id} : get the "id" reserva.
     *
     * @param id the id of the reservaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the reservaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ReservaDTO> getReserva(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Reserva : {}", id);
        Optional<ReservaDTO> reservaDTO = reservaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(reservaDTO);
    }

    /**
     * {@code POST  /reservas/:id/cancelar} : cancela la reserva "id".
     * El socio puede cancelar sus reservas con 2 horas de anticipación; el admin, cualquiera.
     *
     * @param id el id de la reserva a cancelar.
     * @return {@code 200 (OK)} con la reserva cancelada, {@code 400 (Bad Request)} si no se puede cancelar,
     * o {@code 404 (Not Found)} si no existe o no es del usuario.
     */
    @PostMapping("/{id}/cancelar")
    public ResponseEntity<ReservaDTO> cancelarReserva(@PathVariable("id") Long id) {
        LOG.debug("REST request to cancel Reserva : {}", id);
        try {
            return ResponseUtil.wrapOrNotFound(
                reservaService.cancelar(id),
                HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, id.toString())
            );
        } catch (ReglaReservaException e) {
            throw errorDeRegla(e);
        }
    }

    /**
     * Convierte una regla de negocio incumplida en un 400. El motivo (texto para el usuario)
     * va en "detail" y el código de la regla en "message" (error.&lt;codigo&gt;).
     */
    private static BadRequestAlertException errorDeRegla(ReglaReservaException e) {
        BadRequestAlertException error = new BadRequestAlertException(e.getMessage(), ENTITY_NAME, e.getCodigo());
        error.getBody().setDetail(e.getMessage());
        return error;
    }

    /**
     * Un socio solo puede listar sus propias reservas: se fuerza el filtro por su usuario.
     * El administrador ve todas.
     */
    private void restringirAlUsuarioActual(ReservaCriteria criteria) {
        if (SecurityUtils.hasCurrentUserThisAuthority(AuthoritiesConstants.ADMIN)) {
            return;
        }
        Long usuarioId = SecurityUtils.getCurrentUserLogin().flatMap(userRepository::findOneByLogin).map(User::getId).orElse(-1L);
        LongFilter filtro = new LongFilter();
        filtro.setEquals(usuarioId);
        criteria.setUsuarioId(filtro);
    }

    /**
     * {@code DELETE  /reservas/:id} : delete the "id" reserva.
     *
     * @param id the id of the reservaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority(\"" + AuthoritiesConstants.ADMIN + "\")")
    public ResponseEntity<Void> deleteReserva(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Reserva : {}", id);
        reservaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
