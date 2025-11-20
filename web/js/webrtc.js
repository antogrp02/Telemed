/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
// ==========================
//  WebRTC Televisita
//  Sistema Medico–Paziente
// ==========================

// Variabili globali
let localUserId = null;
let remoteUserId = null;

let rtcSocket = null;
let pc = null;

let localStream = null;
let remoteStream = null;

let localVideo = null;
let remoteVideo = null;

let micEnabled = true;
let camEnabled = true;

const rtcConfig = {
    iceServers: [
        {urls: "stun:stun.l.google.com:19302"}
    ]
};

// ==========================
//  INIZIALIZZAZIONE
// ==========================
window.initTelevisit = function (myId, otherId) {
    localUserId = myId;
    remoteUserId = otherId;

    localVideo = document.getElementById("localVideo");
    remoteVideo = document.getElementById("remoteVideo");

    if (!localVideo || !remoteVideo) {
        console.error("Elementi video mancanti nelle JSP.");
        return;
    }

    setupSignalSocket();
};

// ==========================
//  WEBSOCKET DI SEGNALAZIONE
// ==========================
function setupSignalSocket() {
    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const basePath = location.pathname.split("/")[1];
    const wsUrl = proto + location.host + "/" + basePath + "/ws/video/" + localUserId;

    rtcSocket = new WebSocket(wsUrl);

    rtcSocket.onopen = () => console.log("Socket di segnalazione aperto");

    rtcSocket.onmessage = async (msg) => {
        let data = null;
        try {
            data = JSON.parse(msg.data);
        } catch (e) {
            return;
        }

        // ⛔️ IGNORA "offer-init" (gestita dal call_listener)
        if (data.type === "offer-init")
            return;

        switch (data.type) {
            case "offer":
                console.log("Ricevuta OFFER");
                await handleOffer(data);
                break;

            case "answer":
                console.log("Ricevuta ANSWER");
                await handleAnswer(data);
                break;

            case "candidate":
                if (pc && data.candidate) {
                    console.log("ICE Candidate remoto");
                    await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
                }
                break;

            case "hangup":
                endCallUI();
                break;
        }
    };

    rtcSocket.onerror = (err) =>
        console.error("Errore WebSocket:", err);
}


// ==========================
//  AVVIO TELEVISITA
// ==========================

window.startTelevisita = async function (targetId) {

    // Imposta il destinatario SOLO se startTelevisita riceve un ID
    if (targetId) {
        remoteUserId = targetId;
    }

    if (!rtcSocket || rtcSocket.readyState !== WebSocket.OPEN) {
        return alert("Errore: canale di segnalazione non pronto.");
    }

    await setupPeerConnection();

    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);

    rtcSocket.send(JSON.stringify({
        to: remoteUserId,
        type: "offer",
        sdp: offer
    }));

    console.log("Inviata OFFER");
};


// ==========================
//  CONFIGURARE PeerConnection
//  (ATTENZIONE: getUserMedia QUI È CORRETTO!)
// ==========================
async function setupPeerConnection() {
    if (pc)
        return;

    pc = new RTCPeerConnection(rtcConfig);

    // Stream remoto
    remoteStream = new MediaStream();
    remoteVideo.srcObject = remoteStream;

    pc.ontrack = (event) => {
        console.log("Track remota aggiunta");
        remoteStream.addTrack(event.track);

        const overlay = document.getElementById("callWaitingOverlay");
        if (overlay)
            overlay.style.display = "none";  // nasconde l'overlay
    };


    pc.onicecandidate = (event) => {
        if (event.candidate) {
            rtcSocket.send(JSON.stringify({
                to: remoteUserId,
                type: "candidate",
                candidate: event.candidate
            }));
        }
    };

    // ======== OTTIENI WEBCAM QUI ========
    localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
    });

    // Mostra subito la webcam
    localVideo.srcObject = localStream;

    // Aggiungi tracce
    localStream.getTracks().forEach(t => pc.addTrack(t, localStream));
}

// ==========================
//  OFFER → ANSWER
// ==========================
async function handleOffer(data) {
    await setupPeerConnection();

    await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    rtcSocket.send(JSON.stringify({
        to: remoteUserId,
        type: "answer",
        sdp: answer
    }));

    console.log("Inviata ANSWER");
}

async function handleAnswer(data) {
    if (!pc)
        return;
    await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));
    console.log("ANSWER applicata");
}

// ==========================
//  CONTROLLI GLOBALI
// ==========================

// APRI FINESTRA VIDEO
window.openVideoCall = function () {
    const win = document.getElementById("videoCallWindow");
    if (win)
        win.style.display = "block";

    startTelevisita();   // avvia la chiamata
};

// CHIUDI FINESTRA
window.closeVideoCall = function () {
    const win = document.getElementById("videoCallWindow");
    if (win)
        win.style.display = "none";

    hangupCall();
};

// MUTE / UNMUTE
window.toggleMic = function () {
    if (!localStream)
        return;

    micEnabled = !micEnabled;
    localStream.getAudioTracks().forEach(t => t.enabled = micEnabled);

    const btn = document.getElementById("btnMic");
    if (btn)
        btn.textContent = micEnabled ? "🎤" : "🔇";
};

// CAMERA ON/OFF
window.toggleCam = function () {
    if (!localStream)
        return;

    camEnabled = !camEnabled;
    localStream.getVideoTracks().forEach(t => t.enabled = camEnabled);

    const btn = document.getElementById("btnCam");
    if (btn)
        btn.textContent = camEnabled ? "📷" : "🚫";
};


// ==========================
//  TERMINARE LA CHIAMATA
// ==========================
window.hangupCall = function () {
    rtcSocket.send(JSON.stringify({
        to: remoteUserId,
        type: "hangup"
    }));
    endCallUI();
};

function endCallUI() {
    // Chiudi peer connection
    if (pc) {
        pc.close();
        pc = null;
    }

    // Stoppa tutte le tracce locali (cam + mic)
    if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
        localStream = null;
    }

    // Svuota i video
    if (localVideo)
        localVideo.srcObject = null;
    if (remoteVideo)
        remoteVideo.srcObject = null;

    // MOSTRA overlay WhatsApp "In attesa..."
    const overlay = document.getElementById("callWaitingOverlay");
    if (overlay) {
        overlay.style.display = "flex";
    }

    console.log("Chiamata terminata");
}


// ==========================
//  CONDIVISIONE SCHERMO
// ==========================
window.shareScreen = async function () {
    if (!pc)
        return;

    const screenStream = await navigator.mediaDevices.getDisplayMedia({video: true});

    const screenTrack = screenStream.getVideoTracks()[0];
    const sender = pc.getSenders().find(s => s.track.kind === "video");

    sender.replaceTrack(screenTrack);

    screenTrack.onended = () => {
        sender.replaceTrack(localStream.getVideoTracks()[0]);
    };
};
