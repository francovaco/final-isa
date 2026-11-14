package ar.edu.um.isa.clubreservas.service;

/**
 * Se lanza cuando una reserva no cumple una regla de negocio del club.
 * El {@code codigo} identifica la regla (ej. "superposicion") y la capa web lo
 * devuelve al cliente como errorKey de un 400 Bad Request.
 */
public class ReglaReservaException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    private final String codigo;

    public ReglaReservaException(String codigo, String mensaje) {
        super(mensaje);
        this.codigo = codigo;
    }

    public String getCodigo() {
        return codigo;
    }
}
