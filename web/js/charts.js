/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */

document.addEventListener("DOMContentLoaded", function() {
  const canvas = document.getElementById("riskChart");
  if (!canvas) return;

  const ctx = canvas.getContext("2d");

  const labels = ["-6g","-5g","-4g","-3g","-2g","-1g","oggi"];
  const data = [0.18,0.21,0.24,0.30,0.35,0.40,0.45];

  new Chart(ctx, {
    type: "line",
    data: {
      labels: labels,
      datasets: [{
        label: "Rischio",
        data: data,
        borderWidth: 2,
        tension: 0.3
      }]
    },
    options: {
      plugins: { legend: { display: false } },
      scales: {
        y: { min: 0, max:1 }
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


