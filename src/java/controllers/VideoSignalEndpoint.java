/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/video/{userId}")
public class VideoSignalEndpoint {

    // userId -> sessione WebSocket
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    @OnOpen
    public void onOpen(Session session, @PathParam("userId") Long userId) {
        session.getUserProperties().put("userId", userId);
        SESSIONS.put(userId, session);
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            // messaggio JSON di tipo:
            // { "to": 123, "type": "offer|answer|candidate|hangup", ... }
            Long to = extractToUserId(message);
            if (to == null) return;

            Session dest = SESSIONS.get(to);
            if (dest != null && dest.isOpen()) {
                dest.getBasicRemote().sendText(message);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @OnClose
    public void onClose(Session session, CloseReason reason) {
        Object idObj = session.getUserProperties().get("userId");
        if (idObj instanceof Long) {
            Long id = (Long) idObj;
            SESSIONS.remove(id);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

    /**
     * Estrae il campo "to" dal JSON in modo minimale
     * senza dipendere da librerie esterne.
     */
    private Long extractToUserId(String json) {
        try {
            String key = "\"to\"";
            int idx = json.indexOf(key);
            if (idx < 0) return null;

            int colon = json.indexOf(":", idx);
            if (colon < 0) return null;

            int pos = colon + 1;
            // salta spazi
            while (pos < json.length() && Character.isWhitespace(json.charAt(pos))) {
                pos++;
            }

            // leggi numero fino a virgola o chiusura
            int start = pos;
            while (pos < json.length()
                    && (Character.isDigit(json.charAt(pos)) || json.charAt(pos) == '-')) {
                pos++;
            }

            String num = json.substring(start, pos).trim();
            return Long.parseLong(num);
        } catch (Exception e) {
            return null;
        }
    }
}
