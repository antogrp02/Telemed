/*
 * Appuntamento futuro tra medico e paziente
 */
package model;

import java.sql.Timestamp;

public class Appuntamento {

    private long id;
    private long idPaziente;
    private long idMedico;
    private String tipo;
    private Timestamp dataOra;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getIdPaziente() {
        return idPaziente;
    }

    public void setIdPaziente(long idPaziente) {
        this.idPaziente = idPaziente;
    }

    public long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(long idMedico) {
        this.idMedico = idMedico;
    }

    public String getTipo() {
        return tipo;
    }

    public void setTipo(String tipo) {
        this.tipo = tipo;
    }

    public Timestamp getDataOra() {
        return dataOra;
    }

    public void setDataOra(Timestamp dataOra) {
        this.dataOra = dataOra;
    }
}
