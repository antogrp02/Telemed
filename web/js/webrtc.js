// ==========================
//  WebRTC Televisita Unificato
// ==========================

// Identità
let localUserId = null;   // mio id utente
let remoteUserId = null;  // id dell'altro utente

// WebRTC
let rtcSocket = null;
let pc = null;

let localStream = null;
let remoteStream = null;

let localVideo = null;
let remoteVideo = null;

let micEnabled = true;
let camEnabled = true;

// Stato chiamata
let incomingCallerId = null;   // chi mi sta chiamando
let pendingOffer = null;       // offer ricevuta prima di ACCETTARE
let inCall = false;            // siamo in una chiamata attiva?

const rtcConfig = {
    iceServers: [
        { urls: "stun:stun.l.google.com:19302" }
    ]
};

// ==========================
//  INIZIALIZZAZIONE
// ==========================
window.initTelevisit = function (myId) {
    localUserId = myId;

    localVideo = document.getElementById("localVideo");
    remoteVideo = document.getElementById("remoteVideo");

    if (!localVideo || !remoteVideo) {
        console.error("Elementi video mancanti (localVideo / remoteVideo).");
        return;
    }

    const proto    = location.protocol === "https:" ? "wss://" : "ws://";
    const basePath = location.pathname.split("/")[1];
    const wsUrl    = proto + location.host + "/" + basePath + "/ws/video/" + localUserId;

    rtcSocket = new WebSocket(wsUrl);

    rtcSocket.onopen = () => {
        console.log("WS WebRTC unificato connesso per utente:", localUserId);
    };

    rtcSocket.onmessage = async (msg) => {
        let data;
        try {
            data = JSON.parse(msg.data);
        } catch (e) {
            console.error("Errore parsing messaggio WebRTC:", e);
            return;
        }

        // Imposto sempre il remoteUserId dove ha senso
        if (data.from && data.type !== "offer-init") {
            remoteUserId = data.from;
        }

        switch (data.type) {
            // Notifica chiamata in arrivo (solo popup, nessun WebRTC ancora)
            case "offer-init":
                console.log("📞 Chiamata in arrivo da", data.from);
                incomingCallerId = data.from;
                showIncomingCallPopup();
                break;

            // OFFER vera e propria dal chiamante
            case "offer":
                console.log("📡 Ricevuta OFFER da", data.from);
                await onIncomingOffer(data);
                break;

            // ANSWER dal ricevente (quando sei tu il chiamante)
            case "answer":
                console.log("📡 Ricevuta ANSWER da", data.from);
                await handleAnswer(data);
                break;

            // ICE candidate
            case "candidate":
                if (pc && data.candidate) {
                    try {
                        await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
                    } catch (e) {
                        console.error("Errore addIceCandidate:", e);
                    }
                }
                break;

            // Hangup
            case "hangup":
                console.log("📴 Hangup ricevuto");
                endCallUI();
                closeIncomingCallPopup();
                break;
        }
    };

    rtcSocket.onerror = (err) => {
        console.error("Errore WS WebRTC:", err);
    };
};

// ==========================
//  CHIAMATA USCENTE
// ==========================

window.startOutgoingCall = async function (receiverId) {
    if (!rtcSocket || rtcSocket.readyState !== WebSocket.OPEN) {
        alert("Canale WebRTC non pronto.");
        return;
    }

    remoteUserId = receiverId;
    inCall = true;

    showVideoWindow();

    await setupPeerConnection();

    // 1) Notifica che lo stai chiamando (popup lato remoto)
    _send({
        type: "offer-init",
        to: remoteUserId
    });

    // 2) Crea e manda l'OFFER WebRTC
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);

    _send({
        type: "offer",
        to: remoteUserId,
        sdp: offer
    });

    console.log("📤 Inviata OFFER a", remoteUserId);
};

// ==========================
//  OFFER IN ENTRATA (quando sei il RICEVENTE)
// ==========================

async function onIncomingOffer(data) {
    // Se NON ho ancora accettato la chiamata → metto in attesa
    if (!inCall) {
        console.log("Memorizzo OFFER in pendingOffer, in attesa di ACCETTO");
        pendingOffer = data;
        return;
    }

    // Se invece ho già accettato → gestisco subito
    await handleOffer(data);
}

// Chi clicca "ACCETTA" sul popup
window.acceptIncomingCall = async function () {
    if (!incomingCallerId) {
        console.warn("Nessun callerId in incomingCallerId");
        return;
    }

    inCall = true;
    remoteUserId = incomingCallerId;

    closeIncomingCallPopup();
    showVideoWindow();

    await setupPeerConnection();

    // Se avevamo già ricevuto l'OFFER, ora la applichiamo
    if (pendingOffer) {
        console.log("Applico OFFER pendente dopo ACCETTA");
        await handleOffer(pendingOffer);
        pendingOffer = null;
    } else {
        console.log("Accettata chiamata, in attesa di OFFER dal chiamante...");
    }
};

// Chi clicca "RIFIUTA"
window.rejectIncomingCall = function () {
    if (!incomingCallerId) {
        closeIncomingCallPopup();
        return;
    }

    _send({
        type: "hangup",
        to: incomingCallerId
    });

    closeIncomingCallPopup();
    incomingCallerId = null;
};

// ==========================
//  GESTIONE OFFER / ANSWER
// ==========================

async function handleOffer(data) {
    if (!remoteUserId) {
        remoteUserId = data.from;
    }

    await setupPeerConnection();

    await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    _send({
        type: "answer",
        to: remoteUserId,
        sdp: answer
    });

    console.log("📤 Inviata ANSWER a", remoteUserId);
}

async function handleAnswer(data) {
    if (!pc) {
        console.warn("handleAnswer chiamata ma pc è nullo");
        return;
    }

    await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));
    console.log("ANSWER applicata");
}

// ==========================
//  PEER CONNECTION + MEDIA
// ==========================

async function setupPeerConnection() {
    if (pc) return;

    pc = new RTCPeerConnection(rtcConfig);

    // Stream remoto
    remoteStream = new MediaStream();
    remoteVideo.srcObject = remoteStream;

    pc.ontrack = (event) => {
        console.log("🎥 Track remota aggiunta");
        remoteStream.addTrack(event.track);

        const overlay = document.getElementById("callWaitingOverlay");
        if (overlay) overlay.style.display = "none";
    };

    pc.onicecandidate = (event) => {
        if (event.candidate) {
            _send({
                type: "candidate",
                to: remoteUserId,
                candidate: event.candidate
            });
        }
    };

    // Stream locale
    localStream = await navigator.mediaDevices.getUserMedia({
        video: true,
        audio: true
    });

    localVideo.srcObject = localStream;

    localStream.getTracks().forEach(t =>
        pc.addTrack(t, localStream)
    );
}

// ==========================
//  UI: FINESTRA VIDEO / POPUP
// ==========================

function showVideoWindow() {
    const win = document.getElementById("videoCallWindow");
    if (!win) return;

    win.style.display = "flex";

    const overlay = document.getElementById("callWaitingOverlay");
    if (overlay) overlay.style.display = "flex";
}

window.closeVideoCall = function () {
    if (remoteUserId) {
        _send({
            type: "hangup",
            to: remoteUserId
        });
    }
    endCallUI();
};

function endCallUI() {
    inCall = false;
    incomingCallerId = null;
    pendingOffer = null;

    if (pc) {
        pc.close();
        pc = null;
    }

    if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
        localStream = null;
    }

    if (localVideo) localVideo.srcObject = null;
    if (remoteVideo) remoteVideo.srcObject = null;

    const overlay = document.getElementById("callWaitingOverlay");
    if (overlay) overlay.style.display = "flex";

    const win = document.getElementById("videoCallWindow");
    if (win) win.style.display = "none";

    console.log("Chiamata terminata / stato resettato");
}

// ==========================
//  POPUP CHIAMATA IN ARRIVO
// ==========================

function showIncomingCallPopup() {
    if (document.getElementById("incomingCallPopup")) return;

    const div = document.createElement("div");
    div.id = "incomingCallPopup";
    div.style.cssText = `
        position:fixed;
        top:20px; right:20px;
        background:#fff;
        padding:16px 20px;
        border-radius:10px;
        box-shadow:0 4px 14px rgba(0,0,0,0.25);
        z-index:10000;
        font-family:sans-serif;
    `;

    div.innerHTML = `
        <div style="font-weight:600; margin-bottom:8px;">📞 Chiamata in arrivo</div>
        <div style="display:flex; gap:8px; justify-content:flex-end;">
            <button onclick="acceptIncomingCall()"
                    style="padding:6px 12px; border-radius:6px; border:none; background:#28a745; color:#fff; cursor:pointer;">
                Accetta
            </button>
            <button onclick="rejectIncomingCall()"
                    style="padding:6px 12px; border-radius:6px; border:none; background:#dc3545; color:#fff; cursor:pointer;">
                Rifiuta
            </button>
        </div>
    `;

    document.body.appendChild(div);
}

function closeIncomingCallPopup() {
    const p = document.getElementById("incomingCallPopup");
    if (p) p.remove();
}

// ==========================
//  MUTE / CAM
// ==========================

window.toggleMic = function () {
    if (!localStream) return;
    micEnabled = !micEnabled;
    localStream.getAudioTracks().forEach(t => t.enabled = micEnabled);

    const btn = document.getElementById("btnMic");
    if (btn) btn.textContent = micEnabled ? "🎤" : "🔇";
};

window.toggleCam = function () {
    if (!localStream) return;
    camEnabled = !camEnabled;
    localStream.getVideoTracks().forEach(t => t.enabled = camEnabled);

    const btn = document.getElementById("btnCam");
    if (btn) btn.textContent = camEnabled ? "📷" : "🚫";
};

// ==========================
//  UTILITY SEND
// ==========================
function _send(obj) {
    if (!rtcSocket || rtcSocket.readyState !== WebSocket.OPEN) {
        console.error("WS non pronto per inviare:", obj);
        return;
    }
    rtcSocket.send(JSON.stringify(obj));
}
