package ar.edu.um.isa.clubreservas.service.dto;

import ar.edu.um.isa.clubreservas.domain.enumeration.Superficie;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * A DTO for the {@link ar.edu.um.isa.clubreservas.domain.Cancha} entity.
 */
@Schema(description = "Cancha que se puede reservar.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class CanchaDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 40)
    private String nombre;

    @NotNull
    private Superficie superficie;

    @NotNull
    private Boolean techada;

    @NotNull
    @DecimalMin(value = "0")
    private BigDecimal precioPorHora;

    @NotNull
    private Boolean activa;

    @NotNull
    private DeporteDTO deporte;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public Superficie getSuperficie() {
        return superficie;
    }

    public void setSuperficie(Superficie superficie) {
        this.superficie = superficie;
    }

    public Boolean getTechada() {
        return techada;
    }

    public void setTechada(Boolean techada) {
        this.techada = techada;
    }

    public BigDecimal getPrecioPorHora() {
        return precioPorHora;
    }

    public void setPrecioPorHora(BigDecimal precioPorHora) {
        this.precioPorHora = precioPorHora;
    }

    public Boolean getActiva() {
        return activa;
    }

    public void setActiva(Boolean activa) {
        this.activa = activa;
    }

    public DeporteDTO getDeporte() {
        return deporte;
    }

    public void setDeporte(DeporteDTO deporte) {
        this.deporte = deporte;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof CanchaDTO)) {
            return false;
        }

        CanchaDTO canchaDTO = (CanchaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, canchaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "CanchaDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", superficie='" + getSuperficie() + "'" +
            ", techada='" + getTechada() + "'" +
            ", precioPorHora=" + getPrecioPorHora() +
            ", activa='" + getActiva() + "'" +
            ", deporte=" + getDeporte() +
            "}";
    }
}
