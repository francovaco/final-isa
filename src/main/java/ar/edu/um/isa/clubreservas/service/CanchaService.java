package ar.edu.um.isa.clubreservas.service;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.repository.CanchaRepository;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.mapper.CanchaMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.isa.clubreservas.domain.Cancha}.
 */
@Service
@Transactional
public class CanchaService {

    private static final Logger LOG = LoggerFactory.getLogger(CanchaService.class);

    private final CanchaRepository canchaRepository;

    private final CanchaMapper canchaMapper;

    public CanchaService(CanchaRepository canchaRepository, CanchaMapper canchaMapper) {
        this.canchaRepository = canchaRepository;
        this.canchaMapper = canchaMapper;
    }

    /**
     * Save a cancha.
     *
     * @param canchaDTO the entity to save.
     * @return the persisted entity.
     */
    public CanchaDTO save(CanchaDTO canchaDTO) {
        LOG.debug("Request to save Cancha : {}", canchaDTO);
        Cancha cancha = canchaMapper.toEntity(canchaDTO);
        cancha = canchaRepository.save(cancha);
        return canchaMapper.toDto(cancha);
    }

    /**
     * Update a cancha.
     *
     * @param canchaDTO the entity to save.
     * @return the persisted entity.
     */
    public CanchaDTO update(CanchaDTO canchaDTO) {
        LOG.debug("Request to update Cancha : {}", canchaDTO);
        Cancha cancha = canchaMapper.toEntity(canchaDTO);
        cancha = canchaRepository.save(cancha);
        return canchaMapper.toDto(cancha);
    }

    /**
     * Partially update a cancha.
     *
     * @param canchaDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<CanchaDTO> partialUpdate(CanchaDTO canchaDTO) {
        LOG.debug("Request to partially update Cancha : {}", canchaDTO);

        return canchaRepository
            .findById(canchaDTO.getId())
            .map(existingCancha -> {
                canchaMapper.partialUpdate(existingCancha, canchaDTO);

                return existingCancha;
            })
            .map(canchaRepository::save)
            .map(canchaMapper::toDto);
    }

    /**
     * Get all the canchas.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<CanchaDTO> findAll() {
        LOG.debug("Request to get all Canchas");
        return canchaRepository.findAll().stream().map(canchaMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get all the canchas with eager load of many-to-many relationships.
     *
     * @return the list of entities.
     */
    public Page<CanchaDTO> findAllWithEagerRelationships(Pageable pageable) {
        return canchaRepository.findAllWithEagerRelationships(pageable).map(canchaMapper::toDto);
    }

    /**
     * Get one cancha by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<CanchaDTO> findOne(Long id) {
        LOG.debug("Request to get Cancha : {}", id);
        return canchaRepository.findOneWithEagerRelationships(id).map(canchaMapper::toDto);
    }

    /**
     * Delete the cancha by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Cancha : {}", id);
        canchaRepository.deleteById(id);
    }
}
