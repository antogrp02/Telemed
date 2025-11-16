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

