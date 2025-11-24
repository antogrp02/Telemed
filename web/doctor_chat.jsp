<%--
    Document   : doctor_chat
    Created on : 20 nov 2025, 11:36:44
    Author     : Antonio
--%>
<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.ChatMessage, model.Paziente, model.Medico" %>
<%@ taglib prefix="c" uri="http://java.sun.com/jsp/jstl/core" %>
<%@ taglib prefix="msg" uri="http://telemed/functions" %>

<%
    Paziente paz = (Paziente) request.getAttribute("paziente");
    Medico med = (Medico) request.getAttribute("medico");
    List<ChatMessage> history = (List<ChatMessage>) request.getAttribute("history");
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
                <a href="<%= ctx%>/doctor/alerts">Alert</a>
                <a href="<%= ctx%>/doctor/chat?id=<%= paz != null ? paz.getIdPaz() : 0%>" class="active">Chat</a>
            </div>

            <div class="main">

                <h2>Chat con il paziente</h2>
                <p>
                    <strong><%= paz != null ? paz.getNome() + " " + paz.getCognome() : "Paziente"%></strong>
                    — CF: <%= paz != null ? paz.getCf() : "N/D"%>
                </p>

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
                            <%      }

                                boolean mine = (myUserId != null && m.getIdMittente() == myUserId);
                                String sender = mine ? "Medico" : "Paziente";
                                java.time.LocalTime t = m.getInviatoIl().toLocalDateTime().toLocalTime();
                                String orario = String.format("%02d:%02d", t.getHour(), t.getMinute());
                                pageContext.setAttribute("messageText", m.getTesto());
                            %>

                            <div class="chat-msg-row <%= mine ? "mine" : "other"%>">
                                <div>
                                    <span class="chat-sender"><%= sender%></span>:
                                    <span class="chat-text"><c:out value="${msg:formatMessage(messageText)}" escapeXml="false" /></span>
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

                            <!-- QUI LA MODIFICA IMPORTANTE -->
                            <button class="video-btn" onclick="startOutgoingCall(<%= otherUserId%>)">
                                📹 Avvia video
                            </button>
                        </div>
                    </div>

                </div>
            </div>
        </div>


        <script src="<%= ctx%>/js/chat.js"></script>
        <script>
            const MY_ID = <%= myUserId != null ? myUserId : -1%>;
            const OTHER_ID = <%= otherUserId != null ? otherUserId : -1%>;

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

        <!-- WEBRTC + CALL LISTENER (un solo WS) -->
        <script src="<%= ctx%>/js/webrtc.js"></script>
        <script>
            initTelevisit(MY_ID);
        </script>

    </body>
</html>


