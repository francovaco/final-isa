package ar.edu.um.isa.clubreservas.repository;

import ar.edu.um.isa.clubreservas.domain.Reserva;
import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Reserva entity.
 */
@Repository
public interface ReservaRepository extends JpaRepository<Reserva, Long>, JpaSpecificationExecutor<Reserva> {
    @Query("select reserva from Reserva reserva where reserva.usuario.login = ?#{authentication.name}")
    List<Reserva> findByUsuarioIsCurrentUser();

    /**
     * Reservas de una cancha, en un estado dado, que empiezan dentro de un rango.
     * Se usa para buscar candidatas a superponerse con una reserva nueva.
     */
    List<Reserva> findByCanchaIdAndEstadoAndInicioBetween(Long canchaId, EstadoReserva estado, Instant desde, Instant hasta);

    /**
     * Cantidad de reservas de un usuario, en un estado dado, que empiezan después de un instante.
     */
    long countByUsuarioLoginAndEstadoAndInicioAfter(String login, EstadoReserva estado, Instant despuesDe);

    default Optional<Reserva> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Reserva> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Reserva> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select reserva from Reserva reserva left join fetch reserva.cancha left join fetch reserva.usuario",
        countQuery = "select count(reserva) from Reserva reserva"
    )
    Page<Reserva> findAllWithToOneRelationships(Pageable pageable);

    @Query("select reserva from Reserva reserva left join fetch reserva.cancha left join fetch reserva.usuario")
    List<Reserva> findAllWithToOneRelationships();

    @Query("select reserva from Reserva reserva left join fetch reserva.cancha left join fetch reserva.usuario where reserva.id =:id")
    Optional<Reserva> findOneWithToOneRelationships(@Param("id") Long id);
}
