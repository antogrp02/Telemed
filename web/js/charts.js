/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

/* --- FUNZIONI COMUNI --- */

function normalizeDate(d) {
    return new Date(d).toISOString().substring(0, 10);
}

function dateRange(days) {
    const out = [];
    const now = new Date();
    for (let i = days - 1; i >= 0; i--) {
        const d = new Date(now);
        d.setDate(now.getDate() - i);
        out.push(normalizeDate(d));
    }
    return out;
}

function mapDataset(raw) {
    const map = {};
    raw.forEach(r => map[normalizeDate(r.data)] = r);
    return map;
}

function buildSeries(mapped, field, days) {
    const dates = dateRange(days);
    const vals  = dates.map(d => mapped[d] ? mapped[d][field] : null);
    return { labels: dates, data: vals };
}

/* ---- TEMPLATE GRAFICO STILE RISK ---- */
function makeLineChart(canvasId, labels, data, color) {

    return new Chart(document.getElementById(canvasId), {
        type: "line",
        data: {
            labels: labels,
            datasets: [{
                data: data,
                borderColor: color,
                backgroundColor: color + "33",
                tension: 0.35,
                pointRadius: 4,
                pointHoverRadius: 8,
                borderWidth: 2,
                spanGaps: false
            }]
        },
        options: {

            interaction: {
                mode: "nearest",
                intersect: true
            },

            plugins: {
                legend: { display: false },
                tooltip: {
                    enabled: true,
                    callbacks: {
                        title: items => "Data: " + items[0].label,
                        label: item => item.raw == null ?
                                "Nessun valore" :
                                "Valore: " + item.raw
                    }
                }
            },

            elements: {
                point: {
                    hitRadius: 15
                }
            },

            scales: {
                x: { ticks: { maxRotation: 0 } },
                y: { beginAtZero: false }
            }
        }
    });
}

/* --- INIZIALIZZAZIONE PARAMETRI --- */

function initMetricChart(canvasId, raw, field, color) {
    const mapped = mapDataset(raw);
    const initial = buildSeries(mapped, field, 7);

    return makeLineChart(canvasId, initial.labels, initial.data, color);
}

function updateMetricChart(chart, raw, field, days) {
    const mapped = mapDataset(raw);
    const s = buildSeries(mapped, field, days);
    chart.data.labels = s.labels;
    chart.data.datasets[0].data = s.data;
    chart.update();
}

document.addEventListener("DOMContentLoaded", function () {
    const canvas = document.getElementById("riskChart");
    if (!canvas) return;

    const ctx = canvas.getContext("2d");

    // Lista rischi dal backend
    const dataList = riskData || [];

    const today = new Date();

    const labels = [];
    const values = [];

    // Funzione per ricavare YYYY-MM-DD da qualsiasi formato
    function normalizeDate(str) {
        try {
            let d = new Date(str);
            if (!isNaN(d)) {
                return d.toISOString().substring(0, 10);
            }
        } catch (e) {
            console.log("Bad date:", str);
        }
        return null;
    }

    for (let i = 6; i >= 0; i--) {
        const d = new Date(today);
        d.setDate(d.getDate() - i);

        const strDay = d.toISOString().substring(0, 10);

        labels.push(strDay);

        // Cerca corrispondenza ignorando formato
        const entry = dataList.find(r => normalizeDate(r.data) === strDay);

        values.push(entry ? entry.riskScore : null);
    }

    new Chart(ctx, {
        type: "line",
        data: {
            labels: labels,
            datasets: [{
                label: "Rischio HF",
                data: values,
                borderWidth: 2,
                borderColor: "#e11d48",
                tension: 0.3,
                pointRadius: 3,
                spanGaps: false
            }]
        },
        options: {
            scales: {
                y: { min: 0, max: 1 }
            }
        }
    });
});


