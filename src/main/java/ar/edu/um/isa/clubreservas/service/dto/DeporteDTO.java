package ar.edu.um.isa.clubreservas.service.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * A DTO for the {@link ar.edu.um.isa.clubreservas.domain.Deporte} entity.
 */
@Schema(description = "Deporte que se practica en el club (futbol 5, padel, tenis...).")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class DeporteDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(max = 40)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    @Min(value = 1)
    @Max(value = 11)
    private Integer jugadoresPorEquipo;

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

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getJugadoresPorEquipo() {
        return jugadoresPorEquipo;
    }

    public void setJugadoresPorEquipo(Integer jugadoresPorEquipo) {
        this.jugadoresPorEquipo = jugadoresPorEquipo;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DeporteDTO)) {
            return false;
        }

        DeporteDTO deporteDTO = (DeporteDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, deporteDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "DeporteDTO{" +
            "id=" + getId() +
            ", nombre='" + getNombre() + "'" +
            ", descripcion='" + getDescripcion() + "'" +
            ", jugadoresPorEquipo=" + getJugadoresPorEquipo() +
            "}";
    }
}
