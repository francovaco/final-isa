package ar.edu.um.isa.clubreservas.domain;

import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Reserva de una cancha por parte de un socio.
 */
@Entity
@Table(name = "reserva")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Reserva implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Column(name = "inicio", nullable = false)
    private Instant inicio;

    @NotNull
    @Min(value = 60)
    @Max(value = 120)
    @Column(name = "duracion_minutos", nullable = false)
    private Integer duracionMinutos;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "estado", nullable = false)
    private EstadoReserva estado;

    @Size(max = 255)
    @Column(name = "observaciones", length = 255)
    private String observaciones;

    @Column(name = "creada_en")
    private Instant creadaEn;

    @ManyToOne(optional = false)
    @NotNull
    @JsonIgnoreProperties(value = { "deporte" }, allowSetters = true)
    private Cancha cancha;

    @ManyToOne(fetch = FetchType.LAZY)
    private User usuario;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Reserva id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getInicio() {
        return this.inicio;
    }

    public Reserva inicio(Instant inicio) {
        this.setInicio(inicio);
        return this;
    }

    public void setInicio(Instant inicio) {
        this.inicio = inicio;
    }

    public Integer getDuracionMinutos() {
        return this.duracionMinutos;
    }

    public Reserva duracionMinutos(Integer duracionMinutos) {
        this.setDuracionMinutos(duracionMinutos);
        return this;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoReserva getEstado() {
        return this.estado;
    }

    public Reserva estado(EstadoReserva estado) {
        this.setEstado(estado);
        return this;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return this.observaciones;
    }

    public Reserva observaciones(String observaciones) {
        this.setObservaciones(observaciones);
        return this;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreadaEn() {
        return this.creadaEn;
    }

    public Reserva creadaEn(Instant creadaEn) {
        this.setCreadaEn(creadaEn);
        return this;
    }

    public void setCreadaEn(Instant creadaEn) {
        this.creadaEn = creadaEn;
    }

    public Cancha getCancha() {
        return this.cancha;
    }

    public void setCancha(Cancha cancha) {
        this.cancha = cancha;
    }

    public Reserva cancha(Cancha cancha) {
        this.setCancha(cancha);
        return this;
    }

    public User getUsuario() {
        return this.usuario;
    }

    public void setUsuario(User user) {
        this.usuario = user;
    }

    public Reserva usuario(User user) {
        this.setUsuario(user);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Reserva)) {
            return false;
        }
        return getId() != null && getId().equals(((Reserva) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Reserva{" +
            "id=" + getId() +
            ", inicio='" + getInicio() + "'" +
            ", duracionMinutos=" + getDuracionMinutos() +
            ", estado='" + getEstado() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", creadaEn='" + getCreadaEn() + "'" +
            "}";
    }
}
