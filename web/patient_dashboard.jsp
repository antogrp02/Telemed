<%-- 
    Document   : patient_dashboard
    Created on : 14 nov 2025, 18:29:51
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="model.Risk, model.Parametri, utils.RiskEvaluator" %>
<%
    Risk lastRisk = (Risk) request.getAttribute("lastRisk");
    Parametri lastParams = (Parametri) request.getAttribute("lastParams");
    float riskScore = lastRisk != null ? lastRisk.getRiskScore() : 0f;
%>

<%
    Boolean qok = (Boolean) session.getAttribute("questionnaire_ok");
    boolean showPopup = qok != null && qok;

    // lo rimuoviamo subito così il popup NON ricompare al refresh
    if (showPopup) {
        session.removeAttribute("questionnaire_ok");
    }
%>

<!DOCTYPE html>
<html>
<head>
  <title>Heart Monitor - Paziente</title>
  <link rel="stylesheet" href="../css/style.css">
  <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
  <script>
    function closeQModal() {
        document.getElementById("qModal").style.display = "none";
    }
  </script>

</head>
<body>

<div class="topbar">
  <div class="logo">Heart Monitor</div>
  <div class="subtitle">Telemonitoraggio HF · Paziente</div>
  <div class="spacer"></div>
  <a href="../logout" class="toplink">Logout</a>
</div>

<div class="layout">
  <div class="sidebar">
    <a class="active" href="dashboard">Dashboard</a>
    <a href="questionnaire">Questionario</a>
    <a href="metrics">Storico Parametri</a>
    <a href="chat">Chat & Televisita</a>
  </div>


  <div class="main">
    <h2>Dashboard Paziente</h2>

    <div class="grid-4">
      <div class="card">
        <div class="card-title">HR (riposo)</div>
        <div class="card-value">
          <%= lastParams != null ? String.format("%.0f bpm", lastParams.getRhrCurr()) : "--" %>
        </div>
        <div class="card-sub">
          trend 7gg:
          <%= lastParams != null ? String.format("%.1f", lastParams.getRhr7d()) : "-" %>
        </div>
      </div>

      <div class="card">
        <div class="card-title">SpO₂</div>
        <div class="card-value">
          <%= lastParams != null ? String.format("%.0f%%", lastParams.getSpo2Curr()) : "--" %>
        </div>
        <div class="card-sub">
          trend 7gg:
          <%= lastParams != null ? String.format("%.1f", lastParams.getSpo27d()) : "-" %>
        </div>
      </div>

      <div class="card">
        <div class="card-title">Peso</div>
        <div class="card-value">
          <%= lastParams != null ? String.format("%.1f kg", lastParams.getWeightCurr()) : "--" %>
        </div>
        <div class="card-sub">
          Δ 7gg:
          <%= lastParams != null ? String.format("%.1f", lastParams.getWeight7d()) : "-" %>
        </div>
      </div>

      <div class="card">
        <div class="card-title">Passi</div>
        <div class="card-value">
          <%= lastParams != null ? String.format("%.0f", lastParams.getStepsCurr()) : "--" %>
        </div>
        <div class="card-sub">
          Δ 7gg:
          <%= lastParams != null ? String.format("%.0f", lastParams.getSteps7d()) : "-" %>
        </div>
      </div>
    </div>

    <div class="card full">
      <div class="card-header-row">
        <div class="card-title">Stato di rischio</div>
        <div class="card-sub">
          Ultima predizione:
          <%= lastRisk != null ? lastRisk.getData() : "N/D" %>
        </div>
      </div>

      <div class="risk-badge <%= RiskEvaluator.getCssClass(riskScore) %>">
        Rischio: <%= String.format("%.0f%%", riskScore * 100) %> (
        <%= RiskEvaluator.getLevel(riskScore) %> )
      </div>

      <canvas id="riskChart" height="90"></canvas>
    </div>
  </div>
</div>

<!-- Passaggio del JSON alla pagina -->
<script>
    const riskData = <%= new com.google.gson.Gson().toJson(request.getAttribute("storicoRisk")) %>;
</script>
<script src="../js/charts.js"></script>

<!-- Popup conferma questionario -->
<div id="qModal" class="modal-overlay" style="display: <%= showPopup ? "flex" : "none" %>;">
  <div class="modal-box">
    <h3>Questionario inviato</h3>
    <p>I sintomi di oggi sono stati registrati correttamente.</p>
    <button type="button" class="btn-primary" onclick="closeQModal()">OK</button>
  </div>
</div>
</body>
</html>

