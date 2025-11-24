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

                            <button class="send-btn" onclick="sendMessage()">
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


        <script>
    const MY_ID = <%= myUserId != null ? myUserId : -1%>;
    const OTHER_ID = <%= otherUserId != null ? otherUserId : -1%>;
    const ctx = "<%= ctx%>";

    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const wsUrl = proto + location.host + ctx + "/ws/chat/" + MY_ID;

    const chatDiv = document.getElementById("chatMessages");
    const input = document.getElementById("chatInput");

    let ws = new WebSocket(wsUrl);

    // ---------------------------
    // ON OPEN → Dichiarare la chat aperta
    // ---------------------------
    ws.onopen = () => {
        scrollBottom();

        // COMUNICHIAMO AL SERVER QUALE CHAT È APERTA
        ws.send(JSON.stringify({
            type: "ENTER_CHAT",
            otherUserId: OTHER_ID
        }));
    };

    // ---------------------------
    // ON MESSAGE → Gestione messaggi + READ_CONFIRM
    // ---------------------------
    ws.onmessage = (ev) => {
        const msg = JSON.parse(ev.data);

        // Evento: nuovo messaggio
        if (msg.text !== undefined) {
            appendMessage(msg);
            return;
        }

        // Evento: READ_CONFIRM
        if (msg.type === "READ_CONFIRM") {
            // Aggiorno badge o notifiche (puoi personalizzarlo)
            console.log("Messaggi del paziente segnati come letti.");

            // Se vuoi aggiornare la dashboard subito:
            // parent.postMessage({updateNotifications: true}, "*");
        }
    };

    ws.onerror = (e) => console.error("WebSocket error:", e);


    // ---------------------------
    // UTILS
    // ---------------------------
    function scrollBottom() {
        chatDiv.scrollTop = chatDiv.scrollHeight;
    }

    function formatMessage(text) {
        text = text.replace(/&/g, "&amp;")
                .replace(/</g, "&lt;")
                .replace(/>/g, "&gt;")
                .replace(/"/g, "&quot;");

        const urlRegex = /(https?:\/\/[\w\-._~:\/?#\[\]@!$&'()*+,;=%]+)/g;
        return text.replace(urlRegex, '<a href="$1" target="_blank">$1</a>');
    }

    function appendMessage(msg) {
        const wrapper = document.createElement("div");
        wrapper.className = "chat-msg-row " + (msg.mine ? "mine" : "other");

        const sender = msg.mine ? "Medico" : "Paziente";

        const box = document.createElement("div");
        const senderSpan = document.createElement("span");
        senderSpan.className = "chat-sender";
        senderSpan.textContent = sender;

        const textSpan = document.createElement("span");
        textSpan.className = "chat-text";
        textSpan.innerHTML = formatMessage(msg.text);

        const time = document.createElement("div");
        time.className = "chat-time";

        try {
            const d = new Date(msg.sentAt);
            time.textContent = d.toLocaleTimeString([], {hour: "2-digit", minute: "2-digit"});
        } catch (e) {
            time.textContent = "";
        }

        box.appendChild(senderSpan);
        box.appendChild(document.createTextNode(": "));
        box.appendChild(textSpan);
        box.appendChild(time);

        wrapper.appendChild(box);
        chatDiv.appendChild(wrapper);
        scrollBottom();
    }

    function sendMessage() {
        const text = input.value.trim();
        if (!text || ws.readyState !== WebSocket.OPEN)
            return;

        ws.send(JSON.stringify({
            destId: OTHER_ID,
            text: text
        }));

        input.value = "";
    }

    input.addEventListener("keyup", e => {
        if (e.key === "Enter")
            sendMessage();
    });

    scrollBottom();
        </script>


        <!-- WEBRTC + CALL LISTENER (un solo WS) -->
        <script src="<%= ctx%>/js/webrtc.js"></script>
        <script>
    initTelevisit(MY_ID, OTHER_ID);
        </script>

    </body>
</html>


