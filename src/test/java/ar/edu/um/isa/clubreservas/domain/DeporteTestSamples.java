package ar.edu.um.isa.clubreservas.domain;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

public class DeporteTestSamples {

    private static final Random random = new Random();
    private static final AtomicLong longCount = new AtomicLong(random.nextInt() + (2 * Integer.MAX_VALUE));
    private static final AtomicInteger intCount = new AtomicInteger(random.nextInt() + (2 * Short.MAX_VALUE));

    public static Deporte getDeporteSample1() {
        return new Deporte().id(1L).nombre("nombre1").descripcion("descripcion1").jugadoresPorEquipo(1);
    }

    public static Deporte getDeporteSample2() {
        return new Deporte().id(2L).nombre("nombre2").descripcion("descripcion2").jugadoresPorEquipo(2);
    }

    public static Deporte getDeporteRandomSampleGenerator() {
        return new Deporte()
            .id(longCount.incrementAndGet())
            .nombre(UUID.randomUUID().toString())
            .descripcion(UUID.randomUUID().toString())
            .jugadoresPorEquipo(intCount.incrementAndGet());
    }
}
