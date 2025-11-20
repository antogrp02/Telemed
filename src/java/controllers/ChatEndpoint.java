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
    private static final Gson gson = new Gson();

    @OnOpen
    public void onOpen(Session session, @PathParam("idUtente") long idUtente) {
        SESSIONS.put(idUtente, session);
        session.getUserProperties().put("idUtente", idUtente);
    }

    @OnClose
    public void onClose(Session session) {
        Object id = session.getUserProperties().get("idUtente");
        if (id instanceof Long) {
            SESSIONS.remove((Long) id);
        }
    }

    @OnError
    public void onError(Session session, Throwable thr) {
        thr.printStackTrace();
    }

    // JSON IN: { "destId": 123, "text": "ciao" }
    // JSON OUT: { "from": 1, "to": 2, "text": "ciao", "sentAt": "2025-11-20T10:10:10", "mine": true/false }
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Object uidObj = session.getUserProperties().get("idUtente");
            if (!(uidObj instanceof Long)) {
                return;
            }
            long mittenteId = (Long) uidObj;

            IncomingMessage in = gson.fromJson(message, IncomingMessage.class);
            if (in == null || in.destId == 0 || in.text == null || in.text.trim().isEmpty()) {
                return;
            }

            long destId = in.destId;
            String text = in.text.trim();

            // 1) salva su DB
            ChatMessage m = new ChatMessage();
            m.setIdMittente(mittenteId);
            m.setIdDestinatario(destId);
            m.setTesto(text);
            m.setInviatoIl(Timestamp.from(Instant.now()));
            ChatMessageDAO.insert(m);

            String sentAt = m.getInviatoIl().toInstant().toString();

            // 2) prepara JSON per mittente e destinatario
            OutgoingMessage outForSender = new OutgoingMessage();
            outForSender.from = mittenteId;
            outForSender.to = destId;
            outForSender.text = text;
            outForSender.sentAt = sentAt;
            outForSender.mine = true;

            OutgoingMessage outForDest = new OutgoingMessage();
            outForDest.from = mittenteId;
            outForDest.to = destId;
            outForDest.text = text;
            outForDest.sentAt = sentAt;
            outForDest.mine = false;

            String jsonSender = gson.toJson(outForSender);
            String jsonDest = gson.toJson(outForDest);

            // 3) invia al mittente
            session.getAsyncRemote().sendText(jsonSender);

            // 4) invia al destinatario se è collegato
            Session destSession = SESSIONS.get(destId);
            if (destSession != null && destSession.isOpen()) {
                destSession.getAsyncRemote().sendText(jsonDest);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // DTO interni
    private static class IncomingMessage {
        long destId;
        String text;
    }

    private static class OutgoingMessage {
        long from;
        long to;
        String text;
        String sentAt;
        boolean mine;
    }
}

