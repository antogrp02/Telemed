<%-- 
    Document   : doctor_dashboard
    Created on : 14 nov 2025, 18:33:08
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Paziente, model.Risk, utils.RiskEvaluator" %>
<%
    List<Paziente> pazienti = (List<Paziente>) request.getAttribute("pazienti");
    Map<Long, Risk> lastRiskByPaz = (Map<Long, Risk>) request.getAttribute("lastRiskByPaz");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Heart Monitor - Medico</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>

<div class="topbar">
  <div class="logo">Heart Monitor</div>
  <div class="subtitle">Dashboard Medico</div>
  <div class="spacer"></div>
  <a href="../logout" class="toplink">Logout</a>
</div>

<div class="layout">
  <div class="sidebar">
    <a href="dashboard" class="active">Pazienti</a>
    <a href="alerts">Alert</a>
  </div>

  <div class="main">
    <h2>Pazienti in carico</h2>

    <table class="table">
      <thead>
        <tr>
          <th>Paziente</th>
          <th>Ultima predizione</th>
          <th>Rischio</th>
        </tr>
      </thead>
      <tbody>
      <%
        if (pazienti != null) {
          for (Paziente p : pazienti) {
            Risk r = lastRiskByPaz.get(p.getIdPaz());
            float rs = (r != null) ? r.getRiskScore() : 0f;
      %>
        <tr>
          <td><%= p.getNome() %> <%= p.getCognome() %></td>
          <td><%= (r != null) ? r.getData() : "N/D" %></td>
          <td>
            <span class="risk-badge <%= RiskEvaluator.getCssClass(rs) %>">
              <%= String.format("%.0f%%", rs * 100) %>
            </span>
          </td>
        </tr>
      <%
          }
        }
      %>
      </tbody>
    </table>
  </div>
</div>

</body>
</html>

