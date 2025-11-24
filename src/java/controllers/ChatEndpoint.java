package controllers;

import com.google.gson.Gson;
import dao.ChatMessageDAO;
import dao.PazienteDAO;
import model.ChatMessage;
import model.Paziente;

import jakarta.websocket.OnClose;
import jakarta.websocket.OnMessage;
import jakarta.websocket.OnOpen;
import jakarta.websocket.Session;
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
    private static final Map<Long, Long> CHAT_APERTA = new ConcurrentHashMap<>();

    private static final Gson gson = new Gson();


    // ============================================================
    // ON OPEN
    // ============================================================
    @OnOpen
    public void onOpen(Session session, @PathParam("idUtente") long idUtente) {
        SESSIONS.put(idUtente, session);
        LOCKS.put(idUtente, new Object());
        session.getUserProperties().put("idUtente", idUtente);
    }


    // ============================================================
    // ON CLOSE
    // ============================================================
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


    // ============================================================
    // ON MESSAGE
    // ============================================================
    @OnMessage
    public void onMessage(String message, Session session) {
        try {
            Long mittenteObj = (Long) session.getUserProperties().get("idUtente");
            if (mittenteObj == null) return;

            long mittenteId = mittenteObj;

            // 1) Messaggio ENTER_CHAT
            SystemMessage sysMsg = gson.fromJson(message, SystemMessage.class);
            if (sysMsg != null && "ENTER_CHAT".equals(sysMsg.type)) {
                if (sysMsg.otherUserId > 0) CHAT_APERTA.put(mittenteId, sysMsg.otherUserId);
                return;
            }

            // 2) Messaggio normale
            IncomingMessage in = gson.fromJson(message, IncomingMessage.class);
            if (in == null || in.destId == 0 || in.text == null) return;

            long destId = in.destId;
            String text = in.text.trim();
            if (text.isEmpty()) return;

            // 3) Salva nel DB
            ChatMessage m = new ChatMessage();
            m.setIdMittente(mittenteId);
            m.setIdDestinatario(destId);
            m.setTesto(text);
            m.setInviatoIl(Timestamp.from(Instant.now()));
            ChatMessageDAO.insert(m);

            String sentAt = m.getInviatoIl().toInstant().toString();

            // 4) Ricavo info paziente SE il mittente è un paziente
            long pazienteId = -1L;
            String nome = null;
            String cognome = null;

            try {
                Paziente paz = PazienteDAO.getByIdUtente(mittenteId);
                if (paz != null) {
                    pazienteId = paz.getIdPaz();
                    nome = paz.getNome();
                    cognome = paz.getCognome();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            // 5) Risposta a mittente
            OutgoingMessage outSender = new OutgoingMessage(
                    mittenteId, destId, pazienteId,
                    nome, cognome,
                    text, sentAt, true
            );

            // 6) Risposta a destinatario
            OutgoingMessage outDest = new OutgoingMessage(
                    mittenteId, destId, pazienteId,
                    nome, cognome,
                    text, sentAt, false
            );

            safeSend(mittenteId, gson.toJson(outSender));
            safeSend(destId, gson.toJson(outDest));


            // 7) Se il destinatario ha la chat aperta → segna come letto
            Long apertaCon = CHAT_APERTA.get(destId);

            if (apertaCon != null && apertaCon == mittenteId) {
                try {
                    ChatMessageDAO.segnaUltimoComeLetto(mittenteId, destId);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                ReadConfirm rc = new ReadConfirm("READ_CONFIRM", mittenteId);
                safeSend(destId, gson.toJson(rc));
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


    // ============================================================
    // SAFE SEND
    // ============================================================
    private static void safeSend(Long id, String msg) {
        Session s = SESSIONS.get(id);
        Object lock = LOCKS.get(id);

        if (s == null || !s.isOpen()) return;

        synchronized (lock) {
            s.getAsyncRemote().sendText(msg);
        }
    }


    // ============================================================
    // CLASSI JSON
    // ============================================================
    private static class IncomingMessage {
        long destId;
        String text;
    }

    private static class SystemMessage {
        String type;
        long otherUserId;
    }

    private static class OutgoingMessage {
        long from, to, pazienteId;
        String pazienteNome, pazienteCognome;
        String text, sentAt;
        boolean mine;

        OutgoingMessage(long f, long t, long pid,
                        String nome, String cognome,
                        String txt, String at, boolean m) {

            from = f;
            to = t;
            pazienteId = pid;
            pazienteNome = nome;
            pazienteCognome = cognome;
            text = txt;
            sentAt = at;
            mine = m;
        }
    }

    private static class ReadConfirm {
        String type;
        long fromUser;

        ReadConfirm(String t, long f) {
            type = t;
            fromUser = f;
        }
    }
}
