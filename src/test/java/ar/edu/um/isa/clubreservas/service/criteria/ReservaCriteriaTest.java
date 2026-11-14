package ar.edu.um.isa.clubreservas.service.criteria;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Objects;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.assertj.core.api.Condition;
import org.junit.jupiter.api.Test;

class ReservaCriteriaTest {

    @Test
    void newReservaCriteriaHasAllFiltersNullTest() {
        var reservaCriteria = new ReservaCriteria();
        assertThat(reservaCriteria).is(criteriaFiltersAre(Objects::isNull));
    }

    @Test
    void reservaCriteriaFluentMethodsCreatesFiltersTest() {
        var reservaCriteria = new ReservaCriteria();

        setAllFilters(reservaCriteria);

        assertThat(reservaCriteria).is(criteriaFiltersAre(Objects::nonNull));
    }

    @Test
    void reservaCriteriaCopyCreatesNullFilterTest() {
        var reservaCriteria = new ReservaCriteria();
        var copy = reservaCriteria.copy();

        assertThat(reservaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::isNull)),
            criteria -> assertThat(criteria).isEqualTo(reservaCriteria)
        );
    }

    @Test
    void reservaCriteriaCopyDuplicatesEveryExistingFilterTest() {
        var reservaCriteria = new ReservaCriteria();
        setAllFilters(reservaCriteria);

        var copy = reservaCriteria.copy();

        assertThat(reservaCriteria).satisfies(
            criteria ->
                assertThat(criteria).is(
                    copyFiltersAre(copy, (a, b) -> (a == null || a instanceof Boolean) ? a == b : (a != b && a.equals(b)))
                ),
            criteria -> assertThat(criteria).isEqualTo(copy),
            criteria -> assertThat(criteria).hasSameHashCodeAs(copy)
        );

        assertThat(copy).satisfies(
            criteria -> assertThat(criteria).is(criteriaFiltersAre(Objects::nonNull)),
            criteria -> assertThat(criteria).isEqualTo(reservaCriteria)
        );
    }

    @Test
    void toStringVerifier() {
        var reservaCriteria = new ReservaCriteria();

        assertThat(reservaCriteria).hasToString("ReservaCriteria{}");
    }

    private static void setAllFilters(ReservaCriteria reservaCriteria) {
        reservaCriteria.id();
        reservaCriteria.inicio();
        reservaCriteria.duracionMinutos();
        reservaCriteria.estado();
        reservaCriteria.observaciones();
        reservaCriteria.creadaEn();
        reservaCriteria.canchaId();
        reservaCriteria.usuarioId();
        reservaCriteria.distinct();
    }

    private static Condition<ReservaCriteria> criteriaFiltersAre(Function<Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId()) &&
                condition.apply(criteria.getInicio()) &&
                condition.apply(criteria.getDuracionMinutos()) &&
                condition.apply(criteria.getEstado()) &&
                condition.apply(criteria.getObservaciones()) &&
                condition.apply(criteria.getCreadaEn()) &&
                condition.apply(criteria.getCanchaId()) &&
                condition.apply(criteria.getUsuarioId()) &&
                condition.apply(criteria.getDistinct()),
            "every filter matches"
        );
    }

    private static Condition<ReservaCriteria> copyFiltersAre(ReservaCriteria copy, BiFunction<Object, Object, Boolean> condition) {
        return new Condition<>(
            criteria ->
                condition.apply(criteria.getId(), copy.getId()) &&
                condition.apply(criteria.getInicio(), copy.getInicio()) &&
                condition.apply(criteria.getDuracionMinutos(), copy.getDuracionMinutos()) &&
                condition.apply(criteria.getEstado(), copy.getEstado()) &&
                condition.apply(criteria.getObservaciones(), copy.getObservaciones()) &&
                condition.apply(criteria.getCreadaEn(), copy.getCreadaEn()) &&
                condition.apply(criteria.getCanchaId(), copy.getCanchaId()) &&
                condition.apply(criteria.getUsuarioId(), copy.getUsuarioId()) &&
                condition.apply(criteria.getDistinct(), copy.getDistinct()),
            "every filter matches"
        );
    }
}
