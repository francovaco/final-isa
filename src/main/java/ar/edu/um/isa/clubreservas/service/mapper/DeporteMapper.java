package ar.edu.um.isa.clubreservas.service.mapper;

import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Deporte} and its DTO {@link DeporteDTO}.
 */
@Mapper(componentModel = "spring")
public interface DeporteMapper extends EntityMapper<DeporteDTO, Deporte> {}
