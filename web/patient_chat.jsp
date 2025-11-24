<%-- 
    Document   : patient_chat
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.ChatMessage, model.Paziente, model.Medico" %>

<%!
    private String formatMessageJSP(String text) {
        if (text == null) {
            return "";
        }
        text = text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");
        String urlRegex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)";
        text = text.replaceAll(urlRegex, "<a href=\"$1\" target=\"_blank\" rel=\"noopener noreferrer\">$1</a>");
        return text;
    }
%>

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
                            %>
                            <div class="chat-msg-row <%= mine ? "mine" : "other"%>">
                                <div>
                                    <span class="chat-sender"><%= sender%></span>:
                                    <span class="chat-text"><%= formatMessageJSP(m.getTesto())%></span>
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
                            <button type="button" class="send-btn" onclick="sendMessage()">
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

        <script>
            const MY_ID = <%= myUserId != null ? myUserId : -1%>;
            const OTHER_ID = <%= otherUserId != null ? otherUserId : -1%>;
            const ctx = "<%= ctx%>";

            const proto = (location.protocol === "https:") ? "wss://" : "ws://";
            const wsUrl = proto + location.host + ctx + "/ws/chat/" + MY_ID;

            const chatDiv = document.getElementById("chatMessages");
            const input = document.getElementById("chatInput");

            let ws = new WebSocket(wsUrl);

            ws.onopen = () => {
                scrollBottom();
            };

            ws.onmessage = (event) => {
                const msg = JSON.parse(event.data);
                appendMessage(msg);
            };

            ws.onerror = (e) => {
                console.error("WS error", e);
            };

            function scrollBottom() {
                chatDiv.scrollTop = chatDiv.scrollHeight;
            }

            function formatMessage(text) {
                text = text.replace(/&/g, "&amp;")
                           .replace(/</g, "&lt;")
                           .replace(/>/g, "&gt;")
                           .replace(/"/g, "&quot;");
                const urlRegex = /(https?:\/\/[\w\-._~:\/?#\[\]@!$&'()*+,;=%]+)/g;
                text = text.replace(urlRegex, '<a href="$1" target="_blank" rel="noopener noreferrer">$1</a>');
                return text;
            }

            function appendMessage(msg) {
                const wrapper = document.createElement("div");
                wrapper.className = "chat-msg-row " + (msg.mine ? "mine" : "other");

                const sender = msg.mine ? "Paziente" : "Medico";

                const box = document.createElement("div");

                const senderSpan = document.createElement("span");
                senderSpan.className = "chat-sender";
                senderSpan.textContent = sender;

                const colon = document.createTextNode(": ");

                const textSpan = document.createElement("span");
                textSpan.className = "chat-text";
                textSpan.innerHTML = formatMessage(msg.text);

                const time = document.createElement("div");
                time.className = "chat-time";

                try {
                    const d = new Date(msg.sentAt);
                    const h = String(d.getHours()).padStart(2, "0");
                    const m = String(d.getMinutes()).padStart(2, "0");
                    time.textContent = h + ":" + m;
                } catch (e) {
                    time.textContent = "";
                }

                box.appendChild(senderSpan);
                box.appendChild(colon);
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

            input.addEventListener("keyup", (e) => {
                if (e.key === "Enter")
                    sendMessage();
            });
        </script>

        <!-- ====================== WEBRTC ====================== -->
        <script src="<%= ctx%>/js/webrtc.js"></script>

        <script>
            initTelevisit(<%= myUserId%>);
        </script>
        
    </body>
</html>
