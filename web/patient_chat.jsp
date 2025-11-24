<%-- 
    Document   : patient_chat
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
        <title>Heart Monitor - Chat Paziente</title>
        <link rel="stylesheet" href="<%= ctx%>/css/style.css">
        <link rel="stylesheet" href="<%= ctx%>/css/chat.css">
    </head>
    <body>

        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Chat con il medico</div>
            <div class="spacer"></div>
            <a href="<%= ctx%>/logout" class="toplink">Logout</a>
        </div>

        <div class="layout">
            <div class="sidebar">
                <a href="<%= ctx%>/patient/dashboard">Dashboard</a>
                <a href="<%= ctx%>/patient/questionnaire">Questionario</a>
                <a href="<%= ctx%>/patient/metrics">Storico Parametri</a>
                <a href="<%= ctx%>/patient/chat" class="active">Chat & Televisita</a>
            </div>

            <div class="main">
                <h2>Chat & Televisita</h2>

                <div class="chat-card">
                    <div class="chat-header">
                        <h3 class="chat-title">Chat Sicura</h3>
                        <p class="chat-description">
                            Comunicazione con il Dott.<%= med != null ? (med.getNome() + " " + med.getCognome()) : "il medico"%>
                        </p>
                    </div>

                    <div class="chat-content">
                        <div id="chatMessages" class="chat-messages">
                            <%
                                if (history != null && !history.isEmpty()) {
                                    java.time.LocalDate currentDate = null;
                                    for (ChatMessage m : history) {
                                        java.time.LocalDate d = m.getInviatoIl().toLocalDateTime().toLocalDate();
                                        if (currentDate == null || !currentDate.equals(d)) {
                                            currentDate = d;
                            %>
                            <div class="chat-date-sep"><%= d.toString()%></div>
                            <%
                                }
                                boolean mine = (myUserId != null && m.getIdMittente() == myUserId);
                                java.time.LocalTime t = m.getInviatoIl().toLocalDateTime().toLocalTime();
                                String orario = String.format("%02d:%02d", t.getHour(), t.getMinute());
                                String sender = mine ? "Paziente" : "Medico";
                                pageContext.setAttribute("messageText", m.getTesto());
                            %>
                            <div class="chat-msg-row <%= mine ? "mine" : "other"%>">
                                <div>
                                    <span class="chat-sender"><%= sender%></span>:
                                    <span class="chat-text"><c:out value="${msg:formatMessage(messageText)}" escapeXml="false" /></span>
                                    <div class="chat-time"><%= orario%></div>
                                </div>
                            </div>
                            <%
                                }
                            } else {
                            %>
                            <div style="text-align:center; color:hsl(var(--muted-foreground)); margin-top:20px;">
                                Nessun messaggio. Inizia la conversazione con il tuo medico.
                            </div>
                            <%
                                }
                            %>
                        </div>

                        <div class="chat-input-bar">
                            <input id="chatInput" type="text" class="chat-input" placeholder="Scrivi un messaggio..." />
                            <button type="button" id="sendButton" class="send-btn">
                                💬 Invia
                            </button>
                            <button type="button" class="video-btn" onclick="startOutgoingCall(<%= otherUserId %>)">
                                📹 Avvia video
                            </button>
                        </div>
                    </div>
                </div>
            </div>
        </div>

        <!-- ======= FINESTRA VIDEO GLOBALE (INCLUSA) ======= -->
        <%@ include file="/WEB-INF/includes/video_window.jsp" %>

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
                mineLabel: "Paziente",
                otherLabel: "Medico",
                enterChatOnOpen: false
            });
        </script>

        <!-- ====================== WEBRTC ====================== -->
        <script src="<%= ctx%>/js/webrtc.js"></script>

        <script>
            initTelevisit(<%= myUserId%>);
        </script>
        
    </body>
</html>
