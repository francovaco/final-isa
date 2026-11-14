package ar.edu.um.isa.clubreservas.repository;

import ar.edu.um.isa.clubreservas.domain.Cancha;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for the Cancha entity.
 */
@Repository
public interface CanchaRepository extends JpaRepository<Cancha, Long> {
    default Optional<Cancha> findOneWithEagerRelationships(Long id) {
        return this.findOneWithToOneRelationships(id);
    }

    default List<Cancha> findAllWithEagerRelationships() {
        return this.findAllWithToOneRelationships();
    }

    default Page<Cancha> findAllWithEagerRelationships(Pageable pageable) {
        return this.findAllWithToOneRelationships(pageable);
    }

    @Query(
        value = "select cancha from Cancha cancha left join fetch cancha.deporte",
        countQuery = "select count(cancha) from Cancha cancha"
    )
    Page<Cancha> findAllWithToOneRelationships(Pageable pageable);

    @Query("select cancha from Cancha cancha left join fetch cancha.deporte")
    List<Cancha> findAllWithToOneRelationships();

    @Query("select cancha from Cancha cancha left join fetch cancha.deporte where cancha.id =:id")
    Optional<Cancha> findOneWithToOneRelationships(@Param("id") Long id);
}
