package ar.edu.um.isa.clubreservas.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.isa.clubreservas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeporteDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(DeporteDTO.class);
        DeporteDTO deporteDTO1 = new DeporteDTO();
        deporteDTO1.setId(1L);
        DeporteDTO deporteDTO2 = new DeporteDTO();
        assertThat(deporteDTO1).isNotEqualTo(deporteDTO2);
        deporteDTO2.setId(deporteDTO1.getId());
        assertThat(deporteDTO1).isEqualTo(deporteDTO2);
        deporteDTO2.setId(2L);
        assertThat(deporteDTO1).isNotEqualTo(deporteDTO2);
        deporteDTO1.setId(null);
        assertThat(deporteDTO1).isNotEqualTo(deporteDTO2);
    }
}
