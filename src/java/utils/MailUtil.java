package utils;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.MessagingException;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import java.util.Properties;

public class MailUtil {

    // ⚠️ CONFIGURA QUI IL TUO SMTP
    private static final String SMTP_HOST = "smtp.tuoprovider.it";
    private static final String SMTP_PORT = "587";
    private static final String SMTP_USER = "tuoutente@dominio.it";
    private static final String SMTP_PASS = "TUA_PASSWORD_SMTP";

    private static Session getSession() {
        Properties props = new Properties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        return Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });
    }

    public static void sendPasswordResetMail(String to, String resetLink) throws MessagingException {
        Session session = getSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("noreply@heartmonitor.local", false));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Reset password - Heart Monitor");

        String text = "Ciao,\n\n" +
                      "abbiamo ricevuto una richiesta di reset della password per il tuo account.\n" +
                      "Per procedere clicca sul seguente link (oppure copialo nel browser):\n\n" +
                      resetLink + "\n\n" +
                      "Se non hai richiesto tu il reset, puoi ignorare questa email.\n\n" +
                      "Heart Monitor";

        message.setText(text);
        Transport.send(message);
    }
}
