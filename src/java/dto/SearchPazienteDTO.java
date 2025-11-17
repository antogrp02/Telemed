/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dto;

public class SearchPazienteDTO {
    private long idPaz;
    private String nome;
    private String cognome;
    private String cf;

    public SearchPazienteDTO(long idPaz, String nome, String cognome, String cf) {
        this.idPaz = idPaz;
        this.nome = nome;
        this.cognome = cognome;
        this.cf = cf;
    }

    public long getIdPaz() { return idPaz; }
    public String getNome() { return nome; }
    public String getCognome() { return cognome; }
    public String getCf() { return cf; }
}

