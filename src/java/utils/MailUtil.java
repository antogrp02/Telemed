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

    // ==========================
    //   CONFIGURAZIONE SENDGRID
    // ==========================
    private static final String SMTP_HOST = "smtp.sendgrid.net";
    private static final String SMTP_PORT = "587";

    // SendGrid RICHIEDE SEMPRE:
    //  username = "apikey"
    private static final String SMTP_USER = "apikey";

    // ⚠ Inserisci QUI la tua API key (funziona subito)
    // In futuro useremo System.getenv
    private static final String SMTP_PASS = System.getenv("SENDGRID_API_KEY");

    private static final String FROM_ADDRESS = "noreply@heartmonitor.it";

    // ==========================
    //    COSTRUZIONE SESSIONE
    // ==========================
    private static Session getSession() {

        Properties props = new Properties();

        // Autenticazione
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.user", SMTP_USER);
        props.put("mail.smtp.password", SMTP_PASS);
        props.put("mail.smtp.auth.mechanisms", "PLAIN LOGIN");

        // TLS obbligatorio
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2");

        // Host e porta
        props.put("mail.smtp.host", SMTP_HOST);
        props.put("mail.smtp.port", SMTP_PORT);

        // Fix problemi con truststore (PKIX)
        props.put("mail.smtp.ssl.checkserveridentity", "false");
        props.put("mail.smtp.ssl.trust", "*");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                // Username DEVE essere "apikey"
                return new PasswordAuthentication(SMTP_USER, SMTP_PASS);
            }
        });

        // Debug SMTP
        session.setDebug(true);

        return session;
    }

    // ==========================
    //      INVIO EMAIL
    // ==========================
    public static void sendPasswordResetMail(String to, String resetLink)
            throws MessagingException {

        if (SMTP_PASS == null || SMTP_PASS.isEmpty()) {
            System.out.println("❌ ERRORE: SENDGRID_API_KEY NON impostata!");
            throw new MessagingException("API key SendGrid mancante");
        }

        Session session = getSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress(FROM_ADDRESS));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject("Reset password - Heart Monitor");

        String text
                = "Ciao,\n\n"
                + "Hai richiesto la reimpostazione della password del tuo account Heart Monitor.\n\n"
                + "➡ Per procedere clicca qui:\n" + resetLink + "\n\n"
                + "Se non hai effettuato tu questa richiesta, ignora questa email.\n\n"
                + "Heart Monitor";

        message.setText(text);

        Transport.send(message);
    }

    // ==========================
//     INVIO EMAIL ALERT
// ==========================
    public static void sendAlertEmail(String to, String subject, String body)
            throws MessagingException {

        if (SMTP_PASS == null || SMTP_PASS.isEmpty()) {
            System.out.println("❌ ERRORE: SENDGRID_API_KEY NON impostata!");
            throw new MessagingException("API key SendGrid mancante");
        }

        Session session = getSession();

        Message message = new MimeMessage(session);
        message.setFrom(new InternetAddress("alert@heartmonitor.it"));  // mittente ALERT
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);

        message.setText(body);

        Transport.send(message);
    }

}
