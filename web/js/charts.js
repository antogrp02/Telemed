/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

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

document.addEventListener("DOMContentLoaded", function () {

    // Se la pagina non contiene il dataset JSON, esci
    const dataContainer = document.getElementById("metricsData");
    if (!dataContainer) return;  // non siamo in patient_metrics.jsp

    const rawData = JSON.parse(dataContainer.textContent);

    // Estraggo le date
    const labels = rawData.map(r => r.data.substring(0, 10));

    // Estraggo serie parametri
    const hr = rawData.map(r => r.hrCurr);
    const spo2 = rawData.map(r => r.spo2Curr);
    const weight = rawData.map(r => r.weightCurr);
    const steps = rawData.map(r => r.stepsCurr);

    function drawChart(canvasId, label, data) {
        const canvas = document.getElementById(canvasId);
        if (!canvas) return;

        new Chart(canvas, {
            type: "line",
            data: {
                labels: labels,
                datasets: [{
                    label: label,
                    data: data,
                    borderWidth: 2,
                    tension: 0.3,
                    pointRadius: 3
                }]
            },
            options: {
                plugins: { legend: { display: true } },
                scales: { y: { beginAtZero: false } }
            }
        });
    }

    // Disegno i grafici
    drawChart("hrChart", "HR (bpm)", hr);
    drawChart("spo2Chart", "SpO₂ (%)", spo2);
    drawChart("weightChart", "Peso (kg)", weight);
    drawChart("stepsChart", "Passi", steps);
});


