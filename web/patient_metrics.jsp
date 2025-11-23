<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>

<%
    String json = (String) request.getAttribute("jsonData");
    if (json == null) json = "[]";
%>

<!DOCTYPE html>
<html>
<head>
    <title>Storico Parametri</title>

    <link rel="stylesheet" href="../css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>
    <script src="../js/charts.js"></script>

    <style>
        .charts-grid {
            display: grid;
            grid-template-columns: repeat(auto-fill, minmax(430px, 1fr));
            gap: 20px;
            padding-right: 20px;
        }
        .chart-card {
            background: #ffffff;
            border-radius: 14px;
            padding: 18px 22px;
            box-shadow: 0 3px 8px rgba(0,0,0,0.07);
        }
        .chart-card h3 {
            margin-bottom: 12px;
            font-size: 1.1rem;
            color: #0f172a;
        }
        .chart-controls {
            display: flex;
            gap: 6px;
            margin-bottom: 10px;
        }
        .chart-btn {
            padding: 4px 10px;
            border-radius: 6px;
            border: none;
            cursor: pointer;
            background: #e2e8f0;
            transition: 0.2s;
        }
        .chart-btn.active {
            background: #0ea5e9;
            color: white;
        }
        canvas {
            width: 100% !important;
            height: 260px !important;
        }
    </style>
</head>
<body>

<div class="topbar">
    <div class="logo">Heart Monitor</div>
    <div class="subtitle">Storico Parametri</div>
    <div class="spacer"></div>
    <a href="../logout" class="toplink">Logout</a>
</div>

<div class="layout">

    <div class="sidebar">
        <a href="dashboard">Dashboard</a>
        <a href="questionnaire">Questionario</a>
        <a class="active" href="metrics">Storico Parametri</a>
    </div>

    <div class="main">
        <h2>Andamento Parametri</h2>

        <div class="charts-grid">

            <!-- HR a riposo -->
            <div class="chart-card">
                <h3>HR a riposo</h3>
                <div class="chart-controls">
                    <button class="chart-btn active" data-days="7"
                        onclick="refreshChart(event,'chartRHR','rhrCurr')">7g</button>
                    <button class="chart-btn" data-days="30"
                        onclick="refreshChart(event,'chartRHR','rhrCurr')">30g</button>
                </div>
                <canvas id="chartRHR"></canvas>
            </div>

            <!-- HRV RMSSD -->
            <div class="chart-card">
                <h3>HRV RMSSD</h3>
                <div class="chart-controls">
                    <button class="chart-btn active" data-days="7"
                        onclick="refreshChart(event,'chartHRV','hrvRmssdCurr')">7g</button>
                    <button class="chart-btn" data-days="30"
                        onclick="refreshChart(event,'chartHRV','hrvRmssdCurr')">30g</button>
                </div>
                <canvas id="chartHRV"></canvas>
            </div>

            <!-- SpO2 -->
            <div class="chart-card">
                <h3>SpO₂</h3>
                <div class="chart-controls">
                    <button class="chart-btn active" data-days="7"
                        onclick="refreshChart(event,'chartSpO2','spo2Curr')">7g</button>
                    <button class="chart-btn" data-days="30"
                        onclick="refreshChart(event,'chartSpO2','spo2Curr')">30g</button>
                </div>
                <canvas id="chartSpO2"></canvas>
            </div>

            <!-- Peso -->
            <div class="chart-card">
                <h3>Peso</h3>
                <div class="chart-controls">
                    <button class="chart-btn active" data-days="7"
                        onclick="refreshChart(event,'chartWeight','weightCurr')">7g</button>
                    <button class="chart-btn" data-days="30"
                        onclick="refreshChart(event,'chartWeight','weightCurr')">30g</button>
                </div>
                <canvas id="chartWeight"></canvas>
            </div>

            <!-- Passi -->
            <div class="chart-card">
                <h3>Passi</h3>
                <div class="chart-controls">
                    <button class="chart-btn active" data-days="7"
                        onclick="refreshChart(event,'chartSteps','stepsCurr')">7g</button>
                    <button class="chart-btn" data-days="30"
                        onclick="refreshChart(event,'chartSteps','stepsCurr')">30g</button>
                </div>
                <canvas id="chartSteps"></canvas>
            </div>

        </div>

    </div>
</div>

<!-- JSON DATI DAL BACKEND -->
<div id="metrics-json"
     data-json='<%= json.replace("'", "\\'") %>'
     style="display:none;"></div>

<script>
/* ---- INIZIALIZZAZIONE GRAFICI ---- */
const raw = JSON.parse(document.getElementById("metrics-json").dataset.json);

const chartRHR    = initMetricChart("chartRHR", raw, "rhrCurr",      "#0ea5e9");
const chartHRV    = initMetricChart("chartHRV", raw, "hrvRmssdCurr", "#0ea5e9");
const chartSpO2   = initMetricChart("chartSpO2", raw, "spo2Curr",    "#0ea5e9");
const chartWeight = initMetricChart("chartWeight", raw, "weightCurr","#0ea5e9");
const chartSteps  = initMetricChart("chartSteps", raw, "stepsCurr", "#0ea5e9");


/* ---- FUNZIONE AGGIORNA GRAFICO ---- */
async function refreshChart(event, canvasId, field) {

    const btn = event.currentTarget;
    const days = btn.dataset.days;

    const url = "<%= request.getContextPath() %>/patient/metrics?days=" + days;
    console.log("FETCH:", url);

    const res = await fetch(url, {
        headers: { "X-Requested-With": "XMLHttpRequest" }
    });

    const newRaw = await res.json();

    const chart =
        canvasId === "chartRHR"    ? chartRHR :
        canvasId === "chartHRV"    ? chartHRV :
        canvasId === "chartSpO2"   ? chartSpO2 :
        canvasId === "chartWeight" ? chartWeight :
                                     chartSteps;

    updateMetricChart(chart, newRaw, field, days);

    btn.parentNode.querySelectorAll(".chart-btn")
        .forEach(b => b.classList.remove("active"));
    btn.classList.add("active");
}

</script>

<%@ include file="/WEB-INF/includes/video_window.jsp" %>

<script src="<%= request.getContextPath() %>/js/webrtc.js"></script>
<script>
    const MY_ID = <%= session.getAttribute("id_utente") %>;
    initTelevisit(MY_ID);
</script>

</body>
</html>
