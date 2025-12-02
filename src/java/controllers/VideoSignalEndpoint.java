package controllers;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/video/{userId}")
public class VideoSignalEndpoint {

    // Sessioni WebSocket attive
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    // MUTUA ESCLUSIONE: chi è in chiamata con chi (chi → con chi)
    private static final Map<Long, Long> activeCalls = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        session.getUserProperties().put("userId", userId);
        SESSIONS.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long fromId = (Long) session.getUserProperties().get("userId");
            Long toId   = extractToUserId(message);
            if (toId == null) return;

            // ============================================================
            // 1. BLOCCO OFFER / OFFER-INIT SE L'UTENTE È OCCUPATO
            // ============================================================
            if (message.contains("\"offer-init\"") || message.contains("\"offer\"")) {

                // destinatario impegnato?
                if (activeCalls.containsKey(toId)) {
                    sendBusyTo(fromId);
                    return;
                }

                // mittente impegnato?
                if (activeCalls.containsKey(fromId)) {
                    sendBusyTo(fromId);
                    return;
                }
            }

            // ============================================================
            // 2. SE È UNA ANSWER → segna entrambi in chiamata
            // ============================================================
            if (message.contains("\"answer\"")) {
                activeCalls.put(fromId, toId);
                activeCalls.put(toId, fromId);
            }

            // ============================================================
            // 3. SE È UN HANGUP → libera gli utenti
            // ============================================================
            if (message.contains("\"hangup\"")) {
                activeCalls.remove(fromId);
                activeCalls.remove(toId);
            }

            // ============================================================
            // 4. INVIO NORMALE AL DESTINATARIO
            // ============================================================
            Session dest = SESSIONS.get(toId);
            if (dest != null && dest.isOpen()) {

                // arricchiamo con "from": <mittente>
                String enriched = injectFromField(message, fromId);

                dest.getAsyncRemote().sendText(enriched);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Object idObj = session.getUserProperties().get("userId");
        if (idObj instanceof Long userId) {

            // se era in chiamata → libero l’altro utente
            Long other = activeCalls.get(userId);
            if (other != null) {
                activeCalls.remove(other);
                activeCalls.remove(userId);

                // avvisa l'altro che la call è terminata
                Session peer = SESSIONS.get(other);
                if (peer != null && peer.isOpen()) {
                    peer.getAsyncRemote().sendText("{\"type\":\"hangup\",\"from\":" + userId + "}");
                }
            }

            SESSIONS.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }


    // ============================================================
    //   UTILITY JSON: estrai "to": <id>
    // ============================================================
    private Long extractToUserId(String json) {
        try {
            String key = "\"to\"";
            int idx = json.indexOf(key);
            if (idx < 0) return null;

            int colon = json.indexOf(":", idx);
            if (colon < 0) return null;

            int pos = colon + 1;
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;

            int start = pos;
            while (pos < json.length()
                    && (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '-'))
                pos++;

            String num = json.substring(start, pos).trim();

            return Long.parseLong(num);

        } catch (Exception e) {
            return null;
        }
    }

    // ============================================================
    //   UTILITY JSON: aggiungi campo "from": <id>
    // ============================================================
    private String injectFromField(String json, Long fromId) {

        if (json.contains("\"from\""))
            return json;

        int brace = json.indexOf("{");
        if (brace < 0) return json;

        return json.substring(0, brace + 1)
                + "\"from\":" + fromId + ","
                + json.substring(brace + 1);
    }

    // ============================================================
    //   INVIA MESSAGGIO DI OCCUPATO
    // ============================================================
    private void sendBusyTo(Long userId) {
        try {
            Session s = SESSIONS.get(userId);
            if (s != null && s.isOpen()) {
                String pay = "{\"type\":\"busy\",\"from\":0}";
                s.getAsyncRemote().sendText(pay);
            }
        } catch (Exception ignored) {}
    }
}
