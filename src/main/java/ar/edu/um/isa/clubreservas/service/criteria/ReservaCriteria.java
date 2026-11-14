package ar.edu.um.isa.clubreservas.service.criteria;

import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import java.io.Serializable;
import java.util.Objects;
import java.util.Optional;
import org.springdoc.core.annotations.ParameterObject;
import tech.jhipster.service.Criteria;
import tech.jhipster.service.filter.*;

/**
 * Criteria class for the {@link ar.edu.um.isa.clubreservas.domain.Reserva} entity. This class is used
 * in {@link ar.edu.um.isa.clubreservas.web.rest.ReservaResource} to receive all the possible filtering options from
 * the Http GET request parameters.
 * For example the following could be a valid request:
 * {@code /reservas?id.greaterThan=5&attr1.contains=something&attr2.specified=false}
 * As Spring is unable to properly convert the types, unless specific {@link Filter} class are used, we need to use
 * fix type specific filters.
 */
@ParameterObject
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReservaCriteria implements Serializable, Criteria {

    /**
     * Class for filtering EstadoReserva
     */
    public static class EstadoReservaFilter extends Filter<EstadoReserva> {

        public EstadoReservaFilter() {}

        public EstadoReservaFilter(EstadoReservaFilter filter) {
            super(filter);
        }

        @Override
        public EstadoReservaFilter copy() {
            return new EstadoReservaFilter(this);
        }
    }

    private static final long serialVersionUID = 1L;

    private LongFilter id;

    private InstantFilter inicio;

    private IntegerFilter duracionMinutos;

    private EstadoReservaFilter estado;

    private StringFilter observaciones;

    private InstantFilter creadaEn;

    private LongFilter canchaId;

    private LongFilter usuarioId;

    private Boolean distinct;

    public ReservaCriteria() {}

    public ReservaCriteria(ReservaCriteria other) {
        this.id = other.optionalId().map(LongFilter::copy).orElse(null);
        this.inicio = other.optionalInicio().map(InstantFilter::copy).orElse(null);
        this.duracionMinutos = other.optionalDuracionMinutos().map(IntegerFilter::copy).orElse(null);
        this.estado = other.optionalEstado().map(EstadoReservaFilter::copy).orElse(null);
        this.observaciones = other.optionalObservaciones().map(StringFilter::copy).orElse(null);
        this.creadaEn = other.optionalCreadaEn().map(InstantFilter::copy).orElse(null);
        this.canchaId = other.optionalCanchaId().map(LongFilter::copy).orElse(null);
        this.usuarioId = other.optionalUsuarioId().map(LongFilter::copy).orElse(null);
        this.distinct = other.distinct;
    }

    @Override
    public ReservaCriteria copy() {
        return new ReservaCriteria(this);
    }

    public LongFilter getId() {
        return id;
    }

    public Optional<LongFilter> optionalId() {
        return Optional.ofNullable(id);
    }

    public LongFilter id() {
        if (id == null) {
            setId(new LongFilter());
        }
        return id;
    }

    public void setId(LongFilter id) {
        this.id = id;
    }

    public InstantFilter getInicio() {
        return inicio;
    }

    public Optional<InstantFilter> optionalInicio() {
        return Optional.ofNullable(inicio);
    }

    public InstantFilter inicio() {
        if (inicio == null) {
            setInicio(new InstantFilter());
        }
        return inicio;
    }

    public void setInicio(InstantFilter inicio) {
        this.inicio = inicio;
    }

    public IntegerFilter getDuracionMinutos() {
        return duracionMinutos;
    }

    public Optional<IntegerFilter> optionalDuracionMinutos() {
        return Optional.ofNullable(duracionMinutos);
    }

    public IntegerFilter duracionMinutos() {
        if (duracionMinutos == null) {
            setDuracionMinutos(new IntegerFilter());
        }
        return duracionMinutos;
    }

    public void setDuracionMinutos(IntegerFilter duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoReservaFilter getEstado() {
        return estado;
    }

    public Optional<EstadoReservaFilter> optionalEstado() {
        return Optional.ofNullable(estado);
    }

    public EstadoReservaFilter estado() {
        if (estado == null) {
            setEstado(new EstadoReservaFilter());
        }
        return estado;
    }

    public void setEstado(EstadoReservaFilter estado) {
        this.estado = estado;
    }

    public StringFilter getObservaciones() {
        return observaciones;
    }

    public Optional<StringFilter> optionalObservaciones() {
        return Optional.ofNullable(observaciones);
    }

    public StringFilter observaciones() {
        if (observaciones == null) {
            setObservaciones(new StringFilter());
        }
        return observaciones;
    }

    public void setObservaciones(StringFilter observaciones) {
        this.observaciones = observaciones;
    }

    public InstantFilter getCreadaEn() {
        return creadaEn;
    }

    public Optional<InstantFilter> optionalCreadaEn() {
        return Optional.ofNullable(creadaEn);
    }

    public InstantFilter creadaEn() {
        if (creadaEn == null) {
            setCreadaEn(new InstantFilter());
        }
        return creadaEn;
    }

    public void setCreadaEn(InstantFilter creadaEn) {
        this.creadaEn = creadaEn;
    }

    public LongFilter getCanchaId() {
        return canchaId;
    }

    public Optional<LongFilter> optionalCanchaId() {
        return Optional.ofNullable(canchaId);
    }

    public LongFilter canchaId() {
        if (canchaId == null) {
            setCanchaId(new LongFilter());
        }
        return canchaId;
    }

    public void setCanchaId(LongFilter canchaId) {
        this.canchaId = canchaId;
    }

    public LongFilter getUsuarioId() {
        return usuarioId;
    }

    public Optional<LongFilter> optionalUsuarioId() {
        return Optional.ofNullable(usuarioId);
    }

    public LongFilter usuarioId() {
        if (usuarioId == null) {
            setUsuarioId(new LongFilter());
        }
        return usuarioId;
    }

    public void setUsuarioId(LongFilter usuarioId) {
        this.usuarioId = usuarioId;
    }

    public Boolean getDistinct() {
        return distinct;
    }

    public Optional<Boolean> optionalDistinct() {
        return Optional.ofNullable(distinct);
    }

    public Boolean distinct() {
        if (distinct == null) {
            setDistinct(true);
        }
        return distinct;
    }

    public void setDistinct(Boolean distinct) {
        this.distinct = distinct;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final ReservaCriteria that = (ReservaCriteria) o;
        return (
            Objects.equals(id, that.id) &&
            Objects.equals(inicio, that.inicio) &&
            Objects.equals(duracionMinutos, that.duracionMinutos) &&
            Objects.equals(estado, that.estado) &&
            Objects.equals(observaciones, that.observaciones) &&
            Objects.equals(creadaEn, that.creadaEn) &&
            Objects.equals(canchaId, that.canchaId) &&
            Objects.equals(usuarioId, that.usuarioId) &&
            Objects.equals(distinct, that.distinct)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, inicio, duracionMinutos, estado, observaciones, creadaEn, canchaId, usuarioId, distinct);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReservaCriteria{" +
            optionalId().map(f -> "id=" + f + ", ").orElse("") +
            optionalInicio().map(f -> "inicio=" + f + ", ").orElse("") +
            optionalDuracionMinutos().map(f -> "duracionMinutos=" + f + ", ").orElse("") +
            optionalEstado().map(f -> "estado=" + f + ", ").orElse("") +
            optionalObservaciones().map(f -> "observaciones=" + f + ", ").orElse("") +
            optionalCreadaEn().map(f -> "creadaEn=" + f + ", ").orElse("") +
            optionalCanchaId().map(f -> "canchaId=" + f + ", ").orElse("") +
            optionalUsuarioId().map(f -> "usuarioId=" + f + ", ").orElse("") +
            optionalDistinct().map(f -> "distinct=" + f + ", ").orElse("") +
        "}";
    }
}
