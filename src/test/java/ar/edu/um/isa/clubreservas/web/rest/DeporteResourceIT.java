package ar.edu.um.isa.clubreservas.web.rest;

import static ar.edu.um.isa.clubreservas.domain.DeporteAsserts.*;
import static ar.edu.um.isa.clubreservas.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.isa.clubreservas.IntegrationTest;
import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.repository.DeporteRepository;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
import ar.edu.um.isa.clubreservas.service.mapper.DeporteMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link DeporteResource} REST controller.
 */
@IntegrationTest
@AutoConfigureMockMvc
@WithMockUser
class DeporteResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final String DEFAULT_DESCRIPCION = "AAAAAAAAAA";
    private static final String UPDATED_DESCRIPCION = "BBBBBBBBBB";

    private static final Integer DEFAULT_JUGADORES_POR_EQUIPO = 1;
    private static final Integer UPDATED_JUGADORES_POR_EQUIPO = 2;

    private static final String ENTITY_API_URL = "/api/deportes";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private DeporteRepository deporteRepository;

    @Autowired
    private DeporteMapper deporteMapper;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restDeporteMockMvc;

    private Deporte deporte;

    private Deporte insertedDeporte;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Deporte createEntity() {
        return new Deporte().nombre(DEFAULT_NOMBRE).descripcion(DEFAULT_DESCRIPCION).jugadoresPorEquipo(DEFAULT_JUGADORES_POR_EQUIPO);
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Deporte createUpdatedEntity() {
        return new Deporte().nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).jugadoresPorEquipo(UPDATED_JUGADORES_POR_EQUIPO);
    }

    @BeforeEach
    void initTest() {
        deporte = createEntity();
    }

    @AfterEach
    void cleanup() {
        if (insertedDeporte != null) {
            deporteRepository.delete(insertedDeporte);
            insertedDeporte = null;
        }
    }

    @Test
    @Transactional
    void createDeporte() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);
        var returnedDeporteDTO = om.readValue(
            restDeporteMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            DeporteDTO.class
        );

        // Validate the Deporte in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedDeporte = deporteMapper.toEntity(returnedDeporteDTO);
        assertDeporteUpdatableFieldsEquals(returnedDeporte, getPersistedDeporte(returnedDeporte));

        insertedDeporte = returnedDeporte;
    }

    @Test
    @Transactional
    void createDeporteWithExistingId() throws Exception {
        // Create the Deporte with an existing ID
        deporte.setId(1L);
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restDeporteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        deporte.setNombre(null);

        // Create the Deporte, which fails.
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        restDeporteMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllDeportes() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        // Get all the deporteList
        restDeporteMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(deporte.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].descripcion").value(hasItem(DEFAULT_DESCRIPCION)))
            .andExpect(jsonPath("$.[*].jugadoresPorEquipo").value(hasItem(DEFAULT_JUGADORES_POR_EQUIPO)));
    }

    @Test
    @Transactional
    void getDeporte() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        // Get the deporte
        restDeporteMockMvc
            .perform(get(ENTITY_API_URL_ID, deporte.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(deporte.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.descripcion").value(DEFAULT_DESCRIPCION))
            .andExpect(jsonPath("$.jugadoresPorEquipo").value(DEFAULT_JUGADORES_POR_EQUIPO));
    }

    @Test
    @Transactional
    void getNonExistingDeporte() throws Exception {
        // Get the deporte
        restDeporteMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingDeporte() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deporte
        Deporte updatedDeporte = deporteRepository.findById(deporte.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedDeporte are not directly saved in db
        em.detach(updatedDeporte);
        updatedDeporte.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).jugadoresPorEquipo(UPDATED_JUGADORES_POR_EQUIPO);
        DeporteDTO deporteDTO = deporteMapper.toDto(updatedDeporte);

        restDeporteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deporteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO))
            )
            .andExpect(status().isOk());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedDeporteToMatchAllProperties(updatedDeporte);
    }

    @Test
    @Transactional
    void putNonExistingDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, deporteDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(deporteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(deporteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateDeporteWithPatch() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deporte using partial update
        Deporte partialUpdatedDeporte = new Deporte();
        partialUpdatedDeporte.setId(deporte.getId());

        partialUpdatedDeporte.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION);

        restDeporteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeporte.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDeporte))
            )
            .andExpect(status().isOk());

        // Validate the Deporte in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeporteUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedDeporte, deporte), getPersistedDeporte(deporte));
    }

    @Test
    @Transactional
    void fullUpdateDeporteWithPatch() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the deporte using partial update
        Deporte partialUpdatedDeporte = new Deporte();
        partialUpdatedDeporte.setId(deporte.getId());

        partialUpdatedDeporte.nombre(UPDATED_NOMBRE).descripcion(UPDATED_DESCRIPCION).jugadoresPorEquipo(UPDATED_JUGADORES_POR_EQUIPO);

        restDeporteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedDeporte.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedDeporte))
            )
            .andExpect(status().isOk());

        // Validate the Deporte in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertDeporteUpdatableFieldsEquals(partialUpdatedDeporte, getPersistedDeporte(partialUpdatedDeporte));
    }

    @Test
    @Transactional
    void patchNonExistingDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, deporteDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deporteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(deporteDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamDeporte() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        deporte.setId(longCount.incrementAndGet());

        // Create the Deporte
        DeporteDTO deporteDTO = deporteMapper.toDto(deporte);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restDeporteMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(deporteDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Deporte in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteDeporte() throws Exception {
        // Initialize the database
        insertedDeporte = deporteRepository.saveAndFlush(deporte);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the deporte
        restDeporteMockMvc
            .perform(delete(ENTITY_API_URL_ID, deporte.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return deporteRepository.count();
    }

    protected void assertIncrementedRepositoryCount(long countBefore) {
        assertThat(countBefore + 1).isEqualTo(getRepositoryCount());
    }

    protected void assertDecrementedRepositoryCount(long countBefore) {
        assertThat(countBefore - 1).isEqualTo(getRepositoryCount());
    }

    protected void assertSameRepositoryCount(long countBefore) {
        assertThat(countBefore).isEqualTo(getRepositoryCount());
    }

    protected Deporte getPersistedDeporte(Deporte deporte) {
        return deporteRepository.findById(deporte.getId()).orElseThrow();
    }

    protected void assertPersistedDeporteToMatchAllProperties(Deporte expectedDeporte) {
        assertDeporteAllPropertiesEquals(expectedDeporte, getPersistedDeporte(expectedDeporte));
    }

    protected void assertPersistedDeporteToMatchUpdatableProperties(Deporte expectedDeporte) {
        assertDeporteAllUpdatablePropertiesEquals(expectedDeporte, getPersistedDeporte(expectedDeporte));
    }
}
