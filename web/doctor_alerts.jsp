<%-- 
    Document   : doctor_alerts
    Created on : 14 nov 2025, 18:33:56
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Alert, utils.RiskEvaluator" %>
<%
    List<Alert> alerts = (List<Alert>) request.getAttribute("alerts");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Heart Monitor - Alert</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body>

<div class="topbar">
  <div class="logo">Heart Monitor</div>
  <div class="subtitle">Alert attivi</div>
  <div class="spacer"></div>
  <a href="../logout" class="toplink">Logout</a>
</div>

<div class="layout">
  <div class="sidebar">
    <a href="dashboard">Pazienti</a>
    <a href="alerts" class="active">Alert</a>
  </div>

  <div class="main">
    <h2>Alert attivi</h2>

    <table class="table">
      <thead>
        <tr>
          <th>Paziente</th>
          <th>Data</th>
          <th>Rischio</th>
        </tr>
      </thead>
      <tbody>
      <%
        if (alerts != null) {
          for (Alert a : alerts) {
            float rs = a.getRiskScore();
      %>
        <tr>
          <td><%= a.getNomePaz() %> <%= a.getCognomePaz() %></td>
          <td><%= a.getData() %></td>
          <td><span class="risk-badge <%= RiskEvaluator.getCssClass(rs) %>">
                <%= String.format("%.0f%%", rs * 100) %>
              </span></td>
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

