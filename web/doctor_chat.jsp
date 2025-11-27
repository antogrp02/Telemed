<%--
    Document   : doctor_chat
    Created on : 20 nov 2025, 11:36:44
    Author     : Antonio
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.ChatMessage, model.Paziente, model.Medico, model.Appuntamento" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="msg" uri="http://telemed/functions" %>

<%
    Paziente paz = (Paziente) request.getAttribute("paziente");
    Medico med = (Medico) request.getAttribute("medico");
    List<ChatMessage> history = (List<ChatMessage>) request.getAttribute("history");
    List<Appuntamento> appuntamenti = (List<Appuntamento>) request.getAttribute("appuntamenti");

    Long myUserId = (Long) request.getAttribute("myUserId");
    Long otherUserId = (Long) request.getAttribute("otherUserId");
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Heart Monitor - Chat Medico</title>
        <link rel="stylesheet" href="<%= ctx%>/css/style.css">
        <link rel="stylesheet" href="<%= ctx%>/css/chat.css">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

    </head>

    <body>

        <%@ include file="/WEB-INF/includes/video_window.jsp" %>
        
        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Chat con il paziente</div>
            <div class="spacer"></div>
            <a href="<%= ctx%>/logout" class="toplink">Logout</a>
        </div>

        <div class="layout">

            <div class="sidebar">
                <a href="<%= ctx%>/doctor/dashboard">Pazienti</a>
                <a href="<%= ctx%>/doctor/appointments">Appuntamenti</a>
                <a href="<%= ctx%>/doctor/alerts">Alert</a>
                <a href="<%= ctx%>/doctor/chat?id=<%= paz.getIdPaz()%>" class="active">Chat</a>
            </div>

            <div class="main">

                <h2>Chat con il paziente</h2>
                <p>
                    <strong><%= paz.getNome() + " " + paz.getCognome()%></strong>
                    — CF: <%= paz.getCf()%>
                </p>

                <!-- ⭐ LAYOUT A 2 COLONNE -->
                <div class="chat-layout">

                    <!-- COLONNA SINISTRA (CHAT) -->
                    <div class="chat-column">

                        <div class="chat-card">
                            <div class="chat-header">
                                <h3 class="chat-title">Chat Sicura</h3>
                                <p class="chat-description">Conversazione privata con il paziente</p>
                            </div>

                            <div class="chat-content">

                                <div id="chatMessages" class="chat-messages">
                                    <% if (history != null && !history.isEmpty()) {
                                            java.time.LocalDate currentDate = null;
                                            for (ChatMessage m : history) {

                                                java.time.LocalDate d = m.getInviatoIl().toLocalDateTime().toLocalDate();
                                                if (currentDate == null || !currentDate.equals(d)) {
                                                    currentDate = d;%>
                                    <div class="chat-date-sep"><%= d%></div>
                                    <% }

                                        boolean mine = (myUserId != null && m.getIdMittente() == myUserId);
                                        String sender = mine ? "Medico" : "Paziente";
                                        java.time.LocalTime t = m.getInviatoIl().toLocalDateTime().toLocalTime();
                                        String orario = String.format("%02d:%02d", t.getHour(), t.getMinute());
                                        pageContext.setAttribute("messageText", m.getTesto());
                                    %>

                                    <div class="chat-msg-row <%= mine ? "mine" : "other"%>">
                                        <div>
                                            <span class="chat-sender"><%= sender%></span>
                                            <div class="chat-text">
                                                <c:out value="${msg:formatMessage(messageText)}" escapeXml="false" />
                                            </div>
                                            <div class="chat-time"><%= orario%></div>
                                        </div>
                                    </div>

                                    <% }
                                    } else { %>
                                    <div style="text-align:center; color:#666; margin-top:20px;">
                                        Nessun messaggio. Inizia la conversazione con il paziente.
                                    </div>
                                    <% }%>
                                </div>

                                <div class="chat-input-bar">
                                    <input id="chatInput" class="chat-input" placeholder="Scrivi un messaggio..." />

                                    <button id="sendButton" class="send-btn">
                                        💬 Invia
                                    </button>

                                    <button class="video-btn" onclick="startOutgoingCall(<%= otherUserId%>)">
                                        📹 Avvia video
                                    </button>
                                </div>
                            </div>
                        </div>
                    </div>

                    <!-- COLONNA DESTRA (APPUNTAMENTI) -->
                    <div class="appointments-column">

                        <div class="appointments-card">

                            <div class="appointments-header">
                                <h3 class="appointments-title">Prossimi appuntamenti</h3>
                                <p class="appointments-subtitle">Programmazione del paziente</p>
                            </div>

                            <div class="appointments-content">

                                <div class="appointments-list">
                                    <% if (appuntamenti == null || appuntamenti.isEmpty()) { %>

                                    <div class="appointments-empty">Nessun appuntamento futuro</div>

                                    <% } else {
                                        for (Appuntamento a : appuntamenti) {

                                            java.time.LocalDate d = a.getDataOra().toLocalDateTime().toLocalDate();
                                            java.time.LocalTime t = a.getDataOra().toLocalDateTime().toLocalTime();
                                            String dataStr = String.format("%02d/%02d", d.getDayOfMonth(), d.getMonthValue());
                                            String oraStr = String.format("%02d:%02d", t.getHour(), t.getMinute());
                                    %>

                                    <div class="appointment-row">
                                        <span class="appointment-type"><%= a.getTipo()%></span>
                                        <span class="appointment-date"><%= dataStr%> <%= oraStr%></span>
                                    </div>

                                    <% }
                                        }%>
                                </div>

                                <!-- FORM SOLO PER IL MEDICO -->
                                <form method="post" action="<%= ctx%>/doctor/book-appointment" class="appointments-form">

                                    <input type="hidden" name="idPaziente" value="<%= paz.getIdPaz()%>" />

                                    <div class="appointments-form-row">
                                        <label>Tipo appuntamento:</label>
                                        <select name="tipo">
                                            <option>Televisita</option>
                                            <option>Controllo esami</option>
                                            <option>Follow-up</option>
                                        </select>
                                    </div>

                                    <div class="appointments-form-row">
                                        <label>Data:</label>
                                        <!-- Data impostata da JS per accettare solo da DOMANI in poi -->
                                        <input type="date"
                                               id="appointmentDate"
                                               name="data"
                                               required />
                                    </div>

                                    <div class="appointments-form-row">
                                        <label>Ora:</label>
                                        <input type="time" name="ora" required />
                                    </div>

                                    <button class="appointments-submit-btn">📅 Prenota</button>
                                </form>

                            </div>

                        </div>

                    </div>

                </div><!-- END layout a 2 colonne -->
            </div>
        </div>

        <script src="<%= ctx%>/js/chat.js"></script>
        <script>
                                        const MY_ID = <%= myUserId%>;
                                        const OTHER_ID = <%= otherUserId%>;

                                        initChat({
                                            myId: MY_ID,
                                            otherId: OTHER_ID,
                                            contextPath: "<%= ctx%>",
                                            chatContainerSelector: "#chatMessages",
                                            inputSelector: "#chatInput",
                                            sendButtonSelector: "#sendButton",
                                            mineLabel: "Medico",
                                            otherLabel: "Paziente"
                                        });
        </script>

        <script src="<%= ctx%>/js/webrtc.js"></script>
        <script>
                                        initTelevisit(MY_ID);

                                        // 👉 Imposta min della data a DOMANI lato client, così evitiamo qualsiasi problema di formato
                                        document.addEventListener("DOMContentLoaded", function () {
                                            const input = document.getElementById("appointmentDate");
                                            if (!input)
                                                return;

                                            const tomorrow = new Date();
                                            tomorrow.setDate(tomorrow.getDate() + 1);

                                            const minDate = tomorrow.toISOString().slice(0, 10);  // YYYY-MM-DD

                                            input.min = minDate;

                                            console.log("[DoctorChat] minDate correttamente impostata a:", minDate);
                                        });

        </script>

    </body>
</html>
