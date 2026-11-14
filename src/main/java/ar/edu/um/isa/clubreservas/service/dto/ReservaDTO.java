package ar.edu.um.isa.clubreservas.service.dto;

import ar.edu.um.isa.clubreservas.domain.enumeration.EstadoReserva;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link ar.edu.um.isa.clubreservas.domain.Reserva} entity.
 */
@Schema(description = "Reserva de una cancha por parte de un socio.")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class ReservaDTO implements Serializable {

    private Long id;

    @NotNull
    private Instant inicio;

    @NotNull
    @Min(value = 60)
    @Max(value = 120)
    private Integer duracionMinutos;

    @NotNull
    private EstadoReserva estado;

    @Size(max = 255)
    private String observaciones;

    private Instant creadaEn;

    @NotNull
    private CanchaDTO cancha;

    private UserDTO usuario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getInicio() {
        return inicio;
    }

    public void setInicio(Instant inicio) {
        this.inicio = inicio;
    }

    public Integer getDuracionMinutos() {
        return duracionMinutos;
    }

    public void setDuracionMinutos(Integer duracionMinutos) {
        this.duracionMinutos = duracionMinutos;
    }

    public EstadoReserva getEstado() {
        return estado;
    }

    public void setEstado(EstadoReserva estado) {
        this.estado = estado;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public Instant getCreadaEn() {
        return creadaEn;
    }

    public void setCreadaEn(Instant creadaEn) {
        this.creadaEn = creadaEn;
    }

    public CanchaDTO getCancha() {
        return cancha;
    }

    public void setCancha(CanchaDTO cancha) {
        this.cancha = cancha;
    }

    public UserDTO getUsuario() {
        return usuario;
    }

    public void setUsuario(UserDTO usuario) {
        this.usuario = usuario;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ReservaDTO)) {
            return false;
        }

        ReservaDTO reservaDTO = (ReservaDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, reservaDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "ReservaDTO{" +
            "id=" + getId() +
            ", inicio='" + getInicio() + "'" +
            ", duracionMinutos=" + getDuracionMinutos() +
            ", estado='" + getEstado() + "'" +
            ", observaciones='" + getObservaciones() + "'" +
            ", creadaEn='" + getCreadaEn() + "'" +
            ", cancha=" + getCancha() +
            ", usuario=" + getUsuario() +
            "}";
    }
}
