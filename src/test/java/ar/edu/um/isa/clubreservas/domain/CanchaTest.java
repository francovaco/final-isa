package ar.edu.um.isa.clubreservas.domain;

import static ar.edu.um.isa.clubreservas.domain.CanchaTestSamples.*;
import static ar.edu.um.isa.clubreservas.domain.DeporteTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.isa.clubreservas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class CanchaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Cancha.class);
        Cancha cancha1 = getCanchaSample1();
        Cancha cancha2 = new Cancha();
        assertThat(cancha1).isNotEqualTo(cancha2);

        cancha2.setId(cancha1.getId());
        assertThat(cancha1).isEqualTo(cancha2);

        cancha2 = getCanchaSample2();
        assertThat(cancha1).isNotEqualTo(cancha2);
    }

    @Test
    void deporteTest() {
        Cancha cancha = getCanchaRandomSampleGenerator();
        Deporte deporteBack = getDeporteRandomSampleGenerator();

        cancha.setDeporte(deporteBack);
        assertThat(cancha.getDeporte()).isEqualTo(deporteBack);

        cancha.deporte(null);
        assertThat(cancha.getDeporte()).isNull();
    }
}
