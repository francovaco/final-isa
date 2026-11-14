package ar.edu.um.isa.clubreservas.web.rest;

import static ar.edu.um.isa.clubreservas.domain.CanchaAsserts.*;
import static ar.edu.um.isa.clubreservas.web.rest.TestUtil.createUpdateProxyForBean;
import static ar.edu.um.isa.clubreservas.web.rest.TestUtil.sameNumber;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.isa.clubreservas.IntegrationTest;
import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.domain.enumeration.Superficie;
import ar.edu.um.isa.clubreservas.repository.CanchaRepository;
import ar.edu.um.isa.clubreservas.service.CanchaService;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.CanchaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

/**
 * Integration tests for the {@link CanchaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser
class CanchaResourceIT {

    private static final String DEFAULT_NOMBRE = "AAAAAAAAAA";
    private static final String UPDATED_NOMBRE = "BBBBBBBBBB";

    private static final Superficie DEFAULT_SUPERFICIE = Superficie.CESPED_SINTETICO;
    private static final Superficie UPDATED_SUPERFICIE = Superficie.POLVO_DE_LADRILLO;

    private static final Boolean DEFAULT_TECHADA = false;
    private static final Boolean UPDATED_TECHADA = true;

    private static final BigDecimal DEFAULT_PRECIO_POR_HORA = new BigDecimal(0);
    private static final BigDecimal UPDATED_PRECIO_POR_HORA = new BigDecimal(1);

    private static final Boolean DEFAULT_ACTIVA = false;
    private static final Boolean UPDATED_ACTIVA = true;

    private static final String ENTITY_API_URL = "/api/canchas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private CanchaRepository canchaRepository;

    @Mock
    private CanchaRepository canchaRepositoryMock;

    @Autowired
    private CanchaMapper canchaMapper;

    @Mock
    private CanchaService canchaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restCanchaMockMvc;

    private Cancha cancha;

    private Cancha insertedCancha;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cancha createEntity(EntityManager em) {
        Cancha cancha = new Cancha()
            .nombre(DEFAULT_NOMBRE)
            .superficie(DEFAULT_SUPERFICIE)
            .techada(DEFAULT_TECHADA)
            .precioPorHora(DEFAULT_PRECIO_POR_HORA)
            .activa(DEFAULT_ACTIVA);
        // Add required entity
        Deporte deporte;
        if (TestUtil.findAll(em, Deporte.class).isEmpty()) {
            deporte = DeporteResourceIT.createEntity();
            em.persist(deporte);
            em.flush();
        } else {
            deporte = TestUtil.findAll(em, Deporte.class).get(0);
        }
        cancha.setDeporte(deporte);
        return cancha;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Cancha createUpdatedEntity(EntityManager em) {
        Cancha updatedCancha = new Cancha()
            .nombre(UPDATED_NOMBRE)
            .superficie(UPDATED_SUPERFICIE)
            .techada(UPDATED_TECHADA)
            .precioPorHora(UPDATED_PRECIO_POR_HORA)
            .activa(UPDATED_ACTIVA);
        // Add required entity
        Deporte deporte;
        if (TestUtil.findAll(em, Deporte.class).isEmpty()) {
            deporte = DeporteResourceIT.createUpdatedEntity();
            em.persist(deporte);
            em.flush();
        } else {
            deporte = TestUtil.findAll(em, Deporte.class).get(0);
        }
        updatedCancha.setDeporte(deporte);
        return updatedCancha;
    }

    @BeforeEach
    void initTest() {
        cancha = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedCancha != null) {
            canchaRepository.delete(insertedCancha);
            insertedCancha = null;
        }
    }

    @Test
    @Transactional
    void createCancha() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);
        var returnedCanchaDTO = om.readValue(
            restCanchaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            CanchaDTO.class
        );

        // Validate the Cancha in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedCancha = canchaMapper.toEntity(returnedCanchaDTO);
        assertCanchaUpdatableFieldsEquals(returnedCancha, getPersistedCancha(returnedCancha));

        insertedCancha = returnedCancha;
    }

    @Test
    @Transactional
    void createCanchaWithExistingId() throws Exception {
        // Create the Cancha with an existing ID
        cancha.setId(1L);
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkNombreIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cancha.setNombre(null);

        // Create the Cancha, which fails.
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkSuperficieIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cancha.setSuperficie(null);

        // Create the Cancha, which fails.
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkTechadaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cancha.setTechada(null);

        // Create the Cancha, which fails.
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkPrecioPorHoraIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cancha.setPrecioPorHora(null);

        // Create the Cancha, which fails.
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkActivaIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        cancha.setActiva(null);

        // Create the Cancha, which fails.
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        restCanchaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllCanchas() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        // Get all the canchaList
        restCanchaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(cancha.getId().intValue())))
            .andExpect(jsonPath("$.[*].nombre").value(hasItem(DEFAULT_NOMBRE)))
            .andExpect(jsonPath("$.[*].superficie").value(hasItem(DEFAULT_SUPERFICIE.toString())))
            .andExpect(jsonPath("$.[*].techada").value(hasItem(DEFAULT_TECHADA)))
            .andExpect(jsonPath("$.[*].precioPorHora").value(hasItem(sameNumber(DEFAULT_PRECIO_POR_HORA))))
            .andExpect(jsonPath("$.[*].activa").value(hasItem(DEFAULT_ACTIVA)));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCanchasWithEagerRelationshipsIsEnabled() throws Exception {
        when(canchaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCanchaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(canchaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllCanchasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(canchaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restCanchaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(canchaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getCancha() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        // Get the cancha
        restCanchaMockMvc
            .perform(get(ENTITY_API_URL_ID, cancha.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(cancha.getId().intValue()))
            .andExpect(jsonPath("$.nombre").value(DEFAULT_NOMBRE))
            .andExpect(jsonPath("$.superficie").value(DEFAULT_SUPERFICIE.toString()))
            .andExpect(jsonPath("$.techada").value(DEFAULT_TECHADA))
            .andExpect(jsonPath("$.precioPorHora").value(sameNumber(DEFAULT_PRECIO_POR_HORA)))
            .andExpect(jsonPath("$.activa").value(DEFAULT_ACTIVA));
    }

    @Test
    @Transactional
    void getNonExistingCancha() throws Exception {
        // Get the cancha
        restCanchaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingCancha() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cancha
        Cancha updatedCancha = canchaRepository.findById(cancha.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedCancha are not directly saved in db
        em.detach(updatedCancha);
        updatedCancha
            .nombre(UPDATED_NOMBRE)
            .superficie(UPDATED_SUPERFICIE)
            .techada(UPDATED_TECHADA)
            .precioPorHora(UPDATED_PRECIO_POR_HORA)
            .activa(UPDATED_ACTIVA);
        CanchaDTO canchaDTO = canchaMapper.toDto(updatedCancha);

        restCanchaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, canchaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedCanchaToMatchAllProperties(updatedCancha);
    }

    @Test
    @Transactional
    void putNonExistingCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, canchaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(canchaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateCanchaWithPatch() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cancha using partial update
        Cancha partialUpdatedCancha = new Cancha();
        partialUpdatedCancha.setId(cancha.getId());

        partialUpdatedCancha
            .nombre(UPDATED_NOMBRE)
            .superficie(UPDATED_SUPERFICIE)
            .techada(UPDATED_TECHADA)
            .precioPorHora(UPDATED_PRECIO_POR_HORA);

        restCanchaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCancha.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCancha))
            )
            .andExpect(status().isOk());

        // Validate the Cancha in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCanchaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedCancha, cancha), getPersistedCancha(cancha));
    }

    @Test
    @Transactional
    void fullUpdateCanchaWithPatch() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the cancha using partial update
        Cancha partialUpdatedCancha = new Cancha();
        partialUpdatedCancha.setId(cancha.getId());

        partialUpdatedCancha
            .nombre(UPDATED_NOMBRE)
            .superficie(UPDATED_SUPERFICIE)
            .techada(UPDATED_TECHADA)
            .precioPorHora(UPDATED_PRECIO_POR_HORA)
            .activa(UPDATED_ACTIVA);

        restCanchaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedCancha.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedCancha))
            )
            .andExpect(status().isOk());

        // Validate the Cancha in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertCanchaUpdatableFieldsEquals(partialUpdatedCancha, getPersistedCancha(partialUpdatedCancha));
    }

    @Test
    @Transactional
    void patchNonExistingCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, canchaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(canchaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(canchaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamCancha() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        cancha.setId(longCount.incrementAndGet());

        // Create the Cancha
        CanchaDTO canchaDTO = canchaMapper.toDto(cancha);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restCanchaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(canchaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Cancha in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteCancha() throws Exception {
        // Initialize the database
        insertedCancha = canchaRepository.saveAndFlush(cancha);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the cancha
        restCanchaMockMvc
            .perform(delete(ENTITY_API_URL_ID, cancha.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return canchaRepository.count();
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

    protected Cancha getPersistedCancha(Cancha cancha) {
        return canchaRepository.findById(cancha.getId()).orElseThrow();
    }

    protected void assertPersistedCanchaToMatchAllProperties(Cancha expectedCancha) {
        assertCanchaAllPropertiesEquals(expectedCancha, getPersistedCancha(expectedCancha));
    }

    protected void assertPersistedCanchaToMatchUpdatableProperties(Cancha expectedCancha) {
        assertCanchaAllUpdatablePropertiesEquals(expectedCancha, getPersistedCancha(expectedCancha));
    }
}
