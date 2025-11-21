/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package controllers;

import com.google.gson.Gson;
import dao.ChatMessageDAO;
import model.ChatMessage;

import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@ServerEndpoint("/ws/chat/{idUtente}")
public class ChatEndpoint {

    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();
    private static final Map<Long, Object> LOCKS = new ConcurrentHashMap<>();
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("idUtente") long idUtente) {
        SESSIONS.put(idUtente, session);
        LOCKS.put(idUtente, new Object());
        session.getUserProperties().put("idUtente", idUtente);
    }

    @OnClose
    public void onClose(Session session) {
        Object id = session.getUserProperties().get("idUtente");
        if (id instanceof Long) {
            SESSIONS.remove((Long) id);
            LOCKS.remove((Long) id);
        }
    }

    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            long mittenteId = (Long) session.getUserProperties().get("idUtente");
            IncomingMessage in = gson.fromJson(message, IncomingMessage.class);
            if (in == null || in.destId == 0 || in.text == null) {
                return;
            }

            long destId = in.destId;
            String text = in.text.trim();

            ChatMessage m = new ChatMessage();
            m.setIdMittente(mittenteId);
            m.setIdDestinatario(destId);
            m.setTesto(text);
            m.setInviatoIl(Timestamp.from(Instant.now()));
            ChatMessageDAO.insert(m);

            String sentAt = m.getInviatoIl().toInstant().toString();

            OutgoingMessage outSender = new OutgoingMessage(mittenteId, destId, text, sentAt, true);
            OutgoingMessage outDest = new OutgoingMessage(mittenteId, destId, text, sentAt, false);

            safeSend(mittenteId, gson.toJson(outSender));
            safeSend(destId, gson.toJson(outDest));

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void safeSend(Long id, String msg) {
        Session s = SESSIONS.get(id);
        Object lock = LOCKS.get(id);
        if (s == null || !s.isOpen()) {
            return;
        }

        synchronized (lock) {
            s.getAsyncRemote().sendText(msg);
        }
    }

    private static class IncomingMessage {

        long destId;
        String text;
    }

    private static class OutgoingMessage {

        long from, to;
        String text, sentAt;
        boolean mine;

        OutgoingMessage(long f, long t, String txt, String at, boolean m) {
            from = f;
            to = t;
            text = txt;
            sentAt = at;
            mine = m;
        }
    }
}
