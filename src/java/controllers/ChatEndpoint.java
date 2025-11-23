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

    // Sessioni utente → websocket
    private static final Map<Long, Session> SESSIONS = new ConcurrentHashMap<>();

    // Locks per thread safety
    private static final Map<Long, Object> LOCKS = new ConcurrentHashMap<>();

    // Chat attualmente aperta da ogni utente:
    // userId → otherUserId
    private static final Map<Long, Long> CHAT_APERTA = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();


    // ---------------------------------------------------------
    // ON OPEN
    // ---------------------------------------------------------
    @OnOpen
    public void onOpen(Session session, @PathParam("idUtente") long idUtente) {
        SESSIONS.put(idUtente, session);
        LOCKS.put(idUtente, new Object());
        session.getUserProperties().put("idUtente", idUtente);
    }

    // ---------------------------------------------------------
    // ON CLOSE
    // ---------------------------------------------------------
    @OnClose
    public void onClose(Session session) {
        Object id = session.getUserProperties().get("idUtente");
        if (id instanceof Long) {
            long uid = (Long) id;
            SESSIONS.remove(uid);
            LOCKS.remove(uid);
            CHAT_APERTA.remove(uid);
        }
    }

    // ---------------------------------------------------------
    // ON MESSAGE
    // ---------------------------------------------------------
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            long mittenteId = (Long) session.getUserProperties().get("idUtente");

            // Controllo se è un messaggio di sistema (ENTER_CHAT)
            SystemMessage sysMsg = gson.fromJson(message, SystemMessage.class);
            if (sysMsg != null && "ENTER_CHAT".equals(sysMsg.type)) {

                if (sysMsg.otherUserId > 0) {
                    CHAT_APERTA.put(mittenteId, sysMsg.otherUserId);
                }
                return;
            }

            // Messaggio di chat normale
            IncomingMessage in = gson.fromJson(message, IncomingMessage.class);
            if (in == null || in.destId == 0 || in.text == null) {
                return;
            }

            long destId = in.destId;
            String text = in.text.trim();

            // Salvataggio DB
            ChatMessage m = new ChatMessage();
            m.setIdMittente(mittenteId);
            m.setIdDestinatario(destId);
            m.setTesto(text);
            m.setInviatoIl(Timestamp.from(Instant.now()));
            ChatMessageDAO.insert(m);

            String sentAt = m.getInviatoIl().toInstant().toString();

            // Invio risposte WebSocket
            OutgoingMessage outSender = new OutgoingMessage(mittenteId, destId, text, sentAt, true);
            OutgoingMessage outDest = new OutgoingMessage(mittenteId, destId, text, sentAt, false);

            safeSend(mittenteId, gson.toJson(outSender));
            safeSend(destId, gson.toJson(outDest));


            // ---------------------------------------------------------
            // 🔥 SE IL DESTINATARIO È GIÀ NELLA CHAT → segnalo come letto
            // ---------------------------------------------------------
            Long chatApertaCon = CHAT_APERTA.get(destId);

            if (chatApertaCon != null && chatApertaCon == mittenteId) {

                // Marca "letto" in DB
                ChatMessageDAO.segnaUltimoComeLetto(mittenteId, destId);

                // Notifico al destinatario l’aggiornamento
                ReadConfirm rc = new ReadConfirm("READ_CONFIRM", mittenteId);
                safeSend(destId, gson.toJson(rc));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ---------------------------------------------------------
    // SAFE SEND
    // ---------------------------------------------------------
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


    // ---------------------------------------------------------
    // CLASSI JSON
    // ---------------------------------------------------------
    private static class IncomingMessage {
        long destId;
        String text;
    }

    private static class SystemMessage {
        String type;         // "ENTER_CHAT"
        long otherUserId;    // userId con cui sto chattando
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

    private static class ReadConfirm {
        String type;     // "READ_CONFIRM"
        long fromUser;   // chi ha scritto

        ReadConfirm(String t, long f) {
            type = t;
            fromUser = f;
        }
    }
}
