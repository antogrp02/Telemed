<%-- 
    Document   : doctor_alerts
    Created on : 14 nov 2025, 18:33:56
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="java.util.Map" %>
<%@ page import="model.Alert, model.Paziente" %>

<%
    List<Alert> alerts = (List<Alert>) request.getAttribute("alerts");
    Map<Long, Paziente> pazMap = (Map<Long, Paziente>) request.getAttribute("pazienti");
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
    <title>Heart Monitor - Alert</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">

    <style>
        .btn-sm {
            padding: 4px 10px;
            border-radius: 999px;
            border: 1px solid #cbd5f5;
            background: #ffffff;
            cursor: pointer;
            font-size: 12px;
            margin-left: 4px;
        }
        .btn-sm:hover { background: #eef2ff; }

        .btn-primary-sm {
            padding: 4px 10px;
            border-radius: 999px;
            border: none;
            cursor: pointer;
            background: linear-gradient(to right, #0ea5e9, #6366f1);
            color: white;
            font-size: 12px;
            font-weight: 500;
            margin-left: 4px;
        }
        .btn-primary-sm:hover { filter: brightness(1.05); }

        .badge-state {
            display: inline-flex;
            align-items: center;
            padding: 4px 8px;
            border-radius: 999px;
            font-size: 12px;
            background: #e5e7eb;
            color: #374151;
        }
        .badge-new {
            background: #fee2e2;
            color: #b91c1c;
        }
    </style>
</head>

<body>

<div class="topbar">
    <div class="logo">Heart Monitor</div>
    <div class="subtitle">Alert attivi</div>
    <div class="spacer"></div>
    <a href="<%= ctx %>/logout" class="toplink">Logout</a>
</div>

<div class="layout">

    <!-- SIDEBAR -->
    <div class="sidebar">
        <a href="<%= ctx %>/doctor/dashboard">Pazienti</a>
        <a href="<%= ctx %>/doctor/alerts" class="active">Alert</a>
    </div>

    <!-- MAIN -->
    <div class="main">
        <h2>Alert attivi</h2>

        <table class="table">
            <thead>
            <tr>
                <th>Paziente</th>
                <th>Data predizione</th>
                <th>Messaggio</th>
                <th>Stato</th>
                <th style="text-align:right;">Azioni</th>
            </tr>
            </thead>

            <tbody>
            <%
                if (alerts == null || alerts.isEmpty()) {
            %>
                <tr>
                    <td colspan="5" style="text-align:center; padding:20px; color:#64748b;">
                        Nessun alert attivo.
                    </td>
                </tr>
            <%
                } else {
                    for (Alert a : alerts) {

                        Paziente p = pazMap != null ? pazMap.get(a.getIdPaz()) : null;
                        String label =
                                (p != null)
                                ? p.getNome() + " " + p.getCognome() + " (" + p.getCf() + ")"
                                : "Paziente #" + a.getIdPaz();
            %>

                <tr>
                    <td><%= label %></td>

                    <td><%= a.getRiskData() %></td>

                    <td>
                        <%= (a.getMessaggio() != null && !a.getMessaggio().isEmpty())
                                ? a.getMessaggio()
                                : "Alert senza messaggio" %>
                    </td>

                    <td>
                        <span class="badge-state <%= !a.isVisto() ? "badge-new" : "" %>">
                            <% if (a.isArchiviato()) { %>
                                Archiviato
                            <% } else if (!a.isVisto()) { %>
                                Nuovo
                            <% } else { %>
                                Visto
                            <% } %>
                        </span>
                    </td>

                    <td style="text-align:right; white-space:nowrap;">

                        <!-- Apri scheda paziente -->
                        <form action="<%= ctx %>/doctor/patient" method="get" style="display:inline;">
                            <input type="hidden" name="id" value="<%= a.getIdPaz() %>">
                            <button type="submit" class="btn-sm">Apri scheda</button>
                        </form>

                        <% if (!a.isVisto() && !a.isArchiviato()) { %>
                        <form action="<%= ctx %>/doctor/alerts/seen" method="post" style="display:inline;">
                            <input type="hidden" name="id" value="<%= a.getIdAlert() %>">
                            <button type="submit" class="btn-sm">Segna visto</button>
                        </form>
                        <% } %>

                        <% if (!a.isArchiviato()) { %>
                        <form action="<%= ctx %>/doctor/alerts/archive" method="post" style="display:inline;">
                            <input type="hidden" name="id" value="<%= a.getIdAlert() %>">
                            <button type="submit" class="btn-primary-sm">Archivia</button>
                        </form>
                        <% } %>

                    </td>
                </tr>

            <%
                    }
                }
            %>

            </tbody>
        </table>
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
