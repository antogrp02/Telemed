/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/JSP_Servlet/Servlet.java to edit this template
 */
package controllers;

import com.google.gson.Gson;
import dao.PazienteDAO;
import model.Paziente;
import dto.SearchPazienteDTO;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.*;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@WebServlet("/doctor/search")
public class DoctorSearchServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp)
            throws ServletException, IOException {

        HttpSession s = req.getSession(false);
        if (s == null || (int) s.getAttribute("role") != 1) {
            resp.setStatus(401);
            return;
        }

        long idMedico = (long) s.getAttribute("id_medico");
        String q = req.getParameter("q");

        List<Paziente> risultati = PazienteDAO.searchByMedico(idMedico, q);

        List<SearchPazienteDTO> dto = new ArrayList<>();
        for (Paziente p : risultati) {
            dto.add(new SearchPazienteDTO(
                    p.getIdPaz(),
                    p.getNome(),
                    p.getCognome(),
                    p.getCf()
            ));
        }

        resp.setContentType("application/json");
        resp.getWriter().write(new Gson().toJson(dto));
    }
}
