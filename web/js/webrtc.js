/* 
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/JavaScript.js to edit this template
 */
// =======================================
// WebRTC – Televisita bidirezionale
// =======================================

let localStream = null;
let remoteStream = null;
let pc = null;
let isCaller = false;

// Config STUN server pubblico
const rtcConfig = {
    iceServers: [
        { urls: "stun:stun.l.google.com:19302" }
    ]
};

// Riferimenti ai video (li devi avere nella JSP)
let localVideo = null;
let remoteVideo = null;

// Inizializza i riferimenti DOM
export function initTelevisit(localId, remoteId) {
    localVideo = document.getElementById("localVideo");
    remoteVideo = document.getElementById("remoteVideo");

    if (!localVideo || !remoteVideo) {
        console.error("Video elements non trovati");
        return;
    }

    setupSocket(localId, remoteId);
}


// =======================================
// WebSocket segnalazione WebRTC (riusa il WS chat)
// =======================================

let rtcSocket = null;

function setupSocket(localId, remoteId) {
    const proto = (location.protocol === "https:") ? "wss://" : "ws://";
    const wsUrl = proto + location.host + ctx + "/ws/rtc/" + localId;

    rtcSocket = new WebSocket(wsUrl);

    rtcSocket.onmessage = async (event) => {
        const data = JSON.parse(event.data);

        if (data.type === "offer") {
            await handleOffer(data);
        } else if (data.type === "answer") {
            await handleAnswer(data);
        } else if (data.type === "candidate") {
            await handleCandidate(data);
        }
    };
}


// =======================================
// Avvio televisita
// =======================================

export async function startTelevisita() {
    isCaller = true;

    await setupPeerConnection();

    // Crea l’offerta
    const offer = await pc.createOffer();
    await pc.setLocalDescription(offer);

    // Invia al destinatario
    rtcSocket.send(JSON.stringify({
        type: "offer",
        sdp: offer
    }));
}


// =======================================
// Peer connection
// =======================================

async function setupPeerConnection() {
    pc = new RTCPeerConnection(rtcConfig);

    // Quando arrivano candidate ICE
    pc.onicecandidate = (e) => {
        if (e.candidate) {
            rtcSocket.send(JSON.stringify({
                type: "candidate",
                candidate: e.candidate
            }));
        }
    };

    // Stream remoto
    pc.ontrack = (event) => {
        if (!remoteStream) {
            remoteStream = new MediaStream();
            remoteVideo.srcObject = remoteStream;
        }
        remoteStream.addTrack(event.track);
    };

    // Stream locale
    localStream = await navigator.mediaDevices.getUserMedia({ video: true, audio: true });
    localStream.getTracks().forEach(track => pc.addTrack(track, localStream));
    localVideo.srcObject = localStream;
}


// =======================================
// Gestione segnali
// =======================================

async function handleOffer(data) {
    await setupPeerConnection();

    await pc.setRemoteDescription(data.sdp);

    const answer = await pc.createAnswer();
    await pc.setLocalDescription(answer);

    rtcSocket.send(JSON.stringify({
        type: "answer",
        sdp: answer
    }));
}

async function handleAnswer(data) {
    await pc.setRemoteDescription(data.sdp);
}

async function handleCandidate(data) {
    try {
        await pc.addIceCandidate(new RTCIceCandidate(data.candidate));
    } catch (e) {
        console.error("ICE error", e);
    }
}


// =======================================
// Condivisione schermo
// =======================================

export async function shareScreen() {
    const screenStream = await navigator.mediaDevices.getDisplayMedia({
        video: true
    });

    const screenTrack = screenStream.getVideoTracks()[0];

    // Rimpiazza la camera
    const sender = pc.getSenders().find(s => s.track.kind === "video");
    sender.replaceTrack(screenTrack);

    screenTrack.onended = () => {
        sender.replaceTrack(localStream.getVideoTracks()[0]);
    }
}



