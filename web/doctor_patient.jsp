<%-- 
    Document   : doctor_patient
    Created on : 17 nov 2025, 23:48:56
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List" %>
<%@ page import="model.Paziente, model.Parametri, model.Risk, model.Questionario, model.Alert" %>
<%@ page import="utils.RiskEvaluator" %>

<%
    Paziente paz = (Paziente) request.getAttribute("paziente");
    Parametri lastParams = (Parametri) request.getAttribute("lastParams");
    Risk lastRisk = (Risk) request.getAttribute("lastRisk");
    float riskScore = lastRisk != null ? lastRisk.getRiskScore() : 0f;

    Questionario lastQ = (Questionario) request.getAttribute("lastQuestionario");
    List<Alert> alerts = (List<Alert>) request.getAttribute("patientAlerts");

    String metricsJson = (String) request.getAttribute("metricsJson");
    if (metricsJson == null) {
        metricsJson = "[]";
    }

    // label semplici per questionario
    String[] scala0_3 = {"Nessuna", "Lieve", "Moderata", "Grave"};
    String[] yesNo = {"Assente", "Presente"};
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Heart Monitor - Scheda Paziente</title>
        <link rel="stylesheet" href="../css/style.css">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <script src="https://cdn.jsdelivr.net/npm/chart.js"></script>

        <style>
            /* Pulsanti range grafico */
            .chart-btn {
                padding: 8px 16px;
                border-radius: 12px;
                border: 1px solid #e2e8f0;
                cursor: pointer;
                background: #ffffff;
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                font-size: 12px;
                font-weight: 600;
                margin-left: 6px;
                color: #475569;
                box-shadow: 0 1px 3px rgba(15, 23, 42, 0.08);
            }

            .chart-btn:hover {
                background: linear-gradient(135deg, #f8fafc 0%, #f1f5f9 100%);
                border-color: #cbd5e1;
                transform: translateY(-1px);
                box-shadow: 0 2px 8px rgba(15, 23, 42, 0.12);
            }

            .chart-btn.active {
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                border-color: transparent;
                box-shadow: 0 4px 12px rgba(102, 126, 234, 0.35);
                transform: translateY(-1px);
            }

            .chart-btn:active {
                transform: translateY(0);
            }

            .two-cols {
                display: grid;
                grid-template-columns: minmax(0, 2.2fr) minmax(0, 1.2fr);
                gap: 24px;
                margin-top: 24px;
            }

            @media (max-width: 1024px) {
                .two-cols {
                    grid-template-columns: 1fr;
                }
            }

            .question-row {
                display: flex;
                justify-content: space-between;
                align-items: center;
                margin-bottom: 12px;
                font-size: 14px;
                color: #334155;
                font-weight: 500;
            }

            .badge-soft {
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

            .badge-soft:hover {
                transform: scale(1.05);
                box-shadow: 0 2px 8px rgba(71, 85, 105, 0.12);
            }

            .badge-soft.red {
                background: linear-gradient(135deg, #fee2e2 0%, #fecaca 100%);
                color: #b91c1c;
                border: 1px solid #fca5a5;
                box-shadow: 0 2px 8px rgba(185, 28, 28, 0.15);
            }

            .badge-soft.amber {
                background: linear-gradient(135deg, #fef3c7 0%, #fde68a 100%);
                color: #92400e;
                border: 1px solid #fcd34d;
                box-shadow: 0 2px 8px rgba(146, 64, 14, 0.15);
            }

            .alerts-list {
                font-size: 14px;
                color: #334155;
            }

            .alerts-item {
                padding: 14px 16px;
                border-radius: 12px;
                border: 1px solid #e2e8f0;
                margin-bottom: 10px;
                background: #ffffff;
                box-shadow: 0 2px 8px rgba(15, 23, 42, 0.06);
                transition: all 0.3s cubic-bezier(0.4, 0, 0.2, 1);
                position: relative;
                overflow: hidden;
            }

            .alerts-item::before {
                content: '';
                position: absolute;
                left: 0;
                top: 0;
                height: 100%;
                width: 3px;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                opacity: 0;
                transition: opacity 0.3s ease;
            }

            .alerts-item:hover {
                border-color: #cbd5e1;
                box-shadow: 0 4px 16px rgba(15, 23, 42, 0.1);
                transform: translateX(4px);
            }

            .alerts-item:hover::before {
                opacity: 1;
            }

            .alerts-item strong {
                font-weight: 700;
                color: #0f172a;
            }

        </style>
    </head>

    <body>

        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Scheda paziente</div>
            <div class="spacer"></div>
            <a href="../logout" class="toplink">Logout</a>
        </div>

        <div class="layout">

            <!-- SIDEBAR -->
            <div class="sidebar">
                <a href="dashboard">Pazienti</a>
                <a href="appointments">Appuntamenti</a>
                <a href="alerts">Alert</a>
            </div>

            <!-- MAIN -->
            <div class="main">
                <!-- Intestazione anagrafica -->
                <div class="card">
                    <div class="card-header-row">
                        <div>
                            <div class="card-title">Paziente</div>
                            <div class="card-value">
                                <%= (paz != null) ? paz.getNome() + " " + paz.getCognome() : "N/D"%>
                            </div>
                        </div>

                        <div style="text-align:right;">
                            <div style="font-size:13px; color:#64748b;">
                                CF:
                                <strong>
                                    <%= (paz != null) ? paz.getCf() : "N/D"%>
                                </strong>
                            </div>

                            <!-- 🔵 PULSANTE CHAT -->
                            <a href="chat?id=<%= paz != null ? paz.getIdPaz() : 0%>"
                               style="
                               margin-top:6px;
                               display:inline-block;
                               padding:6px 12px;
                               background:#0ea5e9;
                               color:white;
                               border-radius:8px;
                               font-size:13px;
                               text-decoration:none;
                               font-weight:500;
                               ">
                                💬 Apri Chat
                            </a>
                        </div>
                    </div>
                </div>


                <!-- Card parametri sintetici -->
                <div class="grid-4">
                    <div class="card">
                        <div class="card-title">HR (riposo)</div>
                        <div class="card-value">
                            <%= lastParams != null ? String.format("%.0f bpm", lastParams.getRhrCurr()) : "--"%>
                        </div>
                        <div class="card-sub">
                            trend 7gg:
                            <%= lastParams != null ? String.format("%.1f", lastParams.getRhr7d()) : "-"%>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-title">SpO₂</div>
                        <div class="card-value">
                            <%= lastParams != null ? String.format("%.0f%%", lastParams.getSpo2Curr()) : "--"%>
                        </div>
                        <div class="card-sub">
                            trend 7gg:
                            <%= lastParams != null ? String.format("%.1f", lastParams.getSpo27d()) : "-"%>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-title">Peso</div>
                        <div class="card-value">
                            <%= lastParams != null ? String.format("%.1f kg", lastParams.getWeightCurr()) : "--"%>
                        </div>
                        <div class="card-sub">
                            Δ 7gg:
                            <%= lastParams != null ? String.format("%.1f", lastParams.getWeight7d()) : "-"%>
                        </div>
                    </div>

                    <div class="card">
                        <div class="card-title">Passi</div>
                        <div class="card-value">
                            <%= lastParams != null ? String.format("%.0f", lastParams.getStepsCurr()) : "--"%>
                        </div>
                        <div class="card-sub">
                            Δ 7gg:
                            <%= lastParams != null ? String.format("%.0f", lastParams.getSteps7d()) : "-"%>
                        </div>
                    </div>
                </div>

                <!-- Rischio + grafici -->
                <div class="two-cols">

                    <!-- Colonna sinistra: rischio + grafico parametri -->
                    <div>
                        <!-- Stato di rischio / mini grafico -->
                        <div class="card full">
                            <div class="card-header-row">
                                <div class="card-title">Stato di rischio</div>
                                <div class="card-sub">
                                    Ultima predizione:
                                    <%= lastRisk != null ? lastRisk.getData() : "N/D"%>
                                </div>
                            </div>

                            <div class="risk-badge <%= RiskEvaluator.getCssClass(riskScore)%>">
                                Rischio:
                                <%= String.format("%.0f%%", riskScore * 100)%>
                                (
                                <%= RiskEvaluator.getLevel(riskScore)%>
                                )
                            </div>

                            <canvas id="riskChart" height="90"></canvas>
                        </div>

                        <!-- Andamento parametri multi-feature -->
                        <div class="card full" style="margin-top:16px;">
                            <div class="card-header-row">
                                <div class="card-title">Andamento parametri (7 / 30 giorni)</div>
                                <div>
                                    <button class="chart-btn active" onclick="setMetricsRange(7, this)">7g</button>
                                    <button class="chart-btn" onclick="setMetricsRange(30, this)">30g</button>
                                </div>
                            </div>
                            <div style="height:260px;">
                                <canvas id="metricsChart"></canvas>
                            </div>
                        </div>
                    </div>

                    <!-- Colonna destra: questionario + alert -->
                    <div>
                        <!-- Questionario recente -->
                        <div class="card">
                            <div class="card-title">Questionario recente</div>
                            <div class="card-sub">
                                <%
                                    if (lastQ != null) {
                                %>
                                Data:
                                <%= lastQ.getData()%>
                                <%
                                } else {
                                %>
                                Nessun questionario disponibile.
                                <%
                                    }
                                %>
                            </div>

                            <%
                                if (lastQ != null) {
                            %>
                            <div style="margin-top:10px;">
                                <div class="question-row">
                                    <span>Dispnea</span>
                                    <span class="badge-soft <%= lastQ.getDispnea() >= 2 ? "amber" : ""%>">
                                        <%= scala0_3[lastQ.getDispnea()]%>
                                    </span>
                                </div>
                                <div class="question-row">
                                    <span>Edema caviglie</span>
                                    <span class="badge-soft <%= lastQ.getEdema() == 1 ? "red" : ""%>">
                                        <%= yesNo[lastQ.getEdema()]%>
                                    </span>
                                </div>
                                <div class="question-row">
                                    <span>Fatica</span>
                                    <span class="badge-soft <%= lastQ.getFatica() >= 2 ? "amber" : ""%>">
                                        <%= scala0_3[lastQ.getFatica()]%>
                                    </span>
                                </div>
                                <div class="question-row">
                                    <span>Ortopnea</span>
                                    <span class="badge-soft <%= lastQ.getOrtopnea() >= 1 ? "amber" : ""%>">
                                        <%= scala0_3[lastQ.getOrtopnea()]%>
                                    </span>
                                </div>
                            </div>
                            <%
                                }
                            %>
                        </div>

                        <!-- Alert paziente -->
                        <div class="card" style="margin-top:16px;">
                            <div class="card-title">Alert del paziente</div>
                            <div class="alerts-list" style="margin-top:8px;">
                                <% if (alerts == null || alerts.isEmpty()) { %>

                                <div style="color:#64748b;">Nessun alert attivo per questo paziente.</div>

                                <% } else {
                                    for (Alert a : alerts) {%>

                                <div class="alerts-item">

                                    <!-- Data rischio -->
                                    <div>
                                        <strong><%= a.getRiskData()%></strong>
                                    </div>

                                    <!-- Messaggio -->
                                    <div style="margin-top:4px;">
                                        <%= a.getMessaggio() != null ? a.getMessaggio() : "Alert senza messaggio"%>
                                    </div>

                                    <!-- Stato -->
                                    <div style="font-size:12px; color:#6b7280; margin-top:4px;">
                                        Stato: 
                                        <% if (a.isArchiviato()) { %>
                                        Archiviato
                                        <% } else if (!a.isVisto()) { %>
                                        Nuovo
                                        <% } else { %>
                                        Visto
                                        <% } %>
                                    </div>

                                </div>

                                <%   } // end for
                                    } // end else
%>
                            </div>
                        </div>
                    </div>

                </div>

                <!-- JSON nascosto per grafico parametri -->
                <div id="metrics-json"
                     data-json='<%= metricsJson.replace("'", "\\'")%>'
                     style="display:none;"></div>

            </div>
        </div>

        <!-- Script per rischio + grafico multi-parametri -->
        <script>
            // Dati rischio dal backend
            const riskData = <%= new com.google.gson.Gson().toJson(request.getAttribute("storicoRisk"))%> || [];

            // Utility date
            function normalizeDate(d) {
                try {
                    return new Date(d).toISOString().substring(0, 10);
                } catch (e) {
                    return null;
                }
            }

            function dateRange(days) {
                const out = [];
                const now = new Date();
                for (let i = days - 1; i >= 0; i--) {
                    const d = new Date(now);
                    d.setDate(d.getDate() - i);
                    out.push(d.toISOString().substring(0, 10));
                }
                return out;
            }

            // Mini grafico rischio (30 giorni, null gestiti)
            (function () {
                const canvas = document.getElementById("riskChart");
                if (!canvas)
                    return;

                const ctx = canvas.getContext("2d");
                const DAYS = 30;

                const labels = dateRange(DAYS);
                const values = labels.map(day => {
                    const match = riskData.find(r => normalizeDate(r.data) === day);
                    return match ? match.riskScore : null;
                });

                new Chart(ctx, {
                    type: "line",
                    data: {
                        labels: labels,
                        datasets: [{
                                label: "Rischio",
                                data: values,
                                borderColor: "#ef4444",
                                backgroundColor: "#fecaca",
                                tension: 0.35,
                                pointRadius: 3,
                                pointHoverRadius: 6,
                                borderWidth: 2,
                                spanGaps: false
                            }]
                    },
                    options: {
                        scales: {
                            y: {min: 0, max: 1}
                        },
                        plugins: {
                            legend: {display: false},
                            tooltip: {
                                callbacks: {
                                    label: function (item) {
                                        if (item.raw == null) {
                                            return "Nessun valore";
                                        }
                                        return "Rischio: " + Math.round(item.raw * 100) + "%";
                                    }
                                }
                            }
                        }
                    }
                });
            })();

            // Grafico multi-feature parametri (HR, SpO2, Peso, Passi)
            let metricsRaw = [];
            (function () {
                const el = document.getElementById("metrics-json");
                if (!el)
                    return;
                try {
                    metricsRaw = JSON.parse(el.dataset.json || "[]");
                } catch (e) {
                    metricsRaw = [];
                }
            })();

            function buildMetricsSeries(raw, days) {
                const map = {};
                raw.forEach(r => {
                    const key = normalizeDate(r.data);
                    if (key)
                        map[key] = r;
                });

                const labels = dateRange(days);
                const hr = [];
                const spo2 = [];
                const weight = [];
                const steps = [];

                labels.forEach(d => {
                    const row = map[d];
                    hr.push(row ? row.rhrCurr : null);
                    spo2.push(row ? row.spo2Curr : null);
                    weight.push(row ? row.weightCurr : null);
                    steps.push(row ? row.stepsCurr : null);
                });

                return {labels, hr, spo2, weight, steps};
            }

            const ctxM = document.getElementById("metricsChart")?.getContext("2d");
            let metricsChart = null;
            let series7 = null;
            let series30 = null;

            if (ctxM) {
                series7 = buildMetricsSeries(metricsRaw, 7);
                series30 = buildMetricsSeries(metricsRaw, 30);

                metricsChart = new Chart(ctxM, {
                    type: "line",
                    data: {
                        labels: series7.labels,
                        datasets: [
                            {
                                label: "HR riposo",
                                data: series7.hr,
                                borderColor: "#0ea5e9",
                                backgroundColor: "#0ea5e933",
                                tension: 0.35,
                                pointRadius: 2,
                                spanGaps: false
                            },
                            {
                                label: "SpO₂",
                                data: series7.spo2,
                                borderColor: "#22c55e",
                                backgroundColor: "#22c55e33",
                                tension: 0.35,
                                pointRadius: 2,
                                spanGaps: false
                            },
                            {
                                label: "Peso",
                                data: series7.weight,
                                borderColor: "#6366f1",
                                backgroundColor: "#6366f133",
                                tension: 0.35,
                                pointRadius: 2,
                                spanGaps: false
                            },
                            {
                                label: "Passi",
                                data: series7.steps,
                                borderColor: "#f97316",
                                backgroundColor: "#f9731633",
                                tension: 0.35,
                                pointRadius: 2,
                                spanGaps: false
                            }
                        ]
                    },
                    options: {
                        responsive: true,
                        maintainAspectRatio: false,
                        interaction: {
                            mode: "nearest",
                            intersect: false
                        },
                        plugins: {
                            tooltip: {
                                callbacks: {
                                    label: function (item) {
                                        if (item.raw == null) {
                                            return item.dataset.label + ": nessun dato";
                                        }
                                        return item.dataset.label + ": " + item.raw;
                                    }
                                }
                            }
                        }
                    }
                });
            }

            function setMetricsRange(days, btn) {
                if (!metricsChart || !series7 || !series30)
                    return;

                const s = days === 7 ? series7 : series30;

                metricsChart.data.labels = s.labels;
                metricsChart.data.datasets[0].data = s.hr;
                metricsChart.data.datasets[1].data = s.spo2;
                metricsChart.data.datasets[2].data = s.weight;
                metricsChart.data.datasets[3].data = s.steps;
                metricsChart.update();

                if (btn && btn.parentNode) {
                    btn.parentNode.querySelectorAll(".chart-btn")
                            .forEach(b => b.classList.remove("active"));
                    btn.classList.add("active");
                }
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
