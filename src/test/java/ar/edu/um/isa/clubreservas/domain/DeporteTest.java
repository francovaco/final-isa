package ar.edu.um.isa.clubreservas.domain;

import static ar.edu.um.isa.clubreservas.domain.DeporteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.isa.clubreservas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class DeporteTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Deporte.class);
        Deporte deporte1 = getDeporteSample1();
        Deporte deporte2 = new Deporte();
        assertThat(deporte1).isNotEqualTo(deporte2);

        deporte2.setId(deporte1.getId());
        assertThat(deporte1).isEqualTo(deporte2);

        deporte2 = getDeporteSample2();
        assertThat(deporte1).isNotEqualTo(deporte2);
    }
}
