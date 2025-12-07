package controllers;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@ServerEndpoint("/ws/video/{userId}")
public class VideoSignalEndpoint {

    // Sessioni WebSocket attive
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    // MUTUA ESCLUSIONE: chi è in chiamata con chi
    private static final Map<Long, Long> activeCalls = new ConcurrentHashMap<>();

    // MEMORIA 1: OFFERTE SDP (Il "contratto" tecnico della chiamata)
    private static final Map<Long, String> pendingOffers = new ConcurrentHashMap<>();

    // MEMORIA 2: ICE CANDIDATES (Le "strade" per connettersi - Buffer per lo schermo nero)
    private static final Map<Long, List<String>> pendingCandidates = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        session.getUserProperties().put("userId", userId);
        SESSIONS.put(userId, session);

        // ================================================================
        //  CHECK POST-REFRESH: Recupero chiamata persa
        // ================================================================
        if (pendingOffers.containsKey(userId)) {
            String savedOfferJson = pendingOffers.get(userId);
            Long callerId = extractFromUserId(savedOfferJson);

            Session callerSession = SESSIONS.get(callerId);
            
            // Se il chiamante è ancora online, ripristiniamo tutto
            if (callerId != null && callerSession != null && callerSession.isOpen()) {
                System.out.println("Utente " + userId + " riconnesso. Ripristino chiamata da " + callerId);

                // 1. Inviamo lo squillo (Per far apparire il POPUP)
                String ring = "{\"type\":\"offer-init\", \"from\":" + callerId + "}";
                session.getAsyncRemote().sendText(ring);

                // 2. Inviamo l'offerta tecnica (SDP) (Per la connessione WebRTC)
                session.getAsyncRemote().sendText(savedOfferJson);

                // 3. Inviamo tutti i CANDIDATI ICE accumulati (Per evitare schermo nero)
                if (pendingCandidates.containsKey(userId)) {
                    List<String> missedCandidates = pendingCandidates.get(userId);
                    for (String cand : missedCandidates) {
                        session.getAsyncRemote().sendText(cand);
                    }
                    // Una volta inviati, puliamo il buffer
                    pendingCandidates.remove(userId);
                }

            } else {
                // Il chiamante ha chiuso mentre noi ricaricavamo, pulizia
                pendingOffers.remove(userId);
                pendingCandidates.remove(userId);
            }
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long fromId = (Long) session.getUserProperties().get("userId");

            // --- FILTER KEEP-ALIVE: Ignora i ping del JavaScript ---
            if (message.contains("\"ping\"")) return;

            Long toId = extractToUserId(message);
            if (toId == null) return;

            // Arricchiamo il messaggio col mittente se manca
            String enrichedMessage = injectFromField(message, fromId);

            // ============================================================
            // 1. OFFERTE (SALVATAGGIO)
            // ============================================================
            if (message.contains("\"offer-init\"") || message.contains("\"offer\"")) {
                
                // Controllo occupato
                if (activeCalls.containsKey(toId) || activeCalls.containsKey(fromId)) {
                    sendBusyTo(fromId);
                    return;
                }

                // Salviamo l'offerta completa se è quella tecnica (SDP)
                // Così se l'utente ricarica, la ritrova.
                if (message.contains("\"offer\"") && !message.contains("\"offer-init\"")) {
                     pendingOffers.put(toId, enrichedMessage);
                }
            }

            // ============================================================
            // 2. CANDIDATI ICE (BUFFERING)
            // ============================================================
            if (message.contains("\"candidate\"")) {
                Session dest = SESSIONS.get(toId);
                
                // Se il destinatario è OFFLINE oppure ha un'offerta pendente (sta ricaricando)
                // Salviamo i candidati invece di perderli
                if (dest == null || !dest.isOpen() || pendingOffers.containsKey(toId)) {
                    pendingCandidates.computeIfAbsent(toId, k -> new CopyOnWriteArrayList<>()).add(enrichedMessage);
                    return; // Non proviamo a inviarlo ora, lo invierà onOpen
                }
            }

            // ============================================================
            // 3. PULIZIA (ANSWER / HANGUP)
            // ============================================================
            if (message.contains("\"answer\"")) {
                cleanupPendingData(fromId, toId);
                activeCalls.put(fromId, toId);
                activeCalls.put(toId, fromId);
            }

            if (message.contains("\"hangup\"")) {
                activeCalls.remove(fromId);
                activeCalls.remove(toId);
                cleanupPendingData(fromId, toId);
                
                // Rimuovi le mie offerte verso altri (se chiudo mentre chiamo)
                cleanupMyOutgoingData(fromId);
            }

            // ============================================================
            // 4. INOLTRO DIRETTO (Se l'utente è online)
            // ============================================================
            Session dest = SESSIONS.get(toId);
            if (dest != null && dest.isOpen()) {
                dest.getAsyncRemote().sendText(enrichedMessage);
            } 

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Object idObj = session.getUserProperties().get("userId");
        if (idObj instanceof Long userId) {

            // Se cade la linea durante una chiamata attiva
            Long other = activeCalls.get(userId);
            if (other != null) {
                activeCalls.remove(other);
                activeCalls.remove(userId);
                Session peer = SESSIONS.get(other);
                if (peer != null && peer.isOpen()) {
                    peer.getAsyncRemote().sendText("{\"type\":\"hangup\",\"from\":" + userId + "}");
                }
            }

            // Se me ne vado io che STAVO CHIAMANDO, rimuovo le offerte per l'altro
            cleanupMyOutgoingData(userId);

            // NOTA: Se io ero il RICEVENTE (es. refresh), NON tocco pendingOffers/Candidates
            // così al rientro (onOpen) li ritrovo.

            SESSIONS.remove(userId);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

    // --- HELPER METHODS PER PULIZIA ---
    
    private void cleanupPendingData(Long id1, Long id2) {
        pendingOffers.remove(id1);
        pendingOffers.remove(id2);
        pendingCandidates.remove(id1);
        pendingCandidates.remove(id2);
    }

    private void cleanupMyOutgoingData(Long myId) {
        // Rimuove offerte dove "from" == myId
        pendingOffers.entrySet().removeIf(entry -> {
            Long sender = extractFromUserId(entry.getValue());
            return sender != null && sender.equals(myId);
        });
        // I candidati orfani scadranno al prossimo riavvio o verranno sovrascritti
    }


    // --- UTILITIES JSON ---

    private Long extractToUserId(String json) { return extractId(json, "\"to\""); }
    private Long extractFromUserId(String json) { return extractId(json, "\"from\""); }

    private Long extractId(String json, String key) {
        try {
            int idx = json.indexOf(key);
            if (idx < 0) return null;
            int colon = json.indexOf(":", idx);
            if (colon < 0) return null;
            int pos = colon + 1;
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) pos++;
            int start = pos;
            while (pos < json.length() && (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '-')) pos++;
            String num = json.substring(start, pos).trim();
            return Long.parseLong(num);
        } catch (Exception e) { return null; }
    }

    private String injectFromField(String json, Long fromId) {
        if (json.contains("\"from\"")) return json;
        int brace = json.indexOf("{");
        if (brace < 0) return json;
        return json.substring(0, brace + 1) + "\"from\":" + fromId + "," + json.substring(brace + 1);
    }

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