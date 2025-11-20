/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;

public class ChatMessage {
    private long idMsg;
    private long idMittente;
    private long idDestinatario;
    private Timestamp inviatoIl;
    private String testo;

    public long getIdMsg() {
        return idMsg;
    }

    public void setIdMsg(long idMsg) {
        this.idMsg = idMsg;
    }

    public long getIdMittente() {
        return idMittente;
    }

    public void setIdMittente(long idMittente) {
        this.idMittente = idMittente;
    }

    public long getIdDestinatario() {
        return idDestinatario;
    }

    public void setIdDestinatario(long idDestinatario) {
        this.idDestinatario = idDestinatario;
    }

    public Timestamp getInviatoIl() {
        return inviatoIl;
    }

    public void setInviatoIl(Timestamp inviatoIl) {
        this.inviatoIl = inviatoIl;
    }

    public String getTesto() {
        return testo;
    }

    public void setTesto(String testo) {
        this.testo = testo;
    }
}
