/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

import model.Parametri;
// import java.io.*;
// import java.net.*;

public class PlumberClient {

    // Versione dummy: calcola un rischio sintetico
    public static float getRiskScore(Parametri p) {
        double score = 0.2;

        // piccolo esempio “realistico”:
        score += Math.max(0, (p.getWeight7d() / 3.0));   // aumento peso
        score += Math.max(0, (p.getHr7d() / 20.0));      // aumento HR
        score += Math.max(0, (-p.getSpo27d() / 5.0));    // calo SpO2
        score += Math.max(0, (-p.getSteps7d() / 8000.0));// calo attività

        if (score < 0) score = 0;
        if (score > 1) score = 1;

        return (float) score;
    }

    /*
    // Versione futura con HTTP:
    public static float getRiskScoreJSON(String jsonPayload) throws IOException {
        URL url = new URL("http://localhost:8000/predict");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type","application/json; charset=UTF-8");
        conn.setDoOutput(true);

        try (OutputStream os = conn.getOutputStream()) {
            os.write(jsonPayload.getBytes("UTF-8"));
        }

        int code = conn.getResponseCode();
        if (code != 200) throw new IOException("HTTP " + code);

        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            StringBuilder resp = new StringBuilder();
            String line;
            while ((line = br.readLine()) != null) resp.append(line);
            String body = resp.toString();
            String num = body.replaceAll("[^0-9\\.]", "");
            return Float.parseFloat(num);
        }
    }
    */
}

