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
                color: white;
                margin-right: 25px;
                font-size: 20px;
            }

            .notifBadge {
                background: #2563eb;
                color: white;
                padding: 3px 8px;
                border-radius: 999px;
                font-size: 12px;
                position: absolute;
                top: -6px;
                right: -10px;
            }

            #notifBox {
                position: absolute;
                top: 55px;
                right: 20px;
                width: 290px;
                background: white;
                border: 1px solid #ddd;
                border-radius: 10px;
                box-shadow: 0px 4px 14px rgba(0,0,0,0.15);
                display: none;
                z-index: 500;
            }

            .notif-header {
                padding: 10px;
                font-weight: bold;
                background: #f3f4f6;
                border-bottom: 1px solid #ddd;
            }

            .notif-item {
                padding: 10px 12px;
                cursor: pointer;
                border-bottom: 1px solid #eee;
            }

            .notif-item:hover {
                background: #f9fafb;
            }

            .notif-empty {
                padding: 15px;
                text-align: center;
                color: #777;
            }

            /* --- DROPDOWN RICERCA --- */
            #searchResults {
                position: absolute;
                top: 40px;
                width: 100%;
                background: white;
                border: 1px solid #e2e8f0;
                border-radius: 8px;
                display: none;
                z-index: 200;
                box-shadow: 0 4px 12px rgba(0,0,0,0.08);
                max-height: 240px;
                overflow-y: auto;
            }
            .result-item {
                padding: 10px 12px;
                cursor: pointer;
                font-size: 14px;
                color: #1e293b;
                border-bottom: 1px solid #f1f5f9;
            }
            .result-item:hover {
                background: #f8fafc;
            }

            /* --- BOTTONI --- */
            .btn-primary-sm {
                padding: 6px 12px;
                border-radius: 999px;
                border: none;
                cursor: pointer;
                background: linear-gradient(to right, #0ea5e9, #6366f1);
                color: white;
                font-size: 13px;
                font-weight: 500;
            }

            /* --- BADGE ALERT --- */
            .badge-alert {
                display: inline-flex;
                padding: 4px 8px;
                border-radius: 999px;
                font-size: 12px;
                background: #fee2e2;
                color: #b91c1c;
            }
            .badge-none {
                display: inline-flex;
                padding: 4px 8px;
                border-radius: 999px;
                font-size: 12px;
                background: #e5e7eb;
                color: #475569;
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
