// =====================================================
//                 TELEVISITA WEBRTC UNIFICATO
//          Versione migliorata e corretta (2025)
// =====================================================

// Identità
let localUserId = null;
let remoteUserId = null;

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
let incomingCallerId = null; 
let pendingOffer = null;
let inCall = false;

// --- NOVITÀ: Coda per i pacchetti arrivati troppo presto ---
let iceCandidatesQueue = []; 

const rtcConfig = {
  iceServers: [
    {
    
    }
  ]
};

// =====================================================
//                INIZIALIZZAZIONE
// =====================================================

// --- AGGIUNGI QUESTA VARIABILE GLOBALE ---
let heartbeatInterval = null; 

window.initTelevisit = function(myId) {
    localUserId = myId;

    localVideo = document.getElementById("localVideo");
    remoteVideo = document.getElementById("remoteVideo");

    if (!localVideo || !remoteVideo) {
        console.error("Elementi localVideo / remoteVideo mancanti.");
        return;
    }

    // Costruzione URL WebSocket
    const proto = location.protocol === "https:" ? "wss://" : "ws://";
    const seg = location.pathname.split("/").filter(s => s);
    const base = seg.length > 0 ? `/${seg[0]}` : "";
    const wsUrl = `${proto}${location.host}${base}/ws/video/${localUserId}`;

    rtcSocket = new WebSocket(wsUrl);

    // ==========================================================
    // 1. ON OPEN: AVVIA IL HEARTBEAT (PING)
    // ==========================================================
    rtcSocket.onopen = () => {
        console.log("WebSocket WebRTC connesso:", wsUrl);
        
        // Pulisci eventuali vecchi intervalli
        if (heartbeatInterval) clearInterval(heartbeatInterval);

        // Invia un pacchetto vuoto ogni 30 secondi per tenere vivo il tunnel
        heartbeatInterval = setInterval(() => {
            if (rtcSocket.readyState === WebSocket.OPEN) {
                // Inviamo un JSON valido così il server Java non si lamenta
                rtcSocket.send(JSON.stringify({ type: "ping", from: localUserId }));
                console.log("Ping inviato (Keep-Alive)");
            }
        }, 30000); // 30 secondi
    };

    rtcSocket.onerror = (e) => console.error("Errore WebSocket WebRTC:", e);

    // ==========================================================
    // 2. ON CLOSE: RIAVVIA LA CONNESSIONE (AUTO-RECONNECT)
    // ==========================================================
    rtcSocket.onclose = () => {
        console.warn("WebSocket chiuso. Tentativo di riconnessione in 3 secondi...");
        
        // Ferma il heartbeat per non sprecare risorse
        if (heartbeatInterval) clearInterval(heartbeatInterval);

        if (inCall) {
            endCallUI(); // Se eri in chiamata, purtroppo cade
        }

        // Riprova a connetterti tra 3 secondi
        setTimeout(() => {
            console.log("Riconnessione in corso...");
            window.initTelevisit(localUserId);
        }, 3000);
    };

    rtcSocket.onmessage = async (msg) => {
        let data;
        try { data = JSON.parse(msg.data); }
        catch (e) {
            console.error("Errore parsing WebSocket:", e);
            return;
        }

        // Ignora i tuoi stessi messaggi (se il server fa echo)
        if (data.from === localUserId) return;

        // BLOCCO: filtra messaggi da terzi se sei già in chiamata
        if (inCall && data.from && data.from !== remoteUserId) {
            console.warn(`Messaggio ignorato da ${data.from}, sei in call con ${remoteUserId}`);
            if (data.type === "offer-init" || data.type === "offer") {
                _send({ type: "busy", to: data.from });
            }
            return;
        }

        // SWITCH principale
        switch (data.type) {

            case "offer-init":
                if (inCall) {
                    _send({ type: "busy", to: data.from });
                    return;
                }
                incomingCallerId = data.from;
                showIncomingCallPopup(incomingCallerId);
                break;

            case "offer":
                await onIncomingOffer(data);
                break;

            case "answer":
                await handleAnswer(data);
                break;

            case "candidate":
                if (data.candidate) {
                    const candidate = new RTCIceCandidate(data.candidate);
                    
                    // GESTIONE CODA (Già corretta prima)
                    if (!pc || !pc.remoteDescription || !pc.remoteDescription.type) {
                        console.log("Candidato ICE arrivato in anticipo -> Messo in coda.");
                        iceCandidatesQueue.push(candidate);
                    } else {
                        try { await pc.addIceCandidate(candidate); }
                        catch (e) { console.error("ICE error immediato:", e); }
                    }
                }
                break;

            case "hangup":
                endCallUI();
                closeIncomingCallPopup();
                break;

            case "busy":
                alert(`L'utente ${data.from} è occupato.`);
                endCallUI();
                break;
                
            // Il server Java ignorerà il tipo "ping", quindi non serve un case qui
        }
    };
};

// =====================================================
//                  CHIAMATA USCENTE
// =====================================================

window.startOutgoingCall = async function(receiverId) {
    if (!rtcSocket || rtcSocket.readyState !== WebSocket.OPEN) {
        alert("Canale WebRTC non pronto.");
        return;
    }

    if (inCall) {
        alert("Sei già in chiamata.");
        return;
    }

    remoteUserId = receiverId;
    inCall = true;

    showVideoWindow();

    const ok = await setupPeerConnection();
    if (!ok) { endCallUI(); return; }

    _send({ type: "offer-init", to: remoteUserId });

    try {
        const offer = await pc.createOffer();
        await pc.setLocalDescription(offer);

        _send({ type: "offer", to: remoteUserId, sdp: offer });
    } catch (e) {
        console.error("Errore createOffer:", e);
        endCallUI();
    }
};

// =====================================================
//          OFFER IN ENTRATA (solo ricevente)
// =====================================================

async function onIncomingOffer(data) {
    if (!inCall) {
        pendingOffer = data;
        return;
    }
    await handleOffer(data);
}

window.acceptIncomingCall = async function() {
    if (!incomingCallerId) return;

    remoteUserId = incomingCallerId;
    inCall = true;

    closeIncomingCallPopup();
    showVideoWindow();

    const ok = await setupPeerConnection();
    if (!ok) {
        _send({ type: "hangup", to: remoteUserId });
        endCallUI();
        return;
    }

    if (pendingOffer) {
        await handleOffer(pendingOffer);
        pendingOffer = null;
    }
};

window.rejectIncomingCall = function() {
    if (incomingCallerId)
        _send({ type: "hangup", to: incomingCallerId });

    incomingCallerId = null;
    pendingOffer = null;
    closeIncomingCallPopup();
};

// =====================================================
//                 HANDLE OFFER / ANSWER
// =====================================================

// --- FUNZIONE HELPER PER SVUOTARE LA CODA ---
async function processIceQueue() {
    if (iceCandidatesQueue.length > 0) {
        console.log(`Applicazione di ${iceCandidatesQueue.length} candidati dalla coda...`);
        for (const candidate of iceCandidatesQueue) {
            try {
                await pc.addIceCandidate(candidate);
            } catch (e) {
                console.error("Errore candidati in coda:", e);
            }
        }
        iceCandidatesQueue = []; // Svuota
    }
}
// --------------------------------------------

async function handleOffer(data) {
    if (!remoteUserId) remoteUserId = data.from;

    await setupPeerConnection();

    try {
        await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));
        
        // --- ORA CHE ABBIAMO SETTATO LA REMOTE DESC, PROCESSIA LA CODA ---
        await processIceQueue();
        // -----------------------------------------------------------------

        const answer = await pc.createAnswer();
        await pc.setLocalDescription(answer);

        _send({ type: "answer", to: remoteUserId, sdp: answer });

    } catch (e) {
        console.error("Errore handleOffer:", e);
    }
}

async function handleAnswer(data) {
    if (!pc) return;
    try {
        await pc.setRemoteDescription(new RTCSessionDescription(data.sdp));
        
        // --- ORA CHE ABBIAMO SETTATO LA REMOTE DESC, PROCESSIA LA CODA ---
        await processIceQueue();
        // -----------------------------------------------------------------

    } catch (e) {
        console.error("Errore handleAnswer:", e);
    }
}

// =====================================================
//         CREAZIONE + CONFIGURAZIONE PEER CONNECTION
// =====================================================

async function setupPeerConnection() {
    if (pc) return true;

    pc = new RTCPeerConnection(rtcConfig);

    pc.onconnectionstatechange = () => {
        console.log("ConnState:", pc.connectionState);

        if (["failed", "disconnected", "closed"].includes(pc.connectionState)) {
            if (inCall) {
                console.warn("Connessione persa.");
                endCallUI();
            }
        }
    };

    remoteStream = new MediaStream();
    remoteVideo.srcObject = remoteStream;

    pc.ontrack = (ev) => {
        remoteStream.addTrack(ev.track);
        const overlay = document.getElementById("callWaitingOverlay");
        if (overlay) overlay.style.display = "none";
    };

    pc.onicecandidate = (ev) => {
        if (ev.candidate && remoteUserId) {
            _send({ type: "candidate", to: remoteUserId, candidate: ev.candidate });
        }
    };

    try {
        localStream = await navigator.mediaDevices.getUserMedia({
            video: true,
            audio: true
        });

        localVideo.srcObject = localStream;
        micEnabled = true;
        camEnabled = true;
        updateMediaButtonsUI();

        localStream.getTracks().forEach(t => pc.addTrack(t, localStream));

        return true;

    } catch (e) {
        alert("Impossibile accedere a fotocamera o microfono.");
        return false;
    }
}

// =====================================================
//                  UI: FINESTRA VIDEO
// =====================================================

function showVideoWindow() {
    const win = document.getElementById("videoCallWindow");
    if (!win) return;

    win.style.display = "flex";

    const overlay = document.getElementById("callWaitingOverlay");
    if (overlay) overlay.style.display = "flex";
}

window.closeVideoCall = function() {
    if (remoteUserId) _send({ type: "hangup", to: remoteUserId });
    endCallUI();
};

function endCallUI() {
    inCall = false;
    incomingCallerId = null;
    pendingOffer = null;
    remoteUserId = null;
    
    // Pulizia coda candidati
    iceCandidatesQueue = [];

    if (pc) pc.close();
    pc = null;

    if (localStream) {
        localStream.getTracks().forEach(t => t.stop());
        localStream = null;
    }

    if (localVideo) localVideo.srcObject = null;
    if (remoteVideo) remoteVideo.srcObject = null;

    const win = document.getElementById("videoCallWindow");
    if (win) win.style.display = "none";

    const overlay = document.getElementById("callWaitingOverlay");
    if (overlay) overlay.style.display = "flex";
}

// =====================================================
//              POPUP CHIAMATA IN ARRIVO (RIDOTTO)
// =====================================================

function showIncomingCallPopup(callerId) {
    // Chiudi eventuale popup precedente
    closeIncomingCallPopup();

    const div = document.createElement("div");
    div.id = "incomingCallPopup";

    div.style.cssText = `
        position: fixed;
        bottom: 20px;
        right: 20px;
        background: #ffffff;
        padding: 10px 12px;
        border-radius: 6px;
        box-shadow: 0 2px 8px rgba(0,0,0,0.2);
        z-index: 9999;
        font-family: system-ui, -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
        min-width: 220px;
        max-width: 260px;
        border-left: 3px solid #28a745;
        display: flex;
        flex-direction: column;
        gap: 6px;
    `;

    div.innerHTML = `
        <div style="
            font-size: 13px;
            font-weight: 600;
            color: #1f7a3d;
            background: #e8f7ee;
            padding: 4px 6px;
            border-radius: 4px;
            display: inline-block;
        ">
            📞 Chiamata in arrivo
        </div>

        <div style="font-size: 12px; color: #555;">
            Chiamante:<br>
            <strong style="word-break: break-word; font-size: 13px; color: #222;">
                ${callerId || "Sconosciuto"}
            </strong>
        </div>

        <div style="display: flex; justify-content: flex-end; gap: 6px; margin-top: 4px;">
            <button
                type="button"
                onclick="rejectIncomingCall()"
                style="
                    padding: 4px 8px;
                    font-size: 12px;
                    border-radius: 4px;
                    border: 1px solid #dc3545;
                    background: #ffffff;
                    color: #dc3545;
                    cursor: pointer;
                ">
                Rifiuta
            </button>

            <button
                type="button"
                onclick="acceptIncomingCall()"
                style="
                    padding: 4px 8px;
                    font-size: 12px;
                    border-radius: 4px;
                    border: 1px solid #28a745;
                    background: #28a745;
                    color: #ffffff;
                    cursor: pointer;
                ">
                Accetta
            </button>
        </div>
    `;

    document.body.appendChild(div);
}




function closeIncomingCallPopup() {
    const p = document.getElementById("incomingCallPopup");
    if (p) p.remove();
}

// =====================================================
//                 MUTE / CAM
// =====================================================

window.toggleMic = function() {
    if (!localStream) return;
    micEnabled = !micEnabled;
    localStream.getAudioTracks().forEach(t => t.enabled = micEnabled);
    updateMediaButtonsUI();
};

window.toggleCam = function() {
    if (!localStream) return;
    camEnabled = !camEnabled;
    localStream.getVideoTracks().forEach(t => t.enabled = camEnabled);
    updateMediaButtonsUI();
};

function updateMediaButtonsUI() {
    const btnMic = document.getElementById("btnMic");
    const btnCam = document.getElementById("btnCam");

    if (btnMic) btnMic.textContent = micEnabled ? "🎤" : "🔇";
    if (btnCam) btnCam.textContent = camEnabled ? "📷" : "🚫";
}

// =====================================================
//                 UTILITY SEND
// =====================================================

function _send(obj) {
    if (!rtcSocket || rtcSocket.readyState !== WebSocket.OPEN) return;

    if (!obj.from) obj.from = localUserId;
    rtcSocket.send(JSON.stringify(obj));
}