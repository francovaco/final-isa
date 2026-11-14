package ar.edu.um.isa.clubreservas.service.mapper;

import static ar.edu.um.isa.clubreservas.domain.CanchaAsserts.*;
import static ar.edu.um.isa.clubreservas.domain.CanchaTestSamples.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class CanchaMapperTest {

    private CanchaMapper canchaMapper;

    @BeforeEach
    void setUp() {
        canchaMapper = new CanchaMapperImpl();
    }

    @Test
    void shouldConvertToDtoAndBack() {
        var expected = getCanchaSample1();
        var actual = canchaMapper.toEntity(canchaMapper.toDto(expected));
        assertCanchaAllPropertiesEquals(expected, actual);
    }
}
