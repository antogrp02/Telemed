<%-- 
    Document   : doctor_appointments
    Created on : 25 nov 2025
    Author     : Antonio + ChatGPT
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%@ page import="java.util.List, java.util.Map" %>
<%@ page import="model.Appuntamento, model.Paziente" %>

<%
    @SuppressWarnings("unchecked")
    List<Appuntamento> appuntamenti = (List<Appuntamento>) request.getAttribute("appuntamenti");
    @SuppressWarnings("unchecked")
    Map<Long, Paziente> pazById = (Map<Long, Paziente>) request.getAttribute("pazById");

    Integer currentPage = (Integer) request.getAttribute("page");
    Integer totalPagesCount = (Integer) request.getAttribute("totalPages");
    Integer totalAppointments = (Integer) request.getAttribute("totalCount");

    if (currentPage == null) currentPage = 1;
    if (totalPagesCount == null) totalPagesCount = 1;
    if (totalAppointments == null) totalAppointments = 0;

    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html>
<head>
    <title>Heart Monitor - Appuntamenti medico</title>
    <link rel="stylesheet" href="<%= ctx %>/css/style.css">
    
    <!-- (STYLES NON MODIFICATI, LI MANTENGO UGUALI) -->
    <style>
     

/* ========== APPOINTMENTS PAGE - SPECIFIC STYLES ========== */

.appointments-page-header {
    display: flex;
    justify-content: space-between;
    align-items: flex-start;
    margin-bottom: 2.5rem;
    padding-bottom: 2rem;
    border-bottom: 2px solid #e2e8f0;
}

.appointments-page-title {
    font-size: 2rem;
    font-weight: 700;
    color: #1a202c;
    display: flex;
    align-items: center;
    gap: 0.75rem;
    margin-bottom: 0.5rem;
}

.icon {
    font-size: 2.5rem;
}

.appointments-page-subtitle {
    color: #718096;
    font-size: 0.95rem;
    line-height: 1.6;
}

.appointments-page-meta {
    background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
    padding: 1rem 1.5rem;
    border-radius: 12px;
    border-left: 4px solid #667eea;
}

.appointments-page-meta strong {
    color: #667eea;
    font-size: 1.25rem;
}

/* APPOINTMENTS LIST */
.appointments-list {
    display: flex;
    flex-direction: column;
    gap: 0.75rem;
}

.appointments-empty {
    text-align: center;
    padding: 4rem 2rem;
    color: #a0aec0;
    font-size: 1.1rem;
    background: #f7fafc;
    border-radius: 16px;
    border: 2px dashed #cbd5e0;
}

.appointment-row-header {
    display: grid;
    grid-template-columns: 2fr 1.5fr 1.5fr 2fr;
    gap: 1.5rem;
    padding: 1rem 1.5rem;
    background: #f7fafc;
    border-radius: 12px;
    font-weight: 600;
    color: #4a5568;
    font-size: 0.85rem;
    text-transform: uppercase;
    letter-spacing: 0.5px;
}

.appointment-row {
    display: grid;
    grid-template-columns: 2fr 1.5fr 1.5fr 2fr;
    gap: 1.5rem;
    padding: 1.5rem;
    background: white;
    border: 2px solid #e2e8f0;
    border-radius: 16px;
    align-items: center;
    transition: all 0.3s ease;
}

.appointment-row:hover {
    border-color: #667eea;
    box-shadow: 0 8px 25px rgba(102, 126, 234, 0.15);
    transform: translateY(-2px);
}

.appointment-patient-name {
    font-weight: 600;
    color: #2d3748;
    margin-bottom: 0.25rem;
    font-size: 1.05rem;
}

.appointment-patient-cf {
    font-size: 0.85rem;
    color: #a0aec0;
    font-family: 'Courier New', monospace;
}

.appointment-datetime {
    display: flex;
    align-items: center;
}

.badge {
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    padding: 0.6rem 1rem;
    background: linear-gradient(135deg, #667eea15 0%, #764ba215 100%);
    color: #667eea;
    border-radius: 10px;
    font-size: 0.9rem;
    font-weight: 600;
    border: 1px solid #667eea30;
}

.appointment-type {
    display: flex;
    align-items: center;
    gap: 0.5rem;
    color: #4a5568;
    font-weight: 500;
}

.dot {
    width: 8px;
    height: 8px;
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    border-radius: 50%;
    animation: dotPulse 2s infinite;
}

@keyframes dotPulse {
    0%, 100% { opacity: 1; transform: scale(1); }
    50% { opacity: 0.6; transform: scale(1.2); }
}

/* APPOINTMENT ACTIONS */
.appointment-actions {
    display: flex;
    gap: 0.75rem;
    flex-wrap: wrap;
}

.reschedule-form {
    display: flex;
    gap: 0.5rem;
    align-items: center;
    flex-wrap: wrap;
}

.reschedule-form input[type="date"],
.reschedule-form input[type="time"] {
    padding: 0.5rem 0.75rem;
    border: 2px solid #e2e8f0;
    border-radius: 8px;
    font-size: 0.85rem;
    transition: all 0.3s ease;
    font-family: inherit;
}

.reschedule-form input:focus {
    outline: none;
    border-color: #667eea;
    box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.btn-pill {
    padding: 0.6rem 1.25rem;
    border: none;
    border-radius: 10px;
    font-weight: 600;
    font-size: 0.85rem;
    cursor: pointer;
    transition: all 0.3s ease;
    display: inline-flex;
    align-items: center;
    gap: 0.5rem;
    white-space: nowrap;
}

.btn-pill-primary {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.btn-pill-primary:hover {
    transform: translateY(-2px);
    box-shadow: 0 6px 20px rgba(102, 126, 234, 0.4);
}

.btn-pill-ghost-danger {
    background: #fff5f5;
    color: #e53e3e;
    border: 2px solid #feb2b2;
}

.btn-pill-ghost-danger:hover {
    background: #e53e3e;
    color: white;
    border-color: #e53e3e;
    transform: translateY(-2px);
}

/* PAGINATION */
.pagination-bar {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-top: 2.5rem;
    padding-top: 2rem;
    border-top: 2px solid #e2e8f0;
}

.pagination-summary {
    color: #718096;
    font-size: 0.95rem;
}

.pagination-summary strong {
    color: #667eea;
    font-weight: 700;
}

.pagination-pages {
    display: flex;
    gap: 0.5rem;
}

.pagination-link {
    display: flex;
    align-items: center;
    justify-content: center;
    width: 40px;
    height: 40px;
    border-radius: 10px;
    text-decoration: none;
    color: #4a5568;
    font-weight: 600;
    transition: all 0.3s ease;
    border: 2px solid #e2e8f0;
}

.pagination-link:hover {
    border-color: #667eea;
    color: #667eea;
    transform: translateY(-2px);
}

.pagination-link.active {
    background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
    color: white;
    border-color: transparent;
    box-shadow: 0 4px 15px rgba(102, 126, 234, 0.3);
}

/* RESPONSIVE - APPOINTMENTS */
@media (max-width: 1024px) {
    .appointment-row-header,
    .appointment-row {
        grid-template-columns: 1fr;
        gap: 1rem;
    }
    
    .appointments-page-header {
        flex-direction: column;
        gap: 1.5rem;
    }
}

@media (max-width: 640px) {
    .reschedule-form {
        width: 100%;
    }
    
    .reschedule-form input {
        flex: 1;
        min-width: 100px;
    }
    
    .pagination-bar {
        flex-direction: column;
        gap: 1.5rem;
    }
}


    </style>
</head>

<body>

    <!-- TOP BAR -->
    <div class="topbar">
        <div class="logo">Heart Monitor</div>
        <div class="subtitle">Agenda appuntamenti</div>
        <div class="spacer"></div>
        <a href="<%= ctx %>/logout" class="toplink">Logout</a>
    </div>

    <div class="layout">

        <!-- SIDEBAR -->
        <div class="sidebar">
            <a href="<%= ctx %>/doctor/dashboard">Pazienti</a>
            <a href="<%= ctx %>/doctor/appointments" class="active">Appuntamenti</a>
            <a href="<%= ctx %>/doctor/alerts">Alert</a>
        </div>

        <!-- MAIN -->
        <div class="main">
            <div class="appointments-page">

                <div class="appointments-page-header">
                    <div>
                        <h1 class="appointments-page-title">
                            <span class="icon">🗓️</span>
                            Agenda appuntamenti
                        </h1>
                        <p class="appointments-page-subtitle">
                            Tutti i prossimi appuntamenti con i tuoi pazienti, con motivo, data e ora.
                        </p>
                    </div>
                    <div class="appointments-page-meta">
                        <span class="appointments-page-subtitle">
                            Appuntamenti futuri totali: <strong><%= totalAppointments %></strong>
                        </span>
                    </div>
                </div>

                <div class="appointments-list">

                    <% if (appuntamenti == null || appuntamenti.isEmpty()) { %>

                        <div class="appointments-empty">
                            Nessun appuntamento futuro programmato.
                        </div>

                    <% } else { %>

                        <div class="appointment-row-header">
                            <div>Paziente</div>
                            <div>Data &amp; ora</div>
                            <div>Motivo</div>
                            <div>Azione</div>
                        </div>

                        <% for (Appuntamento a : appuntamenti) {

                            Paziente p = pazById != null ? pazById.get(a.getIdPaziente()) : null;

                            java.time.LocalDate d = a.getDataOra().toLocalDateTime().toLocalDate();
                            java.time.LocalTime t = a.getDataOra().toLocalDateTime().toLocalTime();

                            String dateStr = String.format("%02d/%02d/%04d",
                                    d.getDayOfMonth(), d.getMonthValue(), d.getYear());
                            String timeStr = String.format("%02d:%02d", t.getHour(), t.getMinute());

                            String nome = p != null ? p.getNome() : "Paziente sconosciuto";
                            String cognome = p != null ? p.getCognome() : "";
                            String cf = p != null ? p.getCf() : "-";
                        %>

                        <div class="appointment-row">
                            <div class="appointment-patient">
                                <div class="appointment-patient-name">
                                    👤 <%= nome %> <%= cognome %>
                                </div>
                                <div class="appointment-patient-cf">
                                    CF: <%= cf %>
                                </div>
                            </div>

                            <div class="appointment-datetime">
                                <span class="badge">
                                    📅 <%= dateStr %> · <%= timeStr %>
                                </span>
                            </div>

                            <div class="appointment-type">
                                <span class="dot"></span>
                                <span><%= a.getTipo() %></span>
                            </div>

                            <div class="appointment-actions">

                                <!-- RINVIA -->
                                <form method="post"
                                      action="<%= ctx %>/doctor/appointments"
                                      class="reschedule-form"
                                      onsubmit="return onRescheduleSubmit(this, '<%= nome %>', '<%= cognome %>', '<%= dateStr %>', '<%= timeStr %>');">

                                    <input type="hidden" name="action" value="reschedule" />
                                    <input type="hidden" name="id" value="<%= a.getId() %>" />
                                    <input type="hidden" name="page" value="<%= currentPage %>" />

                                    <input type="date" name="data" required />
                                    <input type="time" name="ora" required />

                                    <button type="submit" class="btn-pill btn-pill-primary">
                                        🔁 Rinvia
                                    </button>
                                </form>

                                <!-- ANNULLA -->
                                <form method="post"
                                      action="<%= ctx %>/doctor/appointments"
                                      onsubmit="return onCancelSubmit('<%= nome %>', '<%= cognome %>', '<%= dateStr %>', '<%= timeStr %>');">

                                    <input type="hidden" name="action" value="cancel" />
                                    <input type="hidden" name="id" value="<%= a.getId() %>" />
                                    <input type="hidden" name="page" value="<%= currentPage %>" />

                                    <button type="submit" class="btn-pill btn-pill-ghost-danger">
                                        🗑️ Annulla
                                    </button>
                                </form>

                            </div>
                        </div>

                        <% } %>

                    <% } %>

                </div>

                <!-- PAGINAZIONE -->
                <div class="pagination-bar">
                    <div class="pagination-summary">
                        Pagina <strong><%= currentPage %></strong> di <strong><%= totalPagesCount %></strong>
                    </div>

                    <div class="pagination-pages">
                        <% for (int p = 1; p <= totalPagesCount; p++) {
                            boolean active = (p == currentPage);
                            String cssClass = "pagination-link" + (active ? " active" : "");
                        %>

                        <a class="<%= cssClass %>"
                           href="<%= ctx %>/doctor/appointments?page=<%= p %>">
                            <%= p %>
                        </a>

                        <% } %>
                    </div>
                </div>

            </div> <!-- appointments-page -->
        </div> <!-- main -->
    </div> <!-- layout -->

    <!-- JAVASCRIPT -->
    <script>
        console.log("[DoctorAppointments] Caricata pagina", <%= currentPage %>, "/", <%= totalPagesCount %>,
            "| Appuntamenti totali =", <%= totalAppointments %>);

        function onCancelSubmit(nome, cognome, data, ora) {
            const msg = `Confermi l'annullamento dell'appuntamento di ${nome} ${cognome} del ${data} alle ${ora}?`;
            const ok = window.confirm(msg);
            if (ok) console.log("[DoctorAppointments] Annullamento confermato");
            return ok;
        }

        function onRescheduleSubmit(form, nome, cognome, oldData, oldOra) {
            const newData = form.data.value;
            const newOra = form.ora.value;

            if (!newData || !newOra) {
                alert("Seleziona la nuova data e ora per il rinvio.");
                return false;
            }

            console.log(`[DoctorAppointments] Rinviato ${nome} ${cognome} da ${oldData} ${oldOra} a ${newData} ${newOra}`);
            return true;
        }
    </script>

</body>
</html>
