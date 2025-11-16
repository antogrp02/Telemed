/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Timestamp;

public class Risk {
    private long idPaz;
    private Timestamp data;
    private float riskScore;

    public long getIdPaz() { return idPaz; }
    public void setIdPaz(long idPaz) { this.idPaz = idPaz; }

    public Timestamp getData() { return data; }
    public void setData(Timestamp data) { this.data = data; }

    public float getRiskScore() { return riskScore; }
    public void setRiskScore(float riskScore) { this.riskScore = riskScore; }
}
