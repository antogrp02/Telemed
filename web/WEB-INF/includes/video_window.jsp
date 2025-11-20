<%-- 
    Document   : video_window
    Created on : 20 nov 2025, 23:01:16
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>

<style>
    /* ===========================
       FINESTRA VIDEO GLOBALE
       =========================== */

    #videoCallWindow {
        position: fixed;
        top: 50%;
        left: 50%;
        width: 70vw;
        height: 70vh;
        transform: translate(-50%, -50%);
        background: #000;
        border-radius: 16px;
        overflow: hidden;

        display: none;
        z-index: 9999;

        box-shadow: 0 0 30px rgba(0,0,0,0.35);
    }

    #remoteVideoContainer {
        width: 100%;
        height: 100%;
        position: relative;
        background: black;
    }

    #remoteVideo {
        width: 100%;
        height: 100%;
        object-fit: cover;
    }

    #localVideo {
        position: absolute;
        bottom: 15px;
        right: 15px;
        width: 180px;
        height: 130px;
        border-radius: 12px;
        background: #111;
        object-fit: cover;
        border: 2px solid white;
    }

    /* Overlay stile WhatsApp */
    #callWaitingOverlay {
        position: absolute;
        inset: 0;
        background: rgba(0,0,0,0.65);
        display: flex;
        flex-direction: column;
        align-items: center;
        justify-content: center;

        color: white;
        font-size: 18px;
        font-weight: 500;
        z-index: 20;
    }

    .spinner {
        width: 40px;
        height: 40px;
        border: 4px solid rgba(255,255,255,0.4);
        border-top-color: white;
        border-radius: 50%;
        animation: spin 1s linear infinite;
    }

    @keyframes spin {
        to {
            transform: rotate(360deg);
        }
    }

    /* Pulsanti in stile WhatsApp */
    .video-controls {
        position: absolute;
        bottom: 20px;
        left: 50%;
        transform: translateX(-50%);
        display: flex;
        gap: 16px;
        z-index: 30;
    }

    .control-btn {
        width: 56px;
        height: 56px;
        border-radius: 50%;
        background: rgba(255,255,255,0.15);
        backdrop-filter: blur(3px);
        display: flex;
        align-items: center;
        justify-content: center;
        font-size: 24px;
        cursor: pointer;
        border: 2px solid white;
        transition: 0.2s;
    }

    .control-btn:hover {
        transform: scale(1.08);
        background: rgba(255,255,255,0.25);
    }

    .end-call {
        background: #ff3b30 !important;
        border-color: #ff3b30 !important;
    }

    .end-call:hover {
        background: #ff4f48 !important;
    }

</style>


<!-- ====== FINESTRA VIDEO ====== -->
<div id="videoCallWindow">
    <div id="remoteVideoContainer">
        <video id="remoteVideo" autoplay playsinline></video>
        <video id="localVideo" autoplay muted playsinline></video>

        <div id="callWaitingOverlay">
            <div class="spinner"></div>
            <p style="font-size:22px;margin:12px 0 0 0;">Chiamata in corso…</p>
            <p style="font-size:14px;opacity:0.8;">In attesa dell'altro utente…</p>
        </div>

        <div class="video-controls">
            <div id="btnMic" class="control-btn" onclick="toggleMic()">🎤</div>
            <div id="btnCam" class="control-btn" onclick="toggleCam()">📷</div>
            <div class="control-btn end-call" onclick="closeVideoCall()">✖</div>
        </div>
    </div>
</div>



