<%-- 
    Document   : questionnaire
    Created on : 14 nov 2025, 18:30:48
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Questionario Sintomi</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

    <script>
        let formSubmitted = false;

        function preventDoubleSubmit(form) {
            if (formSubmitted) {
                return false; // blocca ulteriori invii
            }
            formSubmitted = true;

            const btn = document.getElementById("submitBtn");
            if (btn) {
                btn.disabled = true;
                btn.classList.add("btn-disabled");
                btn.innerText = "Invio…";
            }
            return true;
        }
    </script>

    <style>
        /* stile per il pulsante disabilitato, coerente con la UI */
        .btn-disabled {
            opacity: 0.6;
            cursor: not-allowed !important;
        }
    </style>
</head>
<body>

<div class="topbar">
    <div class="logo">Heart Monitor</div>
    <div class="subtitle">Questionario giornaliero</div>
    <div class="spacer"></div>
    <a href="<%= request.getContextPath() %>/logout" class="toplink">Logout</a>
</div>

<div class="layout">

    <div class="sidebar">
        <a href="dashboard">Dashboard</a>
        <a class="active" href="questionnaire">Questionario</a>
        <a href="metrics">Storico Parametri</a>
        <a href="chat">Chat & Televisita</a>
    </div>


    <div class="main">
    <%
      Boolean alreadyToday = (Boolean) request.getAttribute("alreadyToday");
      if (alreadyToday != null && alreadyToday) {
    %>
        <div class="alert warning">
            Hai già compilato il questionario per oggi.
        </div>
    <%
      }
    %>
        <h2>Compila i sintomi di oggi</h2>

        <form action="<%= request.getContextPath() %>/patient/questionnaire"
              method="post"
              class="form"
              onsubmit="return preventDoubleSubmit(this);">

            <!-- DISPNEA -->
            <label>Dispnea</label>
            <select name="dispnea" required>
                <option value="0">Nessuna</option>
                <option value="1">Lieve</option>
                <option value="2">Moderata</option>
                <option value="3">Grave</option>
            </select>

            <!-- EDEMA -->
            <label>Edema caviglie</label>
            <select name="edema" required>
                <option value="0">Assente</option>
                <option value="1">Presente</option>
            </select>

            <!-- FATICA -->
            <label>Astenia / Fatica</label>
            <select name="fatica" required>
                <option value="0">Nessuna</option>
                <option value="1">Lieve</option>
                <option value="2">Moderata</option>
                <option value="3">Severa</option>
            </select>

            <!-- ORTOPNEA -->
            <label>Ortopnea</label>
            <select name="ortopnea" required>
                <option value="0">No</option>
                <option value="1">Sì</option>
            </select>

            <!-- ADL -->
            <label>Limitazioni ADL</label>
            <select name="adl" required>
                <option value="0">Nessuna</option>
                <option value="1">Lieve</option>
                <option value="2">Moderata</option>
                <option value="3">Grave</option>
            </select>

            <!-- VERTIGINI -->
            <label>Vertigini</label>
            <select name="vertigini" required>
                <option value="0">No</option>
                <option value="1">Sì</option>
            </select>

            <button id="submitBtn" type="submit" class="btn-primary">Invia</button>
        </form>

    </div>
</div>
<%@ include file="/WEB-INF/includes/video_window.jsp" %>

<script src="<%= request.getContextPath() %>/js/webrtc.js"></script>
<script>
    const MY_ID = <%= session.getAttribute("id_utente") %>;
    initTelevisit(MY_ID);
</script>



</body>
</html>
