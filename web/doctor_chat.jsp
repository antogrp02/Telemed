<%-- 
    Document   : doctor_chat
    Created on : 20 nov 2025, 11:36:44
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
        <title>Heart Monitor - Chat Medico</title>
        <link rel="stylesheet" href="<%= ctx%>/css/style.css">
        
                <style>
            :root {
                --background: 0 0% 100%;
                --foreground: 222.2 84% 4.9%;
                --card: 0 0% 100%;
                --card-foreground: 222.2 84% 4.9%;
                --secondary: 210 40% 96.1%;
                --border: 214.3 31.8% 91.4%;
                --muted-foreground: 215.4 16.3% 46.9%;
                --primary: 221.2 83.2% 53.3%;
            }

            .chat-card {
                background: hsl(var(--card));
                border: 1px solid hsl(var(--border));
                border-radius: 16px;
                box-shadow: 0 1px 3px 0 rgb(0 0 0 / 0.1);
                overflow: hidden;
            }

            .chat-header {
                padding: 24px 24px 0;
            }

            .chat-title {
                font-size: 20px;
                font-weight: 600;
                color: hsl(var(--foreground));
                margin: 0 0 4px 0;
            }

            .chat-description {
                font-size: 14px;
                color: hsl(var(--muted-foreground));
                margin: 0;
            }

            .chat-content {
                padding: 24px;
            }

            .chat-messages {
                height: 256px;
                border: 1px solid hsl(var(--border));
                border-radius: 12px;
                padding: 12px;
                background: hsl(var(--secondary));
                overflow-y: auto;
                font-size: 14px;
                margin-bottom: 12px;
            }

            .chat-msg-row {
                margin-bottom: 8px;
            }

            .chat-msg-row.mine {
                text-align: right;
            }

            .chat-msg-row.other {
                text-align: left;
            }

            .chat-sender {
                font-weight: 500;
                color: hsl(var(--foreground));
                margin-right: 4px;
            }

            .chat-text {
                color: hsl(var(--foreground));
                word-wrap: break-word;
                word-break: break-word;
                white-space: pre-wrap;
                overflow-wrap: break-word;
            }

            .chat-text a {
                color: hsl(var(--primary));
                text-decoration: underline;
            }

            .chat-time {
                font-size: 11px;
                color: hsl(var(--muted-foreground));
                margin-top: 2px;
            }

            .chat-date-sep {
                text-align: center;
                font-size: 12px;
                color: hsl(var(--muted-foreground));
                margin: 16px 0;
            }

            .chat-input-bar {
                display: flex;
                align-items: center;
                gap: 8px;
            }

            .chat-input {
                flex: 1;
                padding: 8px 12px;
                font-size: 14px;
                border-radius: 6px;
                border: 1px solid hsl(var(--border));
                background: hsl(var(--background));
                color: hsl(var(--foreground));
            }

            .chat-input:focus {
                outline: none;
                border-color: hsl(var(--primary));
                box-shadow: 0 0 0 3px hsl(var(--primary) / 0.1);
            }

            .chat-input::placeholder {
                color: hsl(var(--muted-foreground));
            }

            .send-btn {
                padding: 8px 16px;
                border-radius: 6px;
                border: none;
                cursor: pointer;
                background: hsl(var(--primary));
                color: white;
                font-size: 14px;
                font-weight: 500;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .send-btn:hover {
                filter: brightness(1.05);
            }

            .video-btn {
                padding: 8px 16px;
                border-radius: 6px;
                border: 1px solid hsl(var(--border));
                background: hsl(var(--secondary));
                color: hsl(var(--foreground));
                cursor: pointer;
                font-size: 14px;
                font-weight: 500;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .video-btn:hover {
                background: hsl(var(--secondary) / 0.8);
            }
        </style>
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
                            %>

                            <div class="chat-msg-row <%= mine ? "mine" : "other"%>">
                                <div>
                                    <span class="chat-sender"><%= sender%></span>:
                                    <span class="chat-text"><%= formatMessageJSP(m.getTesto())%></span>
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
const MY_ID = <%= myUserId != null ? myUserId : -1 %>;
    const OTHER_ID = <%= otherUserId != null ? otherUserId : -1 %>;
    const ctx = "<%= ctx %>";

    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const wsUrl = proto + location.host + ctx + "/ws/chat/" + MY_ID;

    const chatDiv = document.getElementById("chatMessages");
    const input = document.getElementById("chatInput");

    let ws = new WebSocket(wsUrl);

    ws.onopen = () => scrollBottom();
    ws.onmessage = (ev) => appendMessage(JSON.parse(ev.data));
    ws.onerror = (e) => console.error("WebSocket error:", e);

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
            time.textContent = d.toLocaleTimeString([], {hour:"2-digit", minute:"2-digit"});
        } catch (e) { time.textContent = ""; }

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
        if (!text || ws.readyState !== WebSocket.OPEN) return;

        ws.send(JSON.stringify({
            destId: OTHER_ID,
            text: text
        }));

        input.value = "";
    }

    input.addEventListener("keyup", e => {
        if (e.key === "Enter") sendMessage();
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


