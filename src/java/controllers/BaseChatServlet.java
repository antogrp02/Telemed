package controllers;

import dao.ChatMessageDAO;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;
import java.util.List;
import model.ChatMessage;
import model.Medico;
import model.Paziente;

public abstract class BaseChatServlet extends HttpServlet {

    protected static class ChatContext {
        private final long otherUserId;
        private final Paziente paziente;
        private final Medico medico;
        private final String view;
        private final boolean markMessagesAsRead;

        public ChatContext(long otherUserId, Paziente paziente, Medico medico, String view, boolean markMessagesAsRead) {
            this.otherUserId = otherUserId;
            this.paziente = paziente;
            this.medico = medico;
            this.view = view;
            this.markMessagesAsRead = markMessagesAsRead;
        }

        public long getOtherUserId() {
            return otherUserId;
        }

        public Paziente getPaziente() {
            return paziente;
        }

        public Medico getMedico() {
            return medico;
        }

        public String getView() {
            return view;
        }

        public boolean shouldMarkMessagesAsRead() {
            return markMessagesAsRead;
        }
    }

    protected abstract int expectedRole();

    protected abstract ChatContext buildChatContext(HttpServletRequest req, HttpServletResponse resp,
                                                    HttpSession session, long myUserId) throws Exception;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession session = req.getSession(false);
        if (session == null || session.getAttribute("role") == null || (int) session.getAttribute("role") != expectedRole()) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        long myUserId = (long) session.getAttribute("id_utente");

        try {
            ChatContext context = buildChatContext(req, resp, session, myUserId);
            if (context == null) {
                return;
            }

            if (context.shouldMarkMessagesAsRead()) {
                ChatMessageDAO.segnaComeLetti(context.getOtherUserId(), myUserId);
            }

            List<ChatMessage> history = ChatMessageDAO.getHistory(myUserId, context.getOtherUserId());

            req.setAttribute("history", history);
            req.setAttribute("myUserId", myUserId);
            req.setAttribute("otherUserId", context.getOtherUserId());
            req.setAttribute("paziente", context.getPaziente());
            req.setAttribute("medico", context.getMedico());

            req.getRequestDispatcher(context.getView()).forward(req, resp);

        } catch (Exception e) {
            throw new ServletException(e);
        }
    }
}
