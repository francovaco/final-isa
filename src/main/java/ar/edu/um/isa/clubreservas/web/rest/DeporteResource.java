package ar.edu.um.isa.clubreservas.web.rest;

import ar.edu.um.isa.clubreservas.repository.DeporteRepository;
import ar.edu.um.isa.clubreservas.service.DeporteService;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
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
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tech.jhipster.web.util.HeaderUtil;
import tech.jhipster.web.util.ResponseUtil;

/**
 * REST controller for managing {@link ar.edu.um.isa.clubreservas.domain.Deporte}.
 */
@RestController
@RequestMapping("/api/deportes")
public class DeporteResource {

    private static final Logger LOG = LoggerFactory.getLogger(DeporteResource.class);

    private static final String ENTITY_NAME = "deporte";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final DeporteService deporteService;

    private final DeporteRepository deporteRepository;

    public DeporteResource(DeporteService deporteService, DeporteRepository deporteRepository) {
        this.deporteService = deporteService;
        this.deporteRepository = deporteRepository;
    }

    /**
     * {@code POST  /deportes} : Create a new deporte.
     *
     * @param deporteDTO the deporteDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new deporteDTO, or with status {@code 400 (Bad Request)} if the deporte has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<DeporteDTO> createDeporte(@Valid @RequestBody DeporteDTO deporteDTO) throws URISyntaxException {
        LOG.debug("REST request to save Deporte : {}", deporteDTO);
        if (deporteDTO.getId() != null) {
            throw new BadRequestAlertException("A new deporte cannot already have an ID", ENTITY_NAME, "idexists");
        }
        deporteDTO = deporteService.save(deporteDTO);
        return ResponseEntity.created(new URI("/api/deportes/" + deporteDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, deporteDTO.getId().toString()))
            .body(deporteDTO);
    }

    /**
     * {@code PUT  /deportes/:id} : Updates an existing deporte.
     *
     * @param id the id of the deporteDTO to save.
     * @param deporteDTO the deporteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deporteDTO,
     * or with status {@code 400 (Bad Request)} if the deporteDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the deporteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<DeporteDTO> updateDeporte(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody DeporteDTO deporteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Deporte : {}, {}", id, deporteDTO);
        if (deporteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deporteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!deporteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        deporteDTO = deporteService.update(deporteDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, deporteDTO.getId().toString()))
            .body(deporteDTO);
    }

    /**
     * {@code PATCH  /deportes/:id} : Partial updates given fields of an existing deporte, field will ignore if it is null
     *
     * @param id the id of the deporteDTO to save.
     * @param deporteDTO the deporteDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated deporteDTO,
     * or with status {@code 400 (Bad Request)} if the deporteDTO is not valid,
     * or with status {@code 404 (Not Found)} if the deporteDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the deporteDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<DeporteDTO> partialUpdateDeporte(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody DeporteDTO deporteDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Deporte partially : {}, {}", id, deporteDTO);
        if (deporteDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, deporteDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!deporteRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<DeporteDTO> result = deporteService.partialUpdate(deporteDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, deporteDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /deportes} : get all the deportes.
     *
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of deportes in body.
     */
    @GetMapping("")
    public List<DeporteDTO> getAllDeportes() {
        LOG.debug("REST request to get all Deportes");
        return deporteService.findAll();
    }

    /**
     * {@code GET  /deportes/:id} : get the "id" deporte.
     *
     * @param id the id of the deporteDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the deporteDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<DeporteDTO> getDeporte(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Deporte : {}", id);
        Optional<DeporteDTO> deporteDTO = deporteService.findOne(id);
        return ResponseUtil.wrapOrNotFound(deporteDTO);
    }

    /**
     * {@code DELETE  /deportes/:id} : delete the "id" deporte.
     *
     * @param id the id of the deporteDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDeporte(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Deporte : {}", id);
        deporteService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
