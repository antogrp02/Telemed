/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;
import java.io.IOException;

@WebServlet("/admin/model")
public class AdminModelServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || s.getAttribute("role") == null || (int) s.getAttribute("role") != 2) {
            resp.sendRedirect(req.getContextPath() + "/login");
            return;
        }

        // valori fittizi; in futuro puoi leggerli da DB
        req.setAttribute("modelVersion", "v1.0.0");
        req.setAttribute("modelAuc", "0.86");
        req.setAttribute("lastRetrain", "2025-10-10");

        req.getRequestDispatcher("/admin_model.jsp").forward(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp)
        throws ServletException, IOException {

        // TODO: qui potresti chiamare un endpoint Plumber /retrain
        System.out.println("Richiesto retraining modello (dummy).");

        resp.sendRedirect("model?ok=1");
    }
}
