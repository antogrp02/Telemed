/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
// ======================================
//   LISTENER GLOBALE CHIAMATE IN ARRIVO
// ======================================

let callPopup = null;
let incomingCallerId = null;

window.initCallListener = function(myId) {

    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const basePath = location.pathname.split("/")[1];
    const wsUrl = proto + location.host + "/" + basePath + "/ws/video/" + myId;

    const socket = new WebSocket(wsUrl);

    socket.onopen = () => console.log("Call listener attivo");

    socket.onmessage = (event) => {
        const data = JSON.parse(event.data);

        if (data.type === "offer") {
            incomingCallerId = data.from;
            showIncomingCallPopup();
        }

        if (data.type === "hangup") {
            closeIncomingCallPopup();
        }
    };

    window._callSignalSocket = socket; // globale
};

// =========================
//  POPUP CHIAMATA IN ARRIVO
// =========================

function showIncomingCallPopup() {

    // Se è già aperto → ignora
    if (callPopup) return;

    callPopup = document.createElement("div");
    callPopup.id = "incomingCallPopup";
    callPopup.innerHTML = `
        <div class="call-popup-box">
            <div class="call-popup-title">📞 Chiamata in arrivo</div>
            <div class="call-popup-buttons">
                <button id="btnAcceptCall" class="btn-accept">Accetta</button>
                <button id="btnRejectCall" class="btn-reject">Rifiuta</button>
            </div>
        </div>
    `;

    document.body.appendChild(callPopup);

    // Listener pulsanti
    document.getElementById("btnAcceptCall").onclick = acceptIncomingCall;
    document.getElementById("btnRejectCall").onclick = rejectIncomingCall;
}

function closeIncomingCallPopup() {
    if (callPopup) {
        callPopup.remove();
        callPopup = null;
    }
}

// =========================
//  AZIONI PULSANTI
// =========================

function acceptIncomingCall() {
    closeIncomingCallPopup();

    // Mostra finestra video
    document.getElementById("videoCallWindow").style.display = "block";

    // Avvia WebRTC ma come "rispondente"
    startTelevisita();
}

function rejectIncomingCall() {
    // Notifica rifiuto
    _callSignalSocket.send(JSON.stringify({
        type: "hangup",
        to: incomingCallerId
    }));

    closeIncomingCallPopup();
}


