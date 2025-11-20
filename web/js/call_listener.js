/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
// ======================================
//   LISTENER GLOBALE CHIAMATE IN ARRIVO
// ======================================

let callPopup = null;
let incomingCallerId = null;
let _callSignalSocket = null;

window.initCallListener = function(myId) {

    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const basePath = location.pathname.split("/")[1];
    const wsUrl = proto + location.host + "/" + basePath + "/ws/video/" + myId;

    _callSignalSocket = new WebSocket(wsUrl);

    _callSignalSocket.onopen = () =>
        console.log("Call listener attivo per utente:", myId);

    _callSignalSocket.onmessage = (event) => {
        const data = JSON.parse(event.data);

        // ================================
        //   1) CHIAMATA IN ARRIVO (OFFERTA INIZIALE)
        // ================================
        if (data.type === "offer-init") {  
            incomingCallerId = data.from;
            showIncomingCallPopup();
            return;
        }

        // ================================
        //   2) CANCELLAZIONE CHIAMATA
        // ================================
        if (data.type === "hangup") {
            closeIncomingCallPopup();
        }
    };
};



// =========================
//  POPUP CHIAMATA IN ARRIVO
// =========================

function showIncomingCallPopup() {

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

    // Mostra finestra video universale
    document.getElementById("videoCallWindow").style.display = "block";

    // Avvia WebRTC come RISPOINDENTE
    // Passiamo l'ID chiamante → serve a webrtc.js
    startTelevisita(incomingCallerId);
}

function rejectIncomingCall() {

    _callSignalSocket.send(JSON.stringify({
        type: "hangup",
        to: incomingCallerId
    }));

    closeIncomingCallPopup();
}



// =========================
//   AVVIA CHIAMATA USCENTE
// =========================

window.startOutgoingCall = function(receiverId) {

    // Mostra subito finestra video
    document.getElementById("videoCallWindow").style.display = "block";

    // Avvia WebRTC come CHIAMANTE
    startTelevisita(receiverId);

    // Notifica al destinatario che lo stai chiamando
    _callSignalSocket.send(JSON.stringify({
        type: "offer-init",
        to: receiverId
    }));
};
