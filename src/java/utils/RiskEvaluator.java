/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package utils;

public class RiskEvaluator {

    public static final float SOGLIA_ALERT = 0.70f;

    public static String getLevel(float risk) {
        if (risk < 0.30f) return "basso";
        if (risk < 0.69f) return "moderato";
        return "alto";
    }

    public static boolean isAlert(float risk) {
        return risk >= SOGLIA_ALERT;
    }

    public static String getCssClass(float risk) {
        if (risk < 0.30f) return "risk-green";
        if (risk < 0.69f) return "risk-yellow";
        return "risk-red";
    }
}
