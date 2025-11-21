package controllers;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/video/{userId}")
public class VideoSignalEndpoint {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

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

            Session dest = SESSIONS.get(toId);
            if (dest != null && dest.isOpen()) {

                // arricchiamo il JSON con "from": <mittente>
                String enriched = injectFromField(message, fromId);

                // *** FIX: invio async per evitare race e blocchi ***
                dest.getAsyncRemote().sendText(enriched);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Object idObj = session.getUserProperties().get("userId");
        if (idObj instanceof Long) {
            SESSIONS.remove((Long) idObj);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

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
            while (pos < json.length() &&
                  (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '-')) pos++;

            String num = json.substring(start, pos).trim();
            return Long.parseLong(num);

        } catch (Exception e) {
            return null;
        }
    }

    private String injectFromField(String json, Long fromId) {

        if (json.contains("\"from\"")) return json;

        int brace = json.indexOf("{");
        if (brace < 0) return json;

        return json.substring(0, brace + 1)
                + "\"from\":" + fromId + ","
                + json.substring(brace + 1);
    }
}
