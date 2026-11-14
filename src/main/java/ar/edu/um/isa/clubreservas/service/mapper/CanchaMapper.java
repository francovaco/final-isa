package ar.edu.um.isa.clubreservas.service.mapper;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Deporte;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.dto.DeporteDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Cancha} and its DTO {@link CanchaDTO}.
 */
@Mapper(componentModel = "spring")
public interface CanchaMapper extends EntityMapper<CanchaDTO, Cancha> {
    @Mapping(target = "deporte", source = "deporte", qualifiedByName = "deporteNombre")
    CanchaDTO toDto(Cancha s);

    @Named("deporteNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    DeporteDTO toDtoDeporteNombre(Deporte deporte);
}
