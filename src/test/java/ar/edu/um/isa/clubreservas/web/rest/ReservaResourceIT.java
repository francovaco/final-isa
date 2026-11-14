package ar.edu.um.isa.clubreservas.web.rest;

import static ar.edu.um.isa.clubreservas.domain.ReservaAsserts.*;
import static ar.edu.um.isa.clubreservas.web.rest.TestUtil.createUpdateProxyForBean;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasItem;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import ar.edu.um.isa.clubreservas.IntegrationTest;
import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.repository.UserRepository;
import ar.edu.um.isa.clubreservas.security.AuthoritiesConstants;
import ar.edu.um.isa.clubreservas.service.ReservaService;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.ReservaMapper;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityManager;
import java.time.Instant;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
 * Integration tests for the {@link ReservaResource} REST controller.
 */
@IntegrationTest
@ExtendWith(MockitoExtension.class)
@AutoConfigureMockMvc
@WithMockUser(authorities = AuthoritiesConstants.ADMIN)
class ReservaResourceIT {

    private static final Instant DEFAULT_INICIO = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_INICIO = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final Integer DEFAULT_DURACION_MINUTOS = 60;
    private static final Integer UPDATED_DURACION_MINUTOS = 61;
    private static final Integer SMALLER_DURACION_MINUTOS = 60 - 1;

    private static final EstadoReserva DEFAULT_ESTADO = EstadoReserva.CONFIRMADA;
    private static final EstadoReserva UPDATED_ESTADO = EstadoReserva.CANCELADA;

    private static final String DEFAULT_OBSERVACIONES = "AAAAAAAAAA";
    private static final String UPDATED_OBSERVACIONES = "BBBBBBBBBB";

    private static final Instant DEFAULT_CREADA_EN = Instant.ofEpochMilli(0L);
    private static final Instant UPDATED_CREADA_EN = Instant.now().truncatedTo(ChronoUnit.MILLIS);

    private static final String ENTITY_API_URL = "/api/reservas";
    private static final String ENTITY_API_URL_ID = ENTITY_API_URL + "/{id}";

    private static Random random = new Random();
    private static AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));

    @Autowired
    private ObjectMapper om;

    @Autowired
    private ReservaRepository reservaRepository;

    @Autowired
    private UserRepository userRepository;

    @Mock
    private ReservaRepository reservaRepositoryMock;

    @Autowired
    private ReservaMapper reservaMapper;

    @Mock
    private ReservaService reservaServiceMock;

    @Autowired
    private EntityManager em;

    @Autowired
    private MockMvc restReservaMockMvc;

    private Reserva reserva;

    private Reserva insertedReserva;

    /**
     * Create an entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reserva createEntity(EntityManager em) {
        Reserva reserva = new Reserva()
            .inicio(DEFAULT_INICIO)
            .duracionMinutos(DEFAULT_DURACION_MINUTOS)
            .estado(DEFAULT_ESTADO)
            .observaciones(DEFAULT_OBSERVACIONES)
            .creadaEn(DEFAULT_CREADA_EN);
        // Add required entity
        Cancha cancha;
        if (TestUtil.findAll(em, Cancha.class).isEmpty()) {
            cancha = CanchaResourceIT.createEntity(em);
            em.persist(cancha);
            em.flush();
        } else {
            cancha = TestUtil.findAll(em, Cancha.class).get(0);
        }
        reserva.setCancha(cancha);
        return reserva;
    }

    /**
     * Create an updated entity for this test.
     *
     * This is a static method, as tests for other entities might also need it,
     * if they test an entity which requires the current entity.
     */
    public static Reserva createUpdatedEntity(EntityManager em) {
        Reserva updatedReserva = new Reserva()
            .inicio(UPDATED_INICIO)
            .duracionMinutos(UPDATED_DURACION_MINUTOS)
            .estado(UPDATED_ESTADO)
            .observaciones(UPDATED_OBSERVACIONES)
            .creadaEn(UPDATED_CREADA_EN);
        // Add required entity
        Cancha cancha;
        if (TestUtil.findAll(em, Cancha.class).isEmpty()) {
            cancha = CanchaResourceIT.createUpdatedEntity(em);
            em.persist(cancha);
            em.flush();
        } else {
            cancha = TestUtil.findAll(em, Cancha.class).get(0);
        }
        updatedReserva.setCancha(cancha);
        return updatedReserva;
    }

    /** Mañana a las 10:00, hora del club: un turno que cumple todas las reglas de negocio. */
    static Instant proximoTurnoValido() {
        return LocalDate.now(ReservaService.ZONA_CLUB).plusDays(1).atTime(10, 0).atZone(ReservaService.ZONA_CLUB).toInstant();
    }

    @BeforeEach
    void initTest() {
        reserva = createEntity(em);
    }

    @AfterEach
    void cleanup() {
        if (insertedReserva != null) {
            reservaRepository.delete(insertedReserva);
            insertedReserva = null;
        }
    }

    @Test
    @Transactional
    void createReserva() throws Exception {
        long databaseSizeBeforeCreate = getRepositoryCount();
        // Reglas de negocio: horario valido (manana a las 10:00 hora del club) y cancha activa
        reserva.setInicio(proximoTurnoValido());
        reserva.getCancha().setActiva(true);
        em.flush();
        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);
        var returnedReservaDTO = om.readValue(
            restReservaMockMvc
                .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString(),
            ReservaDTO.class
        );

        // Validate the Reserva in the database
        assertIncrementedRepositoryCount(databaseSizeBeforeCreate);
        var returnedReserva = reservaMapper.toEntity(returnedReservaDTO);
        assertReservaUpdatableFieldsEquals(returnedReserva, getPersistedReserva(returnedReserva));

        insertedReserva = returnedReserva;
    }

    @Test
    @Transactional
    void createReservaWithExistingId() throws Exception {
        // Create the Reserva with an existing ID
        reserva.setId(1L);
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        long databaseSizeBeforeCreate = getRepositoryCount();

        // An entity with an existing ID cannot be created, so this API call must fail
        restReservaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isBadRequest());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeCreate);
    }

    @Test
    @Transactional
    void checkInicioIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reserva.setInicio(null);

        // Create the Reserva, which fails.
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        restReservaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkDuracionMinutosIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reserva.setDuracionMinutos(null);

        // Create the Reserva, which fails.
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        restReservaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void checkEstadoIsRequired() throws Exception {
        long databaseSizeBeforeTest = getRepositoryCount();
        // set the field null
        reserva.setEstado(null);

        // Create the Reserva, which fails.
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        restReservaMockMvc
            .perform(post(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isBadRequest());

        assertSameRepositoryCount(databaseSizeBeforeTest);
    }

    @Test
    @Transactional
    void getAllReservas() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList
        restReservaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc"))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reserva.getId().intValue())))
            .andExpect(jsonPath("$.[*].inicio").value(hasItem(DEFAULT_INICIO.toString())))
            .andExpect(jsonPath("$.[*].duracionMinutos").value(hasItem(DEFAULT_DURACION_MINUTOS)))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)))
            .andExpect(jsonPath("$.[*].creadaEn").value(hasItem(DEFAULT_CREADA_EN.toString())));
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservasWithEagerRelationshipsIsEnabled() throws Exception {
        when(reservaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReservaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=true")).andExpect(status().isOk());

        verify(reservaServiceMock, times(1)).findAllWithEagerRelationships(any());
    }

    @SuppressWarnings({ "unchecked" })
    void getAllReservasWithEagerRelationshipsIsNotEnabled() throws Exception {
        when(reservaServiceMock.findAllWithEagerRelationships(any())).thenReturn(new PageImpl(new ArrayList<>()));

        restReservaMockMvc.perform(get(ENTITY_API_URL + "?eagerload=false")).andExpect(status().isOk());
        verify(reservaRepositoryMock, times(1)).findAll(any(Pageable.class));
    }

    @Test
    @Transactional
    void getReserva() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get the reserva
        restReservaMockMvc
            .perform(get(ENTITY_API_URL_ID, reserva.getId()))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.id").value(reserva.getId().intValue()))
            .andExpect(jsonPath("$.inicio").value(DEFAULT_INICIO.toString()))
            .andExpect(jsonPath("$.duracionMinutos").value(DEFAULT_DURACION_MINUTOS))
            .andExpect(jsonPath("$.estado").value(DEFAULT_ESTADO.toString()))
            .andExpect(jsonPath("$.observaciones").value(DEFAULT_OBSERVACIONES))
            .andExpect(jsonPath("$.creadaEn").value(DEFAULT_CREADA_EN.toString()));
    }

    @Test
    @Transactional
    void getReservasByIdFiltering() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        Long id = reserva.getId();

        defaultReservaFiltering("id.equals=" + id, "id.notEquals=" + id);

        defaultReservaFiltering("id.greaterThanOrEqual=" + id, "id.greaterThan=" + id);

        defaultReservaFiltering("id.lessThanOrEqual=" + id, "id.lessThan=" + id);
    }

    @Test
    @Transactional
    void getAllReservasByInicioIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where inicio equals to
        defaultReservaFiltering("inicio.equals=" + DEFAULT_INICIO, "inicio.equals=" + UPDATED_INICIO);
    }

    @Test
    @Transactional
    void getAllReservasByInicioIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where inicio in
        defaultReservaFiltering("inicio.in=" + DEFAULT_INICIO + "," + UPDATED_INICIO, "inicio.in=" + UPDATED_INICIO);
    }

    @Test
    @Transactional
    void getAllReservasByInicioIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where inicio is not null
        defaultReservaFiltering("inicio.specified=true", "inicio.specified=false");
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos equals to
        defaultReservaFiltering("duracionMinutos.equals=" + DEFAULT_DURACION_MINUTOS, "duracionMinutos.equals=" + UPDATED_DURACION_MINUTOS);
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos in
        defaultReservaFiltering(
            "duracionMinutos.in=" + DEFAULT_DURACION_MINUTOS + "," + UPDATED_DURACION_MINUTOS,
            "duracionMinutos.in=" + UPDATED_DURACION_MINUTOS
        );
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos is not null
        defaultReservaFiltering("duracionMinutos.specified=true", "duracionMinutos.specified=false");
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsGreaterThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos is greater than or equal to
        defaultReservaFiltering(
            "duracionMinutos.greaterThanOrEqual=" + DEFAULT_DURACION_MINUTOS,
            "duracionMinutos.greaterThanOrEqual=" + (DEFAULT_DURACION_MINUTOS + 1)
        );
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsLessThanOrEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos is less than or equal to
        defaultReservaFiltering(
            "duracionMinutos.lessThanOrEqual=" + DEFAULT_DURACION_MINUTOS,
            "duracionMinutos.lessThanOrEqual=" + SMALLER_DURACION_MINUTOS
        );
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsLessThanSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos is less than
        defaultReservaFiltering(
            "duracionMinutos.lessThan=" + (DEFAULT_DURACION_MINUTOS + 1),
            "duracionMinutos.lessThan=" + DEFAULT_DURACION_MINUTOS
        );
    }

    @Test
    @Transactional
    void getAllReservasByDuracionMinutosIsGreaterThanSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where duracionMinutos is greater than
        defaultReservaFiltering(
            "duracionMinutos.greaterThan=" + SMALLER_DURACION_MINUTOS,
            "duracionMinutos.greaterThan=" + DEFAULT_DURACION_MINUTOS
        );
    }

    @Test
    @Transactional
    void getAllReservasByEstadoIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where estado equals to
        defaultReservaFiltering("estado.equals=" + DEFAULT_ESTADO, "estado.equals=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllReservasByEstadoIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where estado in
        defaultReservaFiltering("estado.in=" + DEFAULT_ESTADO + "," + UPDATED_ESTADO, "estado.in=" + UPDATED_ESTADO);
    }

    @Test
    @Transactional
    void getAllReservasByEstadoIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where estado is not null
        defaultReservaFiltering("estado.specified=true", "estado.specified=false");
    }

    @Test
    @Transactional
    void getAllReservasByObservacionesIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where observaciones equals to
        defaultReservaFiltering("observaciones.equals=" + DEFAULT_OBSERVACIONES, "observaciones.equals=" + UPDATED_OBSERVACIONES);
    }

    @Test
    @Transactional
    void getAllReservasByObservacionesIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where observaciones in
        defaultReservaFiltering(
            "observaciones.in=" + DEFAULT_OBSERVACIONES + "," + UPDATED_OBSERVACIONES,
            "observaciones.in=" + UPDATED_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllReservasByObservacionesIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where observaciones is not null
        defaultReservaFiltering("observaciones.specified=true", "observaciones.specified=false");
    }

    @Test
    @Transactional
    void getAllReservasByObservacionesContainsSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where observaciones contains
        defaultReservaFiltering("observaciones.contains=" + DEFAULT_OBSERVACIONES, "observaciones.contains=" + UPDATED_OBSERVACIONES);
    }

    @Test
    @Transactional
    void getAllReservasByObservacionesNotContainsSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where observaciones does not contain
        defaultReservaFiltering(
            "observaciones.doesNotContain=" + UPDATED_OBSERVACIONES,
            "observaciones.doesNotContain=" + DEFAULT_OBSERVACIONES
        );
    }

    @Test
    @Transactional
    void getAllReservasByCreadaEnIsEqualToSomething() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where creadaEn equals to
        defaultReservaFiltering("creadaEn.equals=" + DEFAULT_CREADA_EN, "creadaEn.equals=" + UPDATED_CREADA_EN);
    }

    @Test
    @Transactional
    void getAllReservasByCreadaEnIsInShouldWork() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where creadaEn in
        defaultReservaFiltering("creadaEn.in=" + DEFAULT_CREADA_EN + "," + UPDATED_CREADA_EN, "creadaEn.in=" + UPDATED_CREADA_EN);
    }

    @Test
    @Transactional
    void getAllReservasByCreadaEnIsNullOrNotNull() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        // Get all the reservaList where creadaEn is not null
        defaultReservaFiltering("creadaEn.specified=true", "creadaEn.specified=false");
    }

    @Test
    @Transactional
    void getAllReservasByCanchaIsEqualToSomething() throws Exception {
        Cancha cancha;
        if (TestUtil.findAll(em, Cancha.class).isEmpty()) {
            reservaRepository.saveAndFlush(reserva);
            cancha = CanchaResourceIT.createEntity(em);
        } else {
            cancha = TestUtil.findAll(em, Cancha.class).get(0);
        }
        em.persist(cancha);
        em.flush();
        reserva.setCancha(cancha);
        reservaRepository.saveAndFlush(reserva);
        Long canchaId = cancha.getId();
        // Get all the reservaList where cancha equals to canchaId
        defaultReservaShouldBeFound("canchaId.equals=" + canchaId);

        // Get all the reservaList where cancha equals to (canchaId + 1)
        defaultReservaShouldNotBeFound("canchaId.equals=" + (canchaId + 1));
    }

    @Test
    @Transactional
    void getAllReservasByUsuarioIsEqualToSomething() throws Exception {
        User usuario;
        if (TestUtil.findAll(em, User.class).isEmpty()) {
            reservaRepository.saveAndFlush(reserva);
            usuario = UserResourceIT.createEntity();
        } else {
            usuario = TestUtil.findAll(em, User.class).get(0);
        }
        em.persist(usuario);
        em.flush();
        reserva.setUsuario(usuario);
        reservaRepository.saveAndFlush(reserva);
        Long usuarioId = usuario.getId();
        // Get all the reservaList where usuario equals to usuarioId
        defaultReservaShouldBeFound("usuarioId.equals=" + usuarioId);

        // Get all the reservaList where usuario equals to (usuarioId + 1)
        defaultReservaShouldNotBeFound("usuarioId.equals=" + (usuarioId + 1));
    }

    private void defaultReservaFiltering(String shouldBeFound, String shouldNotBeFound) throws Exception {
        defaultReservaShouldBeFound(shouldBeFound);
        defaultReservaShouldNotBeFound(shouldNotBeFound);
    }

    /**
     * Executes the search, and checks that the default entity is returned.
     */
    private void defaultReservaShouldBeFound(String filter) throws Exception {
        restReservaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$.[*].id").value(hasItem(reserva.getId().intValue())))
            .andExpect(jsonPath("$.[*].inicio").value(hasItem(DEFAULT_INICIO.toString())))
            .andExpect(jsonPath("$.[*].duracionMinutos").value(hasItem(DEFAULT_DURACION_MINUTOS)))
            .andExpect(jsonPath("$.[*].estado").value(hasItem(DEFAULT_ESTADO.toString())))
            .andExpect(jsonPath("$.[*].observaciones").value(hasItem(DEFAULT_OBSERVACIONES)))
            .andExpect(jsonPath("$.[*].creadaEn").value(hasItem(DEFAULT_CREADA_EN.toString())));

        // Check, that the count call also returns 1
        restReservaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("1"));
    }

    /**
     * Executes the search, and checks that the default entity is not returned.
     */
    private void defaultReservaShouldNotBeFound(String filter) throws Exception {
        restReservaMockMvc
            .perform(get(ENTITY_API_URL + "?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(jsonPath("$").isArray())
            .andExpect(jsonPath("$").isEmpty());

        // Check, that the count call also returns 0
        restReservaMockMvc
            .perform(get(ENTITY_API_URL + "/count?sort=id,desc&" + filter))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON_VALUE))
            .andExpect(content().string("0"));
    }

    @Test
    @Transactional
    void getNonExistingReserva() throws Exception {
        // Get the reserva
        restReservaMockMvc.perform(get(ENTITY_API_URL_ID, Long.MAX_VALUE)).andExpect(status().isNotFound());
    }

    @Test
    @Transactional
    void putExistingReserva() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reserva
        Reserva updatedReserva = reservaRepository.findById(reserva.getId()).orElseThrow();
        // Disconnect from session so that the updates on updatedReserva are not directly saved in db
        em.detach(updatedReserva);
        updatedReserva
            .inicio(UPDATED_INICIO)
            .duracionMinutos(UPDATED_DURACION_MINUTOS)
            .estado(UPDATED_ESTADO)
            .observaciones(UPDATED_OBSERVACIONES)
            .creadaEn(UPDATED_CREADA_EN);
        ReservaDTO reservaDTO = reservaMapper.toDto(updatedReserva);

        restReservaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reservaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO))
            )
            .andExpect(status().isOk());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertPersistedReservaToMatchAllProperties(updatedReserva);
    }

    @Test
    @Transactional
    void putNonExistingReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, reservaDTO.getId()).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithIdMismatchReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(
                put(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(om.writeValueAsBytes(reservaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void putWithMissingIdPathParamReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(put(ENTITY_API_URL).contentType(MediaType.APPLICATION_JSON).content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void partialUpdateReservaWithPatch() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reserva using partial update
        Reserva partialUpdatedReserva = new Reserva();
        partialUpdatedReserva.setId(reserva.getId());

        partialUpdatedReserva.inicio(UPDATED_INICIO);

        restReservaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReserva.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReserva))
            )
            .andExpect(status().isOk());

        // Validate the Reserva in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReservaUpdatableFieldsEquals(createUpdateProxyForBean(partialUpdatedReserva, reserva), getPersistedReserva(reserva));
    }

    @Test
    @Transactional
    void fullUpdateReservaWithPatch() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        long databaseSizeBeforeUpdate = getRepositoryCount();

        // Update the reserva using partial update
        Reserva partialUpdatedReserva = new Reserva();
        partialUpdatedReserva.setId(reserva.getId());

        partialUpdatedReserva
            .inicio(UPDATED_INICIO)
            .duracionMinutos(UPDATED_DURACION_MINUTOS)
            .estado(UPDATED_ESTADO)
            .observaciones(UPDATED_OBSERVACIONES)
            .creadaEn(UPDATED_CREADA_EN);

        restReservaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, partialUpdatedReserva.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(partialUpdatedReserva))
            )
            .andExpect(status().isOk());

        // Validate the Reserva in the database

        assertSameRepositoryCount(databaseSizeBeforeUpdate);
        assertReservaUpdatableFieldsEquals(partialUpdatedReserva, getPersistedReserva(partialUpdatedReserva));
    }

    @Test
    @Transactional
    void patchNonExistingReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If the entity doesn't have an ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, reservaDTO.getId())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reservaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithIdMismatchReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(
                patch(ENTITY_API_URL_ID, longCount.incrementAndGet())
                    .contentType("application/merge-patch+json")
                    .content(om.writeValueAsBytes(reservaDTO))
            )
            .andExpect(status().isBadRequest());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void patchWithMissingIdPathParamReserva() throws Exception {
        long databaseSizeBeforeUpdate = getRepositoryCount();
        reserva.setId(longCount.incrementAndGet());

        // Create the Reserva
        ReservaDTO reservaDTO = reservaMapper.toDto(reserva);

        // If url ID doesn't match entity ID, it will throw BadRequestAlertException
        restReservaMockMvc
            .perform(patch(ENTITY_API_URL).contentType("application/merge-patch+json").content(om.writeValueAsBytes(reservaDTO)))
            .andExpect(status().isMethodNotAllowed());

        // Validate the Reserva in the database
        assertSameRepositoryCount(databaseSizeBeforeUpdate);
    }

    @Test
    @Transactional
    void deleteReserva() throws Exception {
        // Initialize the database
        insertedReserva = reservaRepository.saveAndFlush(reserva);

        long databaseSizeBeforeDelete = getRepositoryCount();

        // Delete the reserva
        restReservaMockMvc
            .perform(delete(ENTITY_API_URL_ID, reserva.getId()).accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isNoContent());

        // Validate the database contains one less item
        assertDecrementedRepositoryCount(databaseSizeBeforeDelete);
    }

    protected long getRepositoryCount() {
        return reservaRepository.count();
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

    protected Reserva getPersistedReserva(Reserva reserva) {
        return reservaRepository.findById(reserva.getId()).orElseThrow();
    }

    protected void assertPersistedReservaToMatchAllProperties(Reserva expectedReserva) {
        assertReservaAllPropertiesEquals(expectedReserva, getPersistedReserva(expectedReserva));
    }

    protected void assertPersistedReservaToMatchUpdatableProperties(Reserva expectedReserva) {
        assertReservaAllUpdatablePropertiesEquals(expectedReserva, getPersistedReserva(expectedReserva));
    }
}
