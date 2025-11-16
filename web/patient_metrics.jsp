<%-- 
    Document   : patient_metrics
    Created on : 16 nov 2025, 11:46:07
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.*" %>

<%
    String json = (String) request.getAttribute("jsonData");
    int days = (Integer) request.getAttribute("days");

    if (json == null) json = "[]";   // evita errori
%>

<!DOCTYPE html>
<html>
<head>
    <title>Storico Parametri</title>

    <link rel="stylesheet" href="../css/style.css">
    <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

    <style>
        .chart-card {
            background: white;
            padding: 20px;
            margin: 20px 0;
            border-radius: 12px;
            box-shadow: 0 2px 6px rgba(0,0,0,0.08);
        }
        canvas {
            width: 100% !important;
            height: 300px !important;
        }
        .filter {
            margin: 10px 0 20px;
        }
        .filter a {
            margin-right: 8px;
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

        <h2>Andamento Parametri (ultimi <%= days %> giorni)</h2>

        <div class="filter">
            <a href="metrics?days=7" class="btn-primary">Ultimi 7 giorni</a>
            <a href="metrics?days=30" class="btn-primary">Ultimi 30 giorni</a>
        </div>

        <!-- I 5 grafici -->
        <div class="chart-card"><canvas id="chartRHR"></canvas></div>
        <div class="chart-card"><canvas id="chartHRV"></canvas></div>
        <div class="chart-card"><canvas id="chartSpO2"></canvas></div>
        <div class="chart-card"><canvas id="chartWeight"></canvas></div>
        <div class="chart-card"><canvas id="chartSteps"></canvas></div>

    </div>
</div>

<!-- JSON safe: NON nel corpo HTML -->
<div id="metricsData"
     data-json='<%= json.replace("'", "\\'") %>'
     style="display:none;"></div>

<script>

    // --- JSON ---
    const wrapper = document.getElementById("metricsData");
    const json = wrapper.getAttribute("data-json");
    const raw = JSON.parse(json);


    // Normalizza una data qualsiasi in YYYY-MM-DD
    function normalizeDate(d) {
        const date = new Date(d);
        return date.toISOString().substring(0, 10);
    }

    // --- Lista date complete ---
    function generateDateRange(days) {
        const out = [];
        const today = new Date();

        for (let i = days - 1; i >= 0; i--) {
            const d = new Date(today);
            d.setDate(today.getDate() - i);
            out.push(normalizeDate(d));
        }
        return out;
    }

    const days = <%= days %>;
    const fullDates = generateDateRange(days);


    // --- Mappa data → valori reali ---
    const map = {};
    raw.forEach(item => {
        const key = normalizeDate(item.data);
        map[key] = item;
    });

    // --- Serie completa con null ---
    function series(field) {
        return fullDates.map(d => map[d] ? map[d][field] : null);
    }


    // --- Disegno grafici ---
    function plot(id, label, field) {
        new Chart(document.getElementById(id), {
            type: "line",
            data: {
                labels: fullDates,
                datasets: [{
                    label: label,
                    data: series(field),
                    borderWidth: 2,
                    borderColor: "#0ea5e9",
                    tension: 0.3,
                    pointRadius: 2,
                    spanGaps: false
                }]
            },
            options: {
                scales: {
                    x: { ticks: { maxRotation: 0, minRotation: 0 }},
                    y: { beginAtZero: false }
                }
            }
        });
    }

    plot("chartRHR", "RHR", "rhrCurr");
    plot("chartHRV", "HRV RMSSD", "hrvRmssdCurr");
    plot("chartSpO2", "SpO₂", "spo2Curr");
    plot("chartWeight", "Peso (kg)", "weightCurr");
    plot("chartSteps", "Passi", "stepsCurr");

</script>

</body>
</html>
