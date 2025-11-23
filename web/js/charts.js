/* --- FUNZIONI COMUNI --- */

function normalizeDate(d) {
    const date = new Date(d);

    const year  = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day   = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
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
    raw.forEach(r => {
        const key = normalizeDate(r.data);
        map[key] = r;
    });
    return map;
}

function buildSeries(mapped, field, days) {
    const dates = dateRange(days);
    const vals  = dates.map(d => mapped[d] ? mapped[d][field] : null);
    return { labels: dates, data: vals };
}


/* ---- TEMPLATE GRAFICO ---- */
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
            interaction: { mode: "nearest", intersect: true },
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
            elements: { point: { hitRadius: 15 }},
            scales: {
                x: { ticks: { maxRotation: 0 }},
                y: { beginAtZero: false }
            }
        }
    });
}


/* --- GRAFICI PARAMETRI --- */

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


/* --- GRAFICO RISCHIO DASHBOARD --- */

document.addEventListener("DOMContentLoaded", () => {
    if (!Array.isArray(riskData)) return; // ✅ NON bloccare array vuoto

    const mapped = mapDataset(riskData);  // ✅ se vuoto → mapped = {}
    const series = buildSeries(mapped, "riskScore", 7); // ✅ genera 7 null

    makeLineChart("riskChart", series.labels, series.data, "#e11d48");
});

