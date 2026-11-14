package ar.edu.um.isa.clubreservas.service;

import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.repository.DeporteRepository;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
import ar.edu.um.isa.clubreservas.service.mapper.DeporteMapper;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service Implementation for managing {@link ar.edu.um.isa.clubreservas.domain.Deporte}.
 */
@Service
@Transactional
public class DeporteService {

    private static final Logger LOG = LoggerFactory.getLogger(DeporteService.class);

    private final DeporteRepository deporteRepository;

    private final DeporteMapper deporteMapper;

    public DeporteService(DeporteRepository deporteRepository, DeporteMapper deporteMapper) {
        this.deporteRepository = deporteRepository;
        this.deporteMapper = deporteMapper;
    }

    /**
     * Save a deporte.
     *
     * @param deporteDTO the entity to save.
     * @return the persisted entity.
     */
    public DeporteDTO save(DeporteDTO deporteDTO) {
        LOG.debug("Request to save Deporte : {}", deporteDTO);
        Deporte deporte = deporteMapper.toEntity(deporteDTO);
        deporte = deporteRepository.save(deporte);
        return deporteMapper.toDto(deporte);
    }

    /**
     * Update a deporte.
     *
     * @param deporteDTO the entity to save.
     * @return the persisted entity.
     */
    public DeporteDTO update(DeporteDTO deporteDTO) {
        LOG.debug("Request to update Deporte : {}", deporteDTO);
        Deporte deporte = deporteMapper.toEntity(deporteDTO);
        deporte = deporteRepository.save(deporte);
        return deporteMapper.toDto(deporte);
    }

    /**
     * Partially update a deporte.
     *
     * @param deporteDTO the entity to update partially.
     * @return the persisted entity.
     */
    public Optional<DeporteDTO> partialUpdate(DeporteDTO deporteDTO) {
        LOG.debug("Request to partially update Deporte : {}", deporteDTO);

        return deporteRepository
            .findById(deporteDTO.getId())
            .map(existingDeporte -> {
                deporteMapper.partialUpdate(existingDeporte, deporteDTO);

                return existingDeporte;
            })
            .map(deporteRepository::save)
            .map(deporteMapper::toDto);
    }

    /**
     * Get all the deportes.
     *
     * @return the list of entities.
     */
    @Transactional(readOnly = true)
    public List<DeporteDTO> findAll() {
        LOG.debug("Request to get all Deportes");
        return deporteRepository.findAll().stream().map(deporteMapper::toDto).collect(Collectors.toCollection(LinkedList::new));
    }

    /**
     * Get one deporte by id.
     *
     * @param id the id of the entity.
     * @return the entity.
     */
    @Transactional(readOnly = true)
    public Optional<DeporteDTO> findOne(Long id) {
        LOG.debug("Request to get Deporte : {}", id);
        return deporteRepository.findById(id).map(deporteMapper::toDto);
    }

    /**
     * Delete the deporte by id.
     *
     * @param id the id of the entity.
     */
    public void delete(Long id) {
        LOG.debug("Request to delete Deporte : {}", id);
        deporteRepository.deleteById(id);
    }
}
