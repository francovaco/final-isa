package ar.edu.um.isa.clubreservas.config;

import java.time.Clock;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Reloj de la aplicación. Se inyecta en lugar de llamar a {@code Instant.now()}
 * para que los tests puedan fijar la fecha y hora.
 */
@Configuration
public class ClockConfiguration {

    @Bean
    public Clock clock() {
        return Clock.systemUTC();
    }
}
