package ar.edu.um.isa.clubreservas.service.mapper;

import static ar.edu.um.isa.clubreservas.domain.DeporteAsserts.*;
import static ar.edu.um.isa.clubreservas.domain.DeporteTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class DeporteMapperTest {

    private DeporteMapper deporteMapper;

    @BeforeEach
    void setUp() {
        deporteMapper = new DeporteMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getDeporteSample1();
        var actual = deporteMapper.toEntity(deporteMapper.toDto(expected));
        assertDeporteAllPropertiesEquals(expected, actual);
    }
}
