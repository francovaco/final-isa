package ar.edu.um.isa.clubreservas.web.rest;

import ar.edu.um.isa.clubreservas.repository.CanchaRepository;
import ar.edu.um.isa.clubreservas.service.CanchaService;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
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
 * REST controller for managing {@link ar.edu.um.isa.clubreservas.domain.Cancha}.
 */
@RestController
@RequestMapping("/api/canchas")
public class CanchaResource {

    private static final Logger LOG = LoggerFactory.getLogger(CanchaResource.class);

    private static final String ENTITY_NAME = "cancha";

    @Value("${jhipster.clientApp.name}")
    private String applicationName;

    private final CanchaService canchaService;

    private final CanchaRepository canchaRepository;

    public CanchaResource(CanchaService canchaService, CanchaRepository canchaRepository) {
        this.canchaService = canchaService;
        this.canchaRepository = canchaRepository;
    }

    /**
     * {@code POST  /canchas} : Create a new cancha.
     *
     * @param canchaDTO the canchaDTO to create.
     * @return the {@link ResponseEntity} with status {@code 201 (Created)} and with body the new canchaDTO, or with status {@code 400 (Bad Request)} if the cancha has already an ID.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PostMapping("")
    public ResponseEntity<CanchaDTO> createCancha(@Valid @RequestBody CanchaDTO canchaDTO) throws URISyntaxException {
        LOG.debug("REST request to save Cancha : {}", canchaDTO);
        if (canchaDTO.getId() != null) {
            throw new BadRequestAlertException("A new cancha cannot already have an ID", ENTITY_NAME, "idexists");
        }
        canchaDTO = canchaService.save(canchaDTO);
        return ResponseEntity.created(new URI("/api/canchas/" + canchaDTO.getId()))
            .headers(HeaderUtil.createEntityCreationAlert(applicationName, false, ENTITY_NAME, canchaDTO.getId().toString()))
            .body(canchaDTO);
    }

    /**
     * {@code PUT  /canchas/:id} : Updates an existing cancha.
     *
     * @param id the id of the canchaDTO to save.
     * @param canchaDTO the canchaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated canchaDTO,
     * or with status {@code 400 (Bad Request)} if the canchaDTO is not valid,
     * or with status {@code 500 (Internal Server Error)} if the canchaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PutMapping("/{id}")
    public ResponseEntity<CanchaDTO> updateCancha(
        @PathVariable(value = "id", required = false) final Long id,
        @Valid @RequestBody CanchaDTO canchaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to update Cancha : {}, {}", id, canchaDTO);
        if (canchaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, canchaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!canchaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        canchaDTO = canchaService.update(canchaDTO);
        return ResponseEntity.ok()
            .headers(HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, canchaDTO.getId().toString()))
            .body(canchaDTO);
    }

    /**
     * {@code PATCH  /canchas/:id} : Partial updates given fields of an existing cancha, field will ignore if it is null
     *
     * @param id the id of the canchaDTO to save.
     * @param canchaDTO the canchaDTO to update.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the updated canchaDTO,
     * or with status {@code 400 (Bad Request)} if the canchaDTO is not valid,
     * or with status {@code 404 (Not Found)} if the canchaDTO is not found,
     * or with status {@code 500 (Internal Server Error)} if the canchaDTO couldn't be updated.
     * @throws URISyntaxException if the Location URI syntax is incorrect.
     */
    @PatchMapping(value = "/{id}", consumes = { "application/json", "application/merge-patch+json" })
    public ResponseEntity<CanchaDTO> partialUpdateCancha(
        @PathVariable(value = "id", required = false) final Long id,
        @NotNull @RequestBody CanchaDTO canchaDTO
    ) throws URISyntaxException {
        LOG.debug("REST request to partial update Cancha partially : {}, {}", id, canchaDTO);
        if (canchaDTO.getId() == null) {
            throw new BadRequestAlertException("Invalid id", ENTITY_NAME, "idnull");
        }
        if (!Objects.equals(id, canchaDTO.getId())) {
            throw new BadRequestAlertException("Invalid ID", ENTITY_NAME, "idinvalid");
        }

        if (!canchaRepository.existsById(id)) {
            throw new BadRequestAlertException("Entity not found", ENTITY_NAME, "idnotfound");
        }

        Optional<CanchaDTO> result = canchaService.partialUpdate(canchaDTO);

        return ResponseUtil.wrapOrNotFound(
            result,
            HeaderUtil.createEntityUpdateAlert(applicationName, false, ENTITY_NAME, canchaDTO.getId().toString())
        );
    }

    /**
     * {@code GET  /canchas} : get all the canchas.
     *
     * @param eagerload flag to eager load entities from relationships (This is applicable for many-to-many).
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and the list of canchas in body.
     */
    @GetMapping("")
    public List<CanchaDTO> getAllCanchas(@RequestParam(name = "eagerload", required = false, defaultValue = "true") boolean eagerload) {
        LOG.debug("REST request to get all Canchas");
        return canchaService.findAll();
    }

    /**
     * {@code GET  /canchas/:id} : get the "id" cancha.
     *
     * @param id the id of the canchaDTO to retrieve.
     * @return the {@link ResponseEntity} with status {@code 200 (OK)} and with body the canchaDTO, or with status {@code 404 (Not Found)}.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CanchaDTO> getCancha(@PathVariable("id") Long id) {
        LOG.debug("REST request to get Cancha : {}", id);
        Optional<CanchaDTO> canchaDTO = canchaService.findOne(id);
        return ResponseUtil.wrapOrNotFound(canchaDTO);
    }

    /**
     * {@code DELETE  /canchas/:id} : delete the "id" cancha.
     *
     * @param id the id of the canchaDTO to delete.
     * @return the {@link ResponseEntity} with status {@code 204 (NO_CONTENT)}.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCancha(@PathVariable("id") Long id) {
        LOG.debug("REST request to delete Cancha : {}", id);
        canchaService.delete(id);
        return ResponseEntity.noContent()
            .headers(HeaderUtil.createEntityDeletionAlert(applicationName, false, ENTITY_NAME, id.toString()))
            .build();
    }
}
