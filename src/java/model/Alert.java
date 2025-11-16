/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;

public class Alert {
    private long idAlert;
    private long idPaz;
    private Timestamp data;
    private float riskScore;
    private float soglia;
    private String stato;

    private String nomePaz;
    private String cognomePaz;

    public long getIdAlert() { return idAlert; }
    public void setIdAlert(long idAlert) { this.idAlert = idAlert; }

    public long getIdPaz() { return idPaz; }
    public void setIdPaz(long idPaz) { this.idPaz = idPaz; }

    public Timestamp getData() { return data; }
    public void setData(Timestamp data) { this.data = data; }

    public float getRiskScore() { return riskScore; }
    public void setRiskScore(float riskScore) { this.riskScore = riskScore; }

    public float getSoglia() { return soglia; }
    public void setSoglia(float soglia) { this.soglia = soglia; }

    public String getStato() { return stato; }
    public void setStato(String stato) { this.stato = stato; }

    public String getNomePaz() { return nomePaz; }
    public void setNomePaz(String nomePaz) { this.nomePaz = nomePaz; }

    public String getCognomePaz() { return cognomePaz; }
    public void setCognomePaz(String cognomePaz) { this.cognomePaz = cognomePaz; }
}
