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
%>

<!DOCTYPE html>
<html>
<head>
    <title>Heart Monitor - Medico</title>

    <link rel="stylesheet" href="../css/style.css">

    <style>
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

        .result-item:last-child {
            border-bottom: none;
        }

        .no-results {
            padding: 10px;
            text-align: center;
            color: #64748b;
            font-size: 14px;
        }

        /* --- PULSANTI --- */
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
        .btn-primary-sm:hover {
            filter: brightness(1.05);
        }

        /* --- BADGE --- */
        .badge-alert {
            display: inline-flex;
            align-items: center;
            padding: 4px 8px;
            border-radius: 999px;
            font-size: 12px;
            background: #fee2e2;
            color: #b91c1c;
        }
        .badge-none {
            display: inline-flex;
            align-items: center;
            padding: 4px 8px;
            border-radius: 999px;
            font-size: 12px;
            background: #e5e7eb;
            color: #475569;
        }
    </style>
</head>

<body>

<div class="topbar">
    <div class="logo">Heart Monitor</div>
    <div class="subtitle">Dashboard Medico</div>
    <div class="spacer"></div>
    <a href="../logout" class="toplink">Logout</a>
</div>

<div class="layout">

    <!-- SIDEBAR -->
    <div class="sidebar">
        <a href="dashboard" class="active">Pazienti</a>
        <a href="alerts">Alert</a>
    </div>

    <!-- CONTENUTO PRINCIPALE -->
    <div class="main">

        <div style="display:flex; align-items:center; justify-content:space-between; margin-bottom:18px;">
            <h2>Pazienti in carico</h2>

            <!-- Barra di ricerca -->
            <div style="position:relative; width:260px;">
                <input id="searchBox"
                       type="text"
                       placeholder="Cerca per nome, cognome o CF..."
                       style="padding:8px 12px; width:100%; border-radius:8px; border:1px solid #cbd5f5;">
                <div id="searchResults"></div>
            </div>
        </div>

        <!-- TABELLONE PAZIENTI -->
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

            <%
                    }

                    for (Paziente p : pazienti) {
                        Risk r = lastRiskByPaz.get(p.getIdPaz());
                        float score = (r != null ? r.getRiskScore() : 0f);

                        String level = RiskEvaluator.getLevel(score);
                        String css = RiskEvaluator.getCssClass(score);
            %>

            <tr>
                <td><%= p.getNome() %> <%= p.getCognome() %></td>
                <td><%= (r != null ? r.getData() : "N/D") %></td>

                <td>
                    <span class="risk-badge <%= css %>">
                        <%= String.format("%.0f%%", score * 100) %> (<%= level %>)
                    </span>
                </td>

                <td>
                    <% if (r != null && RiskEvaluator.isAlert(score)) { %>
                    <span class="badge-alert">Attivo</span>
                    <% } else { %>
                    <span class="badge-none">Nessuno</span>
                    <% } %>
                </td>

                <td style="text-align:right;">
                    <form action="patient" method="get" style="display:inline;">
                        <input type="hidden" name="id" value="<%= p.getIdPaz() %>">
                        <button type="submit" class="btn-primary-sm">Apri scheda</button>
                    </form>
                </td>
            </tr>

            <% } } %>

            </tbody>
        </table>
    </div>
</div>

<!-- SCRIPT RICERCA LIVE -->
<script>
    const box = document.getElementById("searchBox");
    const resultsDiv = document.getElementById("searchResults");

    // Evidenzia la parte cercata
    function highlight(text, query) {
        const regex = new RegExp("(" + query + ")", "gi");
        return text.replace(regex, "<strong>$1</strong>");
    }

    box.addEventListener("input", () => {
        const q = box.value.trim();

        if (q.length === 0) {
            resultsDiv.style.display = "none";
            return;
        }

        const url = "${pageContext.request.contextPath}/doctor/search?q=" + encodeURIComponent(q);

        fetch(url)
            .then(r => r.json())
            .then(data => {

                if (!data || data.length === 0) {
                    resultsDiv.innerHTML = "<div class='no-results'>Nessun risultato</div>";
                    resultsDiv.style.display = "block";
                    return;
                }

                resultsDiv.innerHTML = data.map(p => {
                    const nome = highlight(p.nome, q);
                    const cognome = highlight(p.cognome, q);
                    const cf = highlight(p.cf, q);

                    return (
                        '<div class="result-item" onclick="location.href=\'patient?id=' + p.idPaz + '\'">' +
                            nome + ' ' + cognome +
                            ' <span style="color:#6b7280; font-size:12px;">(' + cf + ')</span>' +
                        '</div>'
                    );
                }).join("");

                resultsDiv.style.display = "block";
            })
            .catch(err => console.error("Errore fetch:", err));
    });

    // Chiude dropdown cliccando fuori
    document.addEventListener("click", (e) => {
        if (!box.contains(e.target)) {
            resultsDiv.style.display = "none";
        }
    });
</script>

</body>
</html>
