/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

public class Paziente {

    private long idPaz;
    private long idUtente;
    private long idMedico;

    private String nome;
    private String cognome;
    private Date dataN;   // <<< SISTEMATO: java.sql.Date !!!
    private String sesso;
    private String cf;
    private String mail;
    private long nTel;

    // GETTER & SETTER

    public long getIdPaz() {
        return idPaz;
    }

    public void setIdPaz(long idPaz) {
        this.idPaz = idPaz;
    }

    public long getIdUtente() {
        return idUtente;
    }

    public void setIdUtente(long idUtente) {
        this.idUtente = idUtente;
    }

    public long getIdMedico() {
        return idMedico;
    }

    public void setIdMedico(long idMedico) {
        this.idMedico = idMedico;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCognome() {
        return cognome;
    }

    public void setCognome(String cognome) {
        this.cognome = cognome;
    }

    public Date getDataN() {
        return dataN;
    }

    public void setDataN(Date dataN) {
        this.dataN = dataN;
    }

    public String getSesso() {
        return sesso;
    }

    public void setSesso(String sesso) {
        this.sesso = sesso;
    }

    public String getCf() {
        return cf;
    }

    public void setCf(String cf) {
        this.cf = cf;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public long getNTel() {
        return nTel;
    }

    public void setNTel(long nTel) {
        this.nTel = nTel;
    }
}
