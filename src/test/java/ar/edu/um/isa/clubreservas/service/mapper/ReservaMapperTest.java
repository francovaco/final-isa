package ar.edu.um.isa.clubreservas.service.mapper;

import static ar.edu.um.isa.clubreservas.domain.ReservaAsserts.*;
import static ar.edu.um.isa.clubreservas.domain.ReservaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ReservaMapperTest {

    private ReservaMapper reservaMapper;

    @BeforeEach
    void setUp() {
        reservaMapper = new ReservaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getReservaSample1();
        var actual = reservaMapper.toEntity(reservaMapper.toDto(expected));
        assertReservaAllPropertiesEquals(expected, actual);
    }
}
