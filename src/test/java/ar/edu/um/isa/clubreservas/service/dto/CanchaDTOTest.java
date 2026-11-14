package ar.edu.um.isa.clubreservas.service.dto;

import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.isa.clubreservas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CanchaDTOTest {

    @Test
    void dtoEqualsVerifier() throws Exception {
        TestUtil.equalsVerifier(CanchaDTO.class);
        CanchaDTO canchaDTO1 = new CanchaDTO();
        canchaDTO1.setId(1L);
        CanchaDTO canchaDTO2 = new CanchaDTO();
        assertThat(canchaDTO1).isNotEqualTo(canchaDTO2);
        canchaDTO2.setId(canchaDTO1.getId());
        assertThat(canchaDTO1).isEqualTo(canchaDTO2);
        canchaDTO2.setId(2L);
        assertThat(canchaDTO1).isNotEqualTo(canchaDTO2);
        canchaDTO1.setId(null);
        assertThat(canchaDTO1).isNotEqualTo(canchaDTO2);
    }
}
