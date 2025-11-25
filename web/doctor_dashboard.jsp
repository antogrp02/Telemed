<%-- 
    Document   : doctor_dashboard
    Created on : 14 nov 2025
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*, model.Paziente, model.Risk, utils.RiskEvaluator" %>

<%
    List<Paziente> pazienti = (List<Paziente>) request.getAttribute("pazienti");
    Map<Long, Risk> lastRiskByPaz = (Map<Long, Risk>) request.getAttribute("lastRiskByPaz");
    Map<Long, Integer> unreadByPaz = (Map<Long, Integer>) request.getAttribute("unreadByPaz");
    String ctx = request.getContextPath();

    int totalUnread = 0;
    if (unreadByPaz != null)
        for (int c : unreadByPaz.values())
            totalUnread += c;
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Heart Monitor - Medico</title>

        <link rel="stylesheet" href="<%= ctx%>/css/style.css">

        <style>
            /* =============== NOTIFICHE ================================= */

            #notifIcon {
                cursor: pointer;
                position: relative;
                color: #f8fafc;
                margin-right: 25px;
                font-size: 20px;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }

            #notifIcon:hover {
                color: #ffffff;
                transform: scale(1.1);
            }

            .notifBadge {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                padding: 4px 10px;
                border-radius: 999px;
                font-size: 11px;
                font-weight: 700;
                position: absolute;
                top: -8px;
                right: -12px;
                box-shadow: 0 2px 8px rgba(102, 126, 234, 0.4);
                animation: pulse 2s ease-in-out infinite;
            }

            @keyframes pulse {
                0%, 100% {
                    transform: scale(1);
                }
                50% {
                    transform: scale(1.05);
                }
            }

            #notifBox {
                position: fixed;
                top: 80px; /* Altezza topbar + piccolo margine */
                right: 20px;
                width: 320px;
                background: #ffffff;
                border: 1px solid #e2e8f0;
                border-radius: 16px;
                box-shadow: 0 12px 32px rgba(15, 23, 42, 0.15);
                display: none;
                z-index: 999;
                overflow: hidden;
                animation: slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }


            @keyframes slideDown {
                from {
                    opacity: 0;
                    transform: translateY(-10px);
                }
                to {
                    opacity: 1;
                    transform: translateY(0);
                }
            }

            .notif-header {
                padding: 16px 20px;
                font-weight: 700;
                font-size: 14px;
                background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
                border-bottom: 1px solid #e2e8f0;
                color: #0f172a;
                letter-spacing: 0.5px;
            }

            .notif-item {
                padding: 14px 20px;
                cursor: pointer;
                border-bottom: 1px solid #f1f5f9;
                transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
                color: #334155;
                font-size: 14px;
            }

            .notif-item:hover {
                background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
                transform: translateX(4px);
            }

            .notif-item:last-child {
                border-bottom: none;
            }

            .notif-empty {
                padding: 24px;
                text-align: center;
                color: #64748b;
                font-size: 14px;
                font-weight: 500;
            }

            /* --- DROPDOWN RICERCA --- */
            #searchResults {
                position: absolute;
                top: 40px;
                width: 100%;
                background: #ffffff;
                border: 1px solid #e2e8f0;
                border-radius: 12px;
                display: none;
                z-index: 200;
                box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
                max-height: 280px;
                overflow-y: auto;
                animation: slideDown 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }

            .result-item {
                padding: 12px 16px;
                cursor: pointer;
                font-size: 14px;
                color: #334155;
                border-bottom: 1px solid #f1f5f9;
                transition: all 0.2s cubic-bezier(0.4, 0, 0.2, 1);
                font-weight: 500;
            }

            .result-item:hover {
                background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
                color: #0f172a;
                transform: translateX(4px);
            }

            .result-item:last-child {
                border-bottom: none;
            }

            /* --- BOTTONI --- */
            .btn-primary-sm {
                padding: 8px 16px;
                border-radius: 12px;
                border: none;
                cursor: pointer;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                font-size: 13px;
                font-weight: 700;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
                letter-spacing: 0.3px;
            }

            .btn-primary-sm:hover {
                transform: translateY(-2px);
                box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
            }

            .btn-primary-sm:active {
                transform: translateY(0);
            }

            /* --- BADGE ALERT --- */
            .badge-alert {
                display: inline-flex;
                align-items: center;
                padding: 6px 12px;
                border-radius: 999px;
                font-size: 12px;
                font-weight: 700;
                background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
                color: #b91c1c;
                border: 1px solid #fca5a5;
                box-shadow: 0 2px 8px rgba(185, 28, 28, 0.15);
                text-transform: uppercase;
                letter-spacing: 0.5px;
                transition: all 0.3s ease;
            }

            .badge-alert:hover {
                transform: scale(1.05);
            }

            .badge-none {
                display: inline-flex;
                align-items: center;
                padding: 6px 12px;
                border-radius: 999px;
                font-size: 12px;
                font-weight: 700;
                background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
                color: #475569;
                border: 1px solid #cbd5e1;
                box-shadow: 0 2px 8px rgba(71, 85, 105, 0.1);
                text-transform: uppercase;
                letter-spacing: 0.5px;
                transition: all 0.3s ease;
            }

            .badge-none:hover {
                transform: scale(1.05);
            }

        </style>
    </head>

    <body>

        <!-- TOP BAR -->
        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Dashboard Medico</div>

            <div class="spacer"></div>

            <!-- ICONA NOTIFICHE -->
            <div id="notifIcon">
                💬
                <span id="notifBadge" class="notifBadge"><%= totalUnread%></span>
            </div>

            <a href="<%= ctx%>/logout" class="toplink">Logout</a>
        </div>

        <!-- BOX DROPDOWN NOTIFICHE -->
        <div id="notifBox">
            <div class="notif-header">Nuovi messaggi</div>

            <div id="notifList">
                <% if (totalUnread == 0) { %>
                <div class="notif-empty">Nessun nuovo messaggio</div>
                <% } else {
                    for (Paziente p : pazienti) {
                        int unread = unreadByPaz != null ? unreadByPaz.getOrDefault(p.getIdPaz(), 0) : 0;
                        if (unread > 0) {
                %>
                <div class="notif-item"
                     id="notif-item-<%= p.getIdPaz()%>"
                     data-count="<%= unread%>"
                     data-name="<%= p.getNome() + " " + p.getCognome()%>"
                     onclick="location.href = '<%= ctx%>/doctor/chat?id=<%= p.getIdPaz()%>'">

                    <%= p.getNome()%> <%= p.getCognome()%> — <%= unread%> messaggi
                </div>

                <% }
                        }
                    }%>
            </div>
        </div>

        <div class="layout">

            <!-- SIDEBAR -->
            <div class="sidebar">
                <a href="<%= ctx%>/doctor/dashboard" class="active">Pazienti</a>
                <a href="<%= ctx%>/doctor/appointments">Appuntamenti</a>
                <a href="<%= ctx%>/doctor/alerts">Alert</a>
            </div>


            <!-- CONTENUTO PRINCIPALE -->
            <div class="main">

                <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:18px;">
                    <h2>Pazienti in carico</h2>

                    <!-- BARRA DI RICERCA -->
                    <div style="position:relative; width:260px;">
                        <input id="searchBox"
                               type="text"
                               placeholder="Cerca per nome, cognome o CF..."
                               style="padding:8px 12px; width:100%; border-radius:8px; border:1px solid #cbd5f5;">
                        <div id="searchResults"></div>
                    </div>
                </div>

                <!-- TABELLA PAZIENTI -->
                <table class="table">
                    <thead>
                        <tr>
                            <th>Paziente</th>
                            <th>Ultima predizione</th>
                            <th>Rischio</th>
                            <th>Alert</th>
                            <th style="text-align:right;">Azioni</th>
                        </tr>
                    </thead>

                    <tbody>
                        <%
                            if (pazienti != null && lastRiskByPaz != null) {

                                if (pazienti.isEmpty()) {
                        %>
                        <tr>
                            <td colspan="5" style="text-align:center; padding:20px; color:#64748b;">
                                Nessun paziente associato.
                            </td>
                        </tr>

                        <% }

                            for (Paziente p : pazienti) {

                                Risk r = lastRiskByPaz.get(p.getIdPaz());
                                float score = (r != null ? r.getRiskScore() : 0f);

                                String level = RiskEvaluator.getLevel(score);
                                String css = RiskEvaluator.getCssClass(score);
                        %>

                        <tr>
                            <td><%= p.getNome()%> <%= p.getCognome()%></td>
                            <td><%= (r != null ? r.getData() : "N/D")%></td>

                            <td>
                                <span class="risk-badge <%= css%>">
                                    <%= String.format("%.0f%%", score * 100)%> (<%= level%>)
                                </span>
                            </td>

                            <td>
                                <%
                                    Map<Long, Boolean> hasAlert = (Map<Long, Boolean>) request.getAttribute("hasAlert");
                                    boolean alertActive = hasAlert != null && hasAlert.getOrDefault(p.getIdPaz(), false);
                                %>

                                <% if (alertActive) { %>
                                <span class="badge-alert">Attivo</span>
                                <% } else { %>
                                <span class="badge-none">Nessuno</span>
                                <% }%>
                            </td>

                            <td style="text-align:right;">
                                <form action="<%= ctx%>/doctor/patient" method="get" style="display:inline;">
                                    <input type="hidden" name="id" value="<%= p.getIdPaz()%>">
                                    <button type="submit" class="btn-primary-sm">Apri scheda</button>
                                </form>
                            </td>
                        </tr>

                        <% }
                            }%>
                    </tbody>
                </table>

            </div>
        </div>

        <!-- SCRIPT NOTIFICHE -->
        <script>
            const notifIcon = document.getElementById("notifIcon");
            const notifBox = document.getElementById("notifBox");

            notifIcon.addEventListener("click", () => {
                notifBox.style.display = notifBox.style.display === "block" ? "none" : "block";
            });

            document.addEventListener("click", (event) => {
                if (!notifIcon.contains(event.target) && !notifBox.contains(event.target)) {
                    notifBox.style.display = "none";
                }
            });
        </script>

        <!-- SCRIPT RICERCA -->
        <script>
            const box = document.getElementById("searchBox");
            const resultsDiv = document.getElementById("searchResults");
            const ctxPath = "<%= ctx%>";

            function highlight(text, q) {
                const r = new RegExp("(" + q + ")", "gi");
                return text.replace(r, "<strong>$1</strong>");
            }

            box.addEventListener("input", () => {
                const q = box.value.trim();
                if (!q) {
                    resultsDiv.style.display = "none";
                    return;
                }

                fetch(ctxPath + "/doctor/search?q=" + encodeURIComponent(q))
                        .then(r => r.json())
                        .then(data => {
                            if (!data || data.length === 0) {
                                resultsDiv.innerHTML = "<div class='no-results'>Nessun risultato</div>";
                                resultsDiv.style.display = "block";
                                return;
                            }

                            resultsDiv.innerHTML = data.map(p =>
                                '<div class="result-item" onclick="location.href=\'' + ctxPath +
                                        '/doctor/patient?id=' + p.idPaz + '\'">' +
                                        highlight(p.nome, q) + ' ' + highlight(p.cognome, q) +
                                        ' <span style="color:#6b7280; font-size:12px;">(' + highlight(p.cf, q) +
                                        ')</span></div>'
                            ).join("");

                            resultsDiv.style.display = "block";
                        });
            });

            document.addEventListener("click", e => {
                if (!box.contains(e.target))
                    resultsDiv.style.display = "none";
            });
        </script>

        <!-- WEBSOCKET NOTIFICHE REALTIME -->
        <script>
            const MY_ID = <%= session.getAttribute("id_utente")%>;

            const wsProto = location.protocol === "https:" ? "wss://" : "ws://";
            const wsUrl = wsProto + location.host + "<%= ctx%>/ws/chat/" + MY_ID;

            let ws = new WebSocket(wsUrl);

            ws.onmessage = (ev) => {
                const msg = JSON.parse(ev.data);

                // Solo messaggi ricevuti da pazienti reali
                if (!msg.mine && msg.pazienteId > 0) {

                    const badge = document.getElementById("notifBadge");
                    const list = document.getElementById("notifList");

                    // aggiorno badge
                    let num = parseInt(badge.textContent) || 0;
                    badge.textContent = num + 1;

                    // rimuovo placeholder "nessun messaggio"
                    const empty = document.querySelector(".notif-empty");
                    if (empty)
                        empty.remove();

                    // controllo se il paziente ha già una riga
                    const existing = document.getElementById("notif-item-" + msg.pazienteId);

                    if (existing) {
                        let c = parseInt(existing.dataset.count) || 0;
                        c++;
                        existing.dataset.count = c;

                        const fullName = existing.dataset.name;
                        existing.innerHTML = fullName + " — " + c + " messaggi";

                    } else {
                        // creo nuova riga
                        const item = document.createElement("div");
                        item.className = "notif-item";
                        item.id = "notif-item-" + msg.pazienteId;

                        const fullName = (msg.pazienteNome || "Paziente") + " " +
                                (msg.pazienteCognome || "");

                        item.dataset.name = fullName;
                        item.dataset.count = 1;
                        item.innerHTML = fullName + " — 1 messaggio";

                        item.onclick = () => location.href = "<%= ctx%>/doctor/chat?id=" + msg.pazienteId;

                        list.appendChild(item);
                    }
                }
            };
        </script>

        <%@ include file="/WEB-INF/includes/video_window.jsp" %>
        <script src="<%= ctx%>/js/webrtc.js"></script>
        <script> initTelevisit(MY_ID);</script>

    </body>
</html>
