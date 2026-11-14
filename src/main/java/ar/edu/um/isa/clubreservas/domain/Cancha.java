package ar.edu.um.isa.clubreservas.domain;

import ar.edu.um.isa.clubreservas.domain.enumeration.Superficie;
import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Cancha que se puede reservar.
 */
@Entity
@Table(name = "cancha")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Cancha implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    @Column(name = "id")
    private Long id;

    @NotNull
    @Size(max = 40)
    @Column(name = "nombre", length = 40, nullable = false, unique = true)
    private String nombre;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "superficie", nullable = false)
    private Superficie superficie;

    @NotNull
    @Column(name = "techada", nullable = false)
    private Boolean techada;

    @NotNull
    @DecimalMin(value = "0")
    @Column(name = "precio_por_hora", precision = 21, scale = 2, nullable = false)
    private BigDecimal precioPorHora;

    @NotNull
    @Column(name = "activa", nullable = false)
    private Boolean activa;

    @ManyToOne(optional = false)
    @NotNull
    private Deporte deporte;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Cancha id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return this.nombre;
    }

    public Cancha nombre(String nombre) {
        this.setNombre(nombre);
        return this;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Superficie getSuperficie() {
        return this.superficie;
    }

    public Cancha superficie(Superficie superficie) {
        this.setSuperficie(superficie);
        return this;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public Boolean getTechada() {
        return this.techada;
    }

    public Cancha techada(Boolean techada) {
        this.setTechada(techada);
        return this;
    }

    public void setTechada(Boolean techada) {
        this.techada = techada;
    }

    public BigDecimal getPrecioPorHora() {
        return this.precioPorHora;
    }

    public Cancha precioPorHora(BigDecimal precioPorHora) {
        this.setPrecioPorHora(precioPorHora);
        return this;
    }

    public void setPrecioPorHora(BigDecimal precioPorHora) {
        this.precioPorHora = precioPorHora;
    }

    public Boolean getActiva() {
        return this.activa;
    }

    public Cancha activa(Boolean activa) {
        this.setActiva(activa);
        return this;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public Deporte getDeporte() {
        return this.deporte;
    }

    public void setDeporte(Deporte deporte) {
        this.deporte = deporte;
    }

    public Cancha deporte(Deporte deporte) {
        this.setDeporte(deporte);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Cancha)) {
            return false;
        }
        return getId() != null && getId().equals(((Cancha) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Cancha{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", superficie='" + getSuperficie() + "'" +
            ", techada='" + getTechada() + "'" +
            ", precioPorHora=" + getPrecioPorHora() +
            ", activa='" + getActiva() + "'" +
            "}";
    }
}
