package ar.edu.um.isa.clubreservas.domain;

import static ar.edu.um.isa.clubreservas.domain.CanchaTestSamples.*;
import static ar.edu.um.isa.clubreservas.domain.ReservaTestSamples.*;
import static org.assertj.core.api.Assertions.assertThat;

import ar.edu.um.isa.clubreservas.web.rest.TestUtil;
import org.junit.jupiter.api.Test;

class ReservaTest {

    @Test
    void equalsVerifier() throws Exception {
        TestUtil.equalsVerifier(Reserva.class);
        Reserva reserva1 = getReservaSample1();
        Reserva reserva2 = new Reserva();
        assertThat(reserva1).isNotEqualTo(reserva2);

        reserva2.setId(reserva1.getId());
        assertThat(reserva1).isEqualTo(reserva2);

        reserva2 = getReservaSample2();
        assertThat(reserva1).isNotEqualTo(reserva2);
    }

    @Test
    void canchaTest() {
        Reserva reserva = getReservaRandomSampleGenerator();
        Cancha canchaBack = getCanchaRandomSampleGenerator();

        reserva.setCancha(canchaBack);
        assertThat(reserva.getCancha()).isEqualTo(canchaBack);

        reserva.cancha(null);
        assertThat(reserva.getCancha()).isNull();
    }
}
