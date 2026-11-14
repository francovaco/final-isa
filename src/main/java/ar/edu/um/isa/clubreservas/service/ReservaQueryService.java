package ar.edu.um.isa.clubreservas.service;

import ar.edu.um.isa.clubreservas.domain.*; // for static metamodels
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.repository.ReservaRepository;
import ar.edu.um.isa.clubreservas.service.criteria.ReservaCriteria;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.ReservaMapper;
import jakarta.persistence.criteria.JoinType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tech.jhipster.service.QueryService;

/**
 * Service for executing complex queries for {@link Reserva} entities in the database.
 * The main input is a {@link ReservaCriteria} which gets converted to {@link Specification},
 * in a way that all the filters must apply.
 * It returns a {@link Page} of {@link ReservaDTO} which fulfills the criteria.
 */
@Service
@Transactional(readOnly = true)
public class ReservaQueryService extends QueryService<Reserva> {

    private static final Logger LOG = LoggerFactory.getLogger(ReservaQueryService.class);

    private final ReservaRepository reservaRepository;

    private final ReservaMapper reservaMapper;

    public ReservaQueryService(ReservaRepository reservaRepository, ReservaMapper reservaMapper) {
        this.reservaRepository = reservaRepository;
        this.reservaMapper = reservaMapper;
    }

    /**
     * Return a {@link Page} of {@link ReservaDTO} which matches the criteria from the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @param page The page, which should be returned.
     * @return the matching entities.
     */
    @Transactional(readOnly = true)
    public Page<ReservaDTO> findByCriteria(ReservaCriteria criteria, Pageable page) {
        LOG.debug("find by criteria : {}, page: {}", criteria, page);
        final Specification<Reserva> specification = createSpecification(criteria);
        return reservaRepository.findAll(specification, page).map(reservaMapper::toDto);
    }

    /**
     * Return the number of matching entities in the database.
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the number of matching entities.
     */
    @Transactional(readOnly = true)
    public long countByCriteria(ReservaCriteria criteria) {
        LOG.debug("count by criteria : {}", criteria);
        final Specification<Reserva> specification = createSpecification(criteria);
        return reservaRepository.count(specification);
    }

    /**
     * Function to convert {@link ReservaCriteria} to a {@link Specification}
     * @param criteria The object which holds all the filters, which the entities should match.
     * @return the matching {@link Specification} of the entity.
     */
    protected Specification<Reserva> createSpecification(ReservaCriteria criteria) {
        Specification<Reserva> specification = Specification.where(null);
        if (criteria != null) {
            // This has to be called first, because the distinct method returns null
            specification = Specification.allOf(
                Boolean.TRUE.equals(criteria.getDistinct()) ? distinct(criteria.getDistinct()) : null,
                buildRangeSpecification(criteria.getId(), Reserva_.id),
                buildRangeSpecification(criteria.getInicio(), Reserva_.inicio),
                buildRangeSpecification(criteria.getDuracionMinutos(), Reserva_.duracionMinutos),
                buildSpecification(criteria.getEstado(), Reserva_.estado),
                buildStringSpecification(criteria.getObservaciones(), Reserva_.observaciones),
                buildRangeSpecification(criteria.getCreadaEn(), Reserva_.creadaEn),
                buildSpecification(criteria.getCanchaId(), root -> root.join(Reserva_.cancha, JoinType.LEFT).get(Cancha_.id)),
                buildSpecification(criteria.getUsuarioId(), root -> root.join(Reserva_.usuario, JoinType.LEFT).get(User_.id))
            );
        }
        return specification;
    }
}
