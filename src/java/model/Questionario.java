/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package model;

import java.sql.Date;

public class Questionario {
    private long idQuestionario;
    private long idPaziente;
    private Date data;
    private short dispnea;
    private short edema;
    private short fatica;
    private short ortopnea;
    private short adl;
    private short vertigini;

    public long getIdQuestionario() { return idQuestionario; }
    public void setIdQuestionario(long idQuestionario) { this.idQuestionario = idQuestionario; }

    public long getIdPaziente() { return idPaziente; }
    public void setIdPaziente(long idPaziente) { this.idPaziente = idPaziente; }

    public Date getData() { return data; }
    public void setData(Date data) { this.data = data; }

    public short getDispnea() { return dispnea; }
    public void setDispnea(short dispnea) { this.dispnea = dispnea; }

    public short getEdema() { return edema; }
    public void setEdema(short edema) { this.edema = edema; }

    public short getFatica() { return fatica; }
    public void setFatica(short fatica) { this.fatica = fatica; }

    public short getOrtopnea() { return ortopnea; }
    public void setOrtopnea(short ortopnea) { this.ortopnea = ortopnea; }

    public short getAdl() { return adl; }
    public void setAdl(short adl) { this.adl = adl; }

    public short getVertigini() { return vertigini; }
    public void setVertigini(short vertigini) { this.vertigini = vertigini; }
}
