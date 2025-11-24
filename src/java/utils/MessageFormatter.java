package utils;

public class MessageFormatter {

    private MessageFormatter() {
    }

    public static String formatMessage(String text) {
        if (text == null) {
            return "";
        }

        text = text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;");

        String urlRegex = "(https?://[\\w\\-._~:/?#\\[\\]@!$&'()*+,;=%]+)";
        return text.replaceAll(urlRegex,
                "<a href=\\\"$1\\\" target=\\\"_blank\\\" rel=\\\"noopener noreferrer\\\">$1</a>");
    }
}
