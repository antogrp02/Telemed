<%-- 
    Document   : appointments_box
    Created on : 24 nov 2025, 19:10:34
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Appuntamento, model.Paziente" %>

<%
    List<Appuntamento> appuntamenti = (List<Appuntamento>) request.getAttribute("appuntamenti");
    Paziente paz = (Paziente) request.getAttribute("paziente");

    boolean isMedico = false;
    Object roleObj = session.getAttribute("role");
    if (roleObj instanceof Integer && ((Integer) roleObj) == 1) {
        isMedico = true;
    }
%>

<div class="appointments-card">
    <div class="appointments-header">
        <h3 class="appointments-title">Prossimi appuntamenti</h3>
        <%
            if (paz != null) {
        %>
        <p class="appointments-subtitle">
            <%= paz.getNome() %> <%= paz.getCognome() %>
        </p>
        <%
            }
        %>
    </div>

    <div class="appointments-content">
        <%
            if (appuntamenti == null || appuntamenti.isEmpty()) {
        %>
        <p class="appointments-empty">Nessun appuntamento futuro</p>
        <%
            } else {
                for (Appuntamento a : appuntamenti) {
                    java.time.LocalDate d = a.getDataOra().toLocalDateTime().toLocalDate();
                    java.time.LocalTime t = a.getDataOra().toLocalDateTime().toLocalTime();
                    String dateStr = String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue());
                    String timeStr = String.format("%02d:%02d", t.getHour(), t.getMinute());
        %>
        <div class="appointment-row">
            <span class="appointment-type"><%= a.getTipo() %></span>
            <span class="appointment-date"><%= dateStr %> <%= timeStr %></span>
        </div>
        <%
                }
            }
        %>
    </div>

    <%
        // Solo il medico può prenotare
        if (isMedico && paz != null) {
    %>
    <form class="appointments-form"
          method="post"
          action="<%= request.getContextPath() %>/doctor/book-appointment">
        <input type="hidden" name="idPaziente" value="<%= paz.getIdPaz() %>" />

        <div class="appointments-form-row">
            <label for="tipo">Tipo appuntamento</label>
            <select id="tipo" name="tipo" required>
                <option value="Televisita">Televisita</option>
                <option value="Controllo esami">Controllo esami</option>
                <option value="Follow-up">Follow-up</option>
            </select>
        </div>

        <div class="appointments-form-row">
            <label for="data">Data</label>
            <input type="date" id="data" name="data" required />
        </div>

        <div class="appointments-form-row">
            <label for="ora">Ora</label>
            <input type="time" id="ora" name="ora" required />
        </div>

        <button type="submit" class="appointments-submit-btn">
            📅 Prenota
        </button>
    </form>
    <%
        }
    %>
</div>
