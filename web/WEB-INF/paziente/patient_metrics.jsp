<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>

<%
    String json = (String) request.getAttribute("jsonData");
    if (json == null)
        json = "[]";
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Storico Parametri</title>

        <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
        <script src="<%= request.getContextPath() %>/js/charts.js"></script>
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <style>
            .charts-grid {
                display: grid;
                grid-template-columns: repeat(auto-fill, minmax(450px, 1fr));
                gap: 24px;
                padding-right: 20px;
            }

            @media (max-width: 640px) {
                .charts-grid {
                    grid-template-columns: 1fr;
                }
            }

            .chart-card {
                background: #ffffff;
                border-radius: 16px;
                padding: 24px;
                box-shadow: 0 4px 20px rgba(15, 23, 42, 0.08);
                border: 1px solid #f1f5f9;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                position: relative;
                overflow: hidden;
            }

            .chart-card::before {
                content: '';
                position: absolute;
                top: 0;
                left: 0;
                right: 0;
                height: 3px;
                background: linear-gradient(90deg, #667eea 0%, #764ba2 100%);
                transform: scaleX(0);
                transition: transform 0.3s cubic-bezier(0.4, 0, 0.2, 1);
            }

            .chart-card:hover {
                box-shadow: 0 12px 32px rgba(15, 23, 42, 0.12);
                transform: translateY(-4px);
                border-color: #e2e8f0;
            }

            .chart-card:hover::before {
                transform: scaleX(1);
            }

            .chart-card h3 {
                margin: 0 0 16px 0;
                font-size: 18px;
                font-weight: 700;
                color: #0f172a;
                letter-spacing: -0.5px;
            }

            .chart-controls {
                display: flex;
                gap: 8px;
                margin-bottom: 16px;
                flex-wrap: wrap;
            }

            .chart-btn {
                padding: 8px 16px;
                border-radius: 12px;
                border: 2px solid #e2e8f0;
                cursor: pointer;
                background: #ffffff;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                font-size: 13px;
                font-weight: 600;
                color: #475569;
                box-shadow: 0 2px 4px rgba(15, 23, 42, 0.05);
            }

            .chart-btn:hover:not(.active) {
                background: #f8fafc;
                border-color: #cbd5e1;
                transform: translateY(-1px);
                box-shadow: 0 4px 8px rgba(15, 23, 42, 0.08);
            }

            .chart-btn.active {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-color: transparent;
                box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
                transform: translateY(-1px);
            }

            canvas {
                width: 100% !important;
                height: 280px !important;
                border-radius: 8px;
            }

        </style>
    </head>
    <body>

        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Storico Parametri</div>
            <div class="spacer"></div>
            <a href="<%= request.getContextPath() %>/logout" class="toplink">Logout</a>
        </div>

        <div class="layout">

            <div class="sidebar">
                <a href="<%= request.getContextPath() %>/patient/dashboard">Dashboard</a>
                <a href="<%= request.getContextPath() %>/patient/questionnaire">Questionario</a>
                <a class="active" href="<%= request.getContextPath() %>/patient/metrics">Storico Parametri</a>
                <a href="<%= request.getContextPath() %>/patient/chat">Chat & Televisita</a>
            </div>

            <div class="main">
                <h2>Andamento Parametri</h2>

                <div class="charts-grid">

                    <!-- HR a riposo -->
                    <div class="chart-card">
                        <h3>HR a riposo</h3>
                        <div class="chart-controls">
                            <button class="chart-btn active" data-days="7"
                                    onclick="refreshChart(event, 'chartRHR', 'rhrCurr')">7g</button>
                            <button class="chart-btn" data-days="30"
                                    onclick="refreshChart(event, 'chartRHR', 'rhrCurr')">30g</button>
                        </div>
                        <canvas id="chartRHR"></canvas>
                    </div>

                    <!-- HRV RMSSD -->
                    <div class="chart-card">
                        <h3>HRV RMSSD</h3>
                        <div class="chart-controls">
                            <button class="chart-btn active" data-days="7"
                                    onclick="refreshChart(event, 'chartHRV', 'hrvRmssdCurr')">7g</button>
                            <button class="chart-btn" data-days="30"
                                    onclick="refreshChart(event, 'chartHRV', 'hrvRmssdCurr')">30g</button>
                        </div>
                        <canvas id="chartHRV"></canvas>
                    </div>

                    <!-- SpO2 -->
                    <div class="chart-card">
                        <h3>SpO₂</h3>
                        <div class="chart-controls">
                            <button class="chart-btn active" data-days="7"
                                    onclick="refreshChart(event, 'chartSpO2', 'spo2Curr')">7g</button>
                            <button class="chart-btn" data-days="30"
                                    onclick="refreshChart(event, 'chartSpO2', 'spo2Curr')">30g</button>
                        </div>
                        <canvas id="chartSpO2"></canvas>
                    </div>

                    <!-- Peso -->
                    <div class="chart-card">
                        <h3>Peso</h3>
                        <div class="chart-controls">
                            <button class="chart-btn active" data-days="7"
                                    onclick="refreshChart(event, 'chartWeight', 'weightCurr')">7g</button>
                            <button class="chart-btn" data-days="30"
                                    onclick="refreshChart(event, 'chartWeight', 'weightCurr')">30g</button>
                        </div>
                        <canvas id="chartWeight"></canvas>
                    </div>

                    <!-- Passi -->
                    <div class="chart-card">
                        <h3>Passi</h3>
                        <div class="chart-controls">
                            <button class="chart-btn active" data-days="7"
                                    onclick="refreshChart(event, 'chartSteps', 'stepsCurr')">7g</button>
                            <button class="chart-btn" data-days="30"
                                    onclick="refreshChart(event, 'chartSteps', 'stepsCurr')">30g</button>
                        </div>
                        <canvas id="chartSteps"></canvas>
                    </div>

                </div>

            </div>
        </div>

        <!-- JSON DATI DAL BACKEND -->
        <div id="metrics-json"
             data-json='<%= json.replace("'", "\\'")%>'
             style="display:none;"></div>

        <script>
            /* ---- INIZIALIZZAZIONE GRAFICI ---- */
            const raw = JSON.parse(document.getElementById("metrics-json").dataset.json);

            const chartRHR = initMetricChart("chartRHR", raw, "rhrCurr", "#0ea5e9");
            const chartHRV = initMetricChart("chartHRV", raw, "hrvRmssdCurr", "#0ea5e9");
            const chartSpO2 = initMetricChart("chartSpO2", raw, "spo2Curr", "#0ea5e9");
            const chartWeight = initMetricChart("chartWeight", raw, "weightCurr", "#0ea5e9");
            const chartSteps = initMetricChart("chartSteps", raw, "stepsCurr", "#0ea5e9");


            /* ---- FUNZIONE AGGIORNA GRAFICO ---- */
            async function refreshChart(event, canvasId, field) {

                const btn = event.currentTarget;
                const days = btn.dataset.days;

                const url = "<%= request.getContextPath()%>/patient/metrics?days=" + days;
                console.log("FETCH:", url);

                const res = await fetch(url, {
                    headers: {"X-Requested-With": "XMLHttpRequest"}
                });

                const newRaw = await res.json();

                const chart =
                        canvasId === "chartRHR" ? chartRHR :
                        canvasId === "chartHRV" ? chartHRV :
                        canvasId === "chartSpO2" ? chartSpO2 :
                        canvasId === "chartWeight" ? chartWeight :
                        chartSteps;

                updateMetricChart(chart, newRaw, field, days);

                btn.parentNode.querySelectorAll(".chart-btn")
                        .forEach(b => b.classList.remove("active"));
                btn.classList.add("active");
            }

        </script>

        <%@ include file="/WEB-INF/includes/video_window.jsp" %>

        <script src="<%= request.getContextPath()%>/js/webrtc.js"></script>
        <script>
    const MY_ID = <%= session.getAttribute("id_utente")%>;
    initTelevisit(MY_ID);
        </script>

    </body>
</html>
