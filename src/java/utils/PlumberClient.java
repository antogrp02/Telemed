/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import model.Parametri;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlumberClient {

    private static final String PLUMBER_URL = "http://localhost:8000/predict";

    public static float getRiskScore(Parametri p) throws IOException {

        // TODO: se il tuo endpoint Plumber si chiama in altro modo, cambia PLUMBER_URL
        // TODO: se si aspetta un JSON diverso, qui va adattato il body

        // Costruisco un JSON minimo, puoi estenderlo con tutte le feature
        String json = "{"
                + "\"id_paz\":" + p.getIdPaz() + ","
                + "\"hr_curr\":" + p.getHrCurr() + ","
                + "\"rhr_curr\":" + p.getRhrCurr() + ","
                + "\"hrv_rmssd_curr\":" + p.getHrvRmssdCurr() + ","
                + "\"spo2_curr\":" + p.getSpo2Curr() + ","
                + "\"resp_rate_curr\":" + p.getRespRateCurr() + ","
                + "\"bioimp_curr\":" + p.getBioimpCurr() + ","
                + "\"weight_curr\":" + p.getWeightCurr() + ","
                + "\"steps_curr\":" + p.getStepsCurr()
                // ... qui puoi aggiungere 7d e baseline se il modello li usa
                + "}";

        URL url = new URL(PLUMBER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(4000);
        conn.setReadTimeout(4000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("HTTP Plumber " + code);
        }

        try (BufferedReader br = new BufferedReader(
                new InputStreamReader(conn.getInputStream()))) {
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) resp.append(line);

            // supponiamo che Plumber risponda { "risk": 0.78 }
            String body = resp.toString();
            String num = body.replaceAll("[^0-9\\.]", "");
            return Float.parseFloat(num);
        }
    }
}
