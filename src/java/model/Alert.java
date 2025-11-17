/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;

public class Alert {

    private long idAlert;
    private long idPaz;
    private Timestamp riskData;
    private long idMedico;

    private String messaggio;

    private boolean visto;
    private boolean archiviato;

    public long getIdAlert() { return idAlert; }
    public void setIdAlert(long idAlert) { this.idAlert = idAlert; }

    public long getIdPaz() { return idPaz; }
    public void setIdPaz(long idPaz) { this.idPaz = idPaz; }

    public Timestamp getRiskData() { return riskData; }
    public void setRiskData(Timestamp riskData) { this.riskData = riskData; }

    public long getIdMedico() { return idMedico; }
    public void setIdMedico(long idMedico) { this.idMedico = idMedico; }

    public String getMessaggio() { return messaggio; }
    public void setMessaggio(String messaggio) { this.messaggio = messaggio; }

    public boolean isVisto() { return visto; }
    public void setVisto(boolean visto) { this.visto = visto; }

    public boolean isArchiviato() { return archiviato; }
    public void setArchiviato(boolean archiviato) { this.archiviato = archiviato; }
}

