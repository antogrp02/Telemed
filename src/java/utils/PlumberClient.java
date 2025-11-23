package utils;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import model.Parametri;
import model.Questionario;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;

public class PlumberClient {

    private static final String PLUMBER_URL = "http://localhost:8000/predict";

    /**
     * Esegue la predizione chiamando Plumber (R)
     */
    public static float getRiskScore(Parametri p, Questionario q) throws IOException {

        if (q == null) {
            throw new IllegalArgumentException("Questionario nullo: impossibile chiamare il modello ML");
        }

        // -----------------------------
        // COSTRUZIONE JSON REQUEST BODY
        // -----------------------------
        JsonObject root = new JsonObject();

        // 1) Correnti
        root.addProperty("heart_rate_curr", p.getHrCurr());
        root.addProperty("hrv_rmssd_curr", p.getHrvRmssdCurr());
        root.addProperty("spo2_curr", p.getSpo2Curr());
        root.addProperty("resp_rate_curr", p.getRespRateCurr());
        root.addProperty("thoracic_bioimpedance_curr", p.getBioimpCurr());
        root.addProperty("weight_curr", p.getWeightCurr());
        root.addProperty("steps_curr", p.getStepsCurr());
        root.addProperty("resting_hr_curr", p.getRhrCurr());

        // 2) Questionario (stringhe, come richiesto dal modello ML)
        root.addProperty("Q1_dyspnea", String.valueOf(q.getDispnea()));
        root.addProperty("Q2_edema", String.valueOf(q.getEdema()));
        root.addProperty("Q3_fatigue", String.valueOf(q.getFatica()));
        root.addProperty("Q4_adl_lim", String.valueOf(q.getAdl()));
        root.addProperty("Q5_orthopnea", String.valueOf(q.getOrtopnea()));
        root.addProperty("Q6_dizziness", String.valueOf(q.getVertigini()));

        // 3) 7 giorni
        root.addProperty("heart_rate_tr7", p.getHr7d());
        root.addProperty("hrv_rmssd_tr7", p.getHrvRmssd7d());
        root.addProperty("spo2_tr7", p.getSpo27d());
        root.addProperty("resp_rate_tr7", p.getRespRate7d());
        root.addProperty("thoracic_bioimpedance_tr7", p.getBioimp7d());
        root.addProperty("weight_tr7", p.getWeight7d());
        root.addProperty("steps_tr7", p.getSteps7d());
        root.addProperty("resting_hr_tr7", p.getRhr7d());

        // 4) Baseline (zero baseline)
        root.addProperty("heart_rate_zb", p.getHrBs());
        root.addProperty("hrv_rmssd_zb", p.getHrvRmssdBs());
        root.addProperty("spo2_zb", p.getSpo2Bs());
        root.addProperty("resp_rate_zb", p.getRespRateBs());
        root.addProperty("thoracic_bioimpedance_zb", p.getBioimpBs());
        root.addProperty("weight_zb", p.getWeightBs());
        root.addProperty("steps_zb", p.getStepsBs());
        root.addProperty("resting_hr_zb", p.getRhrBs());

        String json = root.toString();

        // -----------------------------
        // HTTP REQUEST
        // -----------------------------
        URL url = new URL(PLUMBER_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(5000);
        conn.setReadTimeout(5000);
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setDoOutput(true);

        // SCRITTURA DEL BODY
        try (OutputStream os = conn.getOutputStream()) {
            os.write(json.getBytes());
        }

        // VERIFICA CODICE HTTP
        int code = conn.getResponseCode();
        if (code != 200) {
            throw new IOException("Plumber HTTP error: " + code);
        }

        // LETTURA RISPOSTA
        StringBuilder resp = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = br.readLine()) != null) {
                resp.append(line);
            }
        }

        // PARSING JSON
        JsonObject obj = JsonParser.parseString(resp.toString()).getAsJsonObject();

        if (!obj.has("risk")) {
            throw new IOException("Risposta ML priva di campo 'risk'");
        }

        return obj.get("risk").getAsFloat();
    }
}
