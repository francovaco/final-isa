package ar.edu.um.isa.clubreservas.service.mapper;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.User;
import ar.edu.um.isa.clubreservas.service.dto.CanchaDTO;
import ar.edu.um.isa.clubreservas.service.dto.ReservaDTO;
import ar.edu.um.isa.clubreservas.service.dto.UserDTO;
import org.mapstruct.*;

/**
 * Mapper for the entity {@link Reserva} and its DTO {@link ReservaDTO}.
 */
@Mapper(componentModel = "spring")
public interface ReservaMapper extends EntityMapper<ReservaDTO, Reserva> {
    @Mapping(target = "cancha", source = "cancha", qualifiedByName = "canchaNombre")
    @Mapping(target = "usuario", source = "usuario", qualifiedByName = "userLogin")
    ReservaDTO toDto(Reserva s);

    @Named("canchaNombre")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "nombre", source = "nombre")
    CanchaDTO toDtoCanchaNombre(Cancha cancha);

    @Named("userLogin")
    @BeanMapping(ignoreByDefault = true)
    @Mapping(target = "id", source = "id")
    @Mapping(target = "login", source = "login")
    UserDTO toDtoUserLogin(User user);
}
