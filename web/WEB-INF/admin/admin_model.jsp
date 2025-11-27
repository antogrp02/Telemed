<%-- 
    Document   : admin_model
    Created on : 14 nov 2025, 18:34:27
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String version = (String) request.getAttribute("modelVersion");
    String auc = (String) request.getAttribute("modelAuc");
    String lastRetrain = (String) request.getAttribute("lastRetrain");
%>
<!DOCTYPE html>
<html>
<head>
  <title>Heart Monitor - Admin Modello</title>
  <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">

</head>
<body>

<div class="topbar">
  <div class="logo">Heart Monitor</div>
  <div class="subtitle">Admin Modello ML</div>
  <div class="spacer"></div>
  <a href="<%= request.getContextPath() %>/logout" class="toplink">Logout</a>
</div>

<div class="layout">
  <div class="sidebar">
    <a href="model" class="active">Modello ML</a>
  </div>

  <div class="main">
    <h2>Stato modello di predizione</h2>

    <div class="card full">
      <div class="card-title">Dettagli modello</div>
      <p>Versione: <strong><%= version %></strong></p>
      <p>AUC: <strong><%= auc %></strong></p>
      <p>Ultimo retraining: <strong><%= lastRetrain %></strong></p>

      <form method="post" action="model">
        <button type="submit" class="btn-primary">Avvia retraining (dummy)</button>
      </form>
    </div>
  </div>
</div>

</body>
</html>

