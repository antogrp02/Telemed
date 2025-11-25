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
        <link rel="stylesheet" href="<%= ctx%>/css/style.css">

        <style>
            .btn-sm {
                padding: 6px 14px;
                border-radius: 12px;
                border: 1px solid #e2e8f0;
                background: #ffffff;
                cursor: pointer;
                font-size: 12px;
                font-weight: 600;
                margin-left: 6px;
                color: #334155;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
            }

            .btn-sm:hover {
                background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
                border-color: #cbd5e1;
                transform: translateY(-1px);
                box-shadow: 0 2px 8px rgba(15, 23, 42, 0.12);
            }

            .btn-sm:active {
                transform: translateY(0);
            }

            .btn-primary-sm {
                padding: 6px 14px;
                border-radius: 12px;
                border: none;
                cursor: pointer;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                font-size: 12px;
                font-weight: 700;
                margin-left: 6px;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
                letter-spacing: 0.3px;
            }

            .btn-primary-sm:hover {
                background: linear-gradient(135deg, #5a67d8 0%, #6b5b95 100%);
                transform: translateY(-2px);
                box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
            }

            .btn-primary-sm:active {
                transform: translateY(0);
            }

            .badge-state {
                display: inline-flex;
                align-items: center;
                padding: 6px 12px;
                border-radius: 999px;
                font-size: 11px;
                font-weight: 700;
                background: linear-gradient(135deg, #f1f5f9 0%, #e2e8f0 100%);
                color: #475569;
                border: 1px solid #cbd5e1;
                box-shadow: 0 2px 6px rgba(71, 85, 105, 0.08);
                text-transform: uppercase;
                letter-spacing: 0.5px;
                transition: all 0.3s ease;
            }

            .badge-state:hover {
                transform: scale(1.05);
                box-shadow: 0 2px 8px rgba(71, 85, 105, 0.12);
            }

            .badge-new {
                background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
                color: #b91c1c;
                border: 1px solid #fca5a5;
                box-shadow: 0 2px 8px rgba(185, 28, 28, 0.15);
                animation: pulseNew 2s ease-in-out infinite;
            }

            @keyframes pulseNew {
                0%, 100% {
                    transform: scale(1);
                }
                50% {
                    transform: scale(1.03);
                }
            }

            .badge-new:hover {
                transform: scale(1.05);
                box-shadow: 0 2px 10px rgba(185, 28, 28, 0.25);
            }

        </style>
    </head>

    <body>

        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Alert attivi</div>
            <div class="spacer"></div>
            <a href="<%= ctx%>/logout" class="toplink">Logout</a>
        </div>

        <div class="layout">

            <!-- SIDEBAR -->
            <div class="sidebar">
                <a href="<%= ctx%>/doctor/dashboard">Pazienti</a>
                <a href="<%= ctx%>/doctor/appointments">Appuntamenti</a>
                <a href="<%= ctx%>/doctor/alerts" class="active">Alert</a>
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
                                String label
                                        = (p != null)
                                                ? p.getNome() + " " + p.getCognome() + " (" + p.getCf() + ")"
                                                : "Paziente #" + a.getIdPaz();
                        %>

                        <tr>
                            <td><%= label%></td>

                            <td><%= a.getRiskData()%></td>

                            <td>
                                <%= (a.getMessaggio() != null && !a.getMessaggio().isEmpty())
                                        ? a.getMessaggio()
                                        : "Alert senza messaggio"%>
                            </td>

                            <td>
                                <span class="badge-state <%= !a.isVisto() ? "badge-new" : ""%>">
                                    <% if (a.isArchiviato()) { %>
                                    Archiviato
                                    <% } else if (!a.isVisto()) { %>
                                    Nuovo
                                    <% } else { %>
                                    Visto
                                    <% }%>
                                </span>
                            </td>

                            <td style="text-align:right; white-space:nowrap;">

                                <!-- Apri scheda paziente -->
                                <form action="<%= ctx%>/doctor/patient" method="get" style="display:inline;">
                                    <input type="hidden" name="id" value="<%= a.getIdPaz()%>">
                                    <button type="submit" class="btn-sm">Apri scheda</button>
                                </form>

                                <% if (!a.isVisto() && !a.isArchiviato()) {%>
                                <form action="<%= ctx%>/doctor/alerts/seen" method="post" style="display:inline;">
                                    <input type="hidden" name="id" value="<%= a.getIdAlert()%>">
                                    <button type="submit" class="btn-sm">Segna visto</button>
                                </form>
                                <% } %>

                                <% if (!a.isArchiviato()) {%>
                                <form action="<%= ctx%>/doctor/alerts/archive" method="post" style="display:inline;">
                                    <input type="hidden" name="id" value="<%= a.getIdAlert()%>">
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

        <script src="<%= request.getContextPath()%>/js/webrtc.js"></script>
        <script>
            const MY_ID = <%= session.getAttribute("id_utente")%>;
            initTelevisit(MY_ID);
        </script>
    </body>
</html>
