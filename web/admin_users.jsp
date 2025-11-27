<%-- 
    Admin - Gestione utenti (pazienti, medici, account, assegnazioni)
--%>
<%@ page pageEncoding="UTF-8" contentType="text/html; charset=UTF-8" %>

<%@ page import="java.util.List" %>
<%@ page import="model.Paziente, model.Medico" %>

<%
    List<Paziente> pazienti = (List<Paziente>) request.getAttribute("pazienti");
    List<Medico> medici = (List<Medico>) request.getAttribute("medici");
    String message = (String) request.getAttribute("message");
    String error = (String) request.getAttribute("error");
    String ctx = request.getContextPath();
%>

<!DOCTYPE html>
<html>
    <head>
        <title>Heart Monitor - Admin Utenti</title>
        <link rel="stylesheet" href="<%= ctx%>/css/style.css">
        <meta name="viewport" content="width=device-width, initial-scale=1.0">

        <style>
            .admin-grid {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(340px, 1fr));
                gap: 24px;
                margin-top: 24px;
            }

            .admin-section-title {
                font-size: 22px;
                font-weight: 700;
                margin: 24px 0 8px;
            }

            .admin-subtitle {
                font-size: 14px;
                color: #64748b;
                margin-bottom: 16px;
            }

            .admin-badge {
                display: inline-flex;
                align-items: center;
                gap: 8px;
                padding: 6px 12px;
                border-radius: 999px;
                background: rgba(148, 163, 184, 0.12);
                font-size: 12px;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.08em;
                color: #475569;
            }

            .admin-pill {
                border-radius: 50px;
                padding: 4px 10px;
                background: rgba(148, 163, 184, 0.16);
                font-size: 11px;
                font-weight: 600;
                text-transform: uppercase;
                letter-spacing: 0.06em;
                color: #64748b;
            }

            .admin-form {
                margin-top: 8px;
            }

            .admin-form label {
                display: block;
                font-size: 13px;
                margin-top: 10px;
                margin-bottom: 4px;
                color: #334155;
                font-weight: 600;
            }

            .admin-form input,
            .admin-form select {
                width: 100%;
                border-radius: 12px;
                border: 2px solid #e2e8f0;
                padding: 10px 14px;
                font-size: 14px;
                transition: all 0.2s ease;
                background: #f8fafc;
            }

            .admin-form input:focus,
            .admin-form select:focus {
                outline: none;
                border-color: #667eea;
                background: #ffffff;
                box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.25);
            }

            .admin-row {
                display: grid;
                grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
                gap: 10px;
            }

            .btn-inline {
                margin-top: 16px;
                padding: 10px 14px;
                border-radius: 999px;
                border: none;
                cursor: pointer;
                background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
                color: white;
                font-weight: 600;
                font-size: 13px;
                display: inline-flex;
                align-items: center;
                gap: 8px;
            }

            .btn-inline.secondary {
                background: #e2e8f0;
                color: #0f172a;
            }

            .alert-banner {
                margin-top: 16px;
                padding: 12px 18px;
                border-radius: 14px;
                font-size: 14px;
                display: flex;
                align-items: center;
                gap: 10px;
            }

            .alert-banner.success {
                background: rgba(34, 197, 94, 0.08);
                border: 1px solid rgba(34, 197, 94, 0.4);
                color: #166534;
            }

            .alert-banner.error {
                background: rgba(248, 113, 113, 0.08);
                border: 1px solid rgba(248, 113, 113, 0.5);
                color: #7f1d1d;
            }

            .admin-table {
                width: 100%;
                border-collapse: collapse;
                margin-top: 12px;
            }

            .admin-table th,
            .admin-table td {
                padding: 8px 10px;
                font-size: 13px;
                text-align: left;
                border-bottom: 1px solid #e2e8f0;
            }

            .admin-table th {
                font-size: 12px;
                text-transform: uppercase;
                letter-spacing: 0.08em;
                color: #64748b;
                background: #f8fafc;
            }

            .tag {
                display: inline-flex;
                align-items: center;
                padding: 2px 8px;
                border-radius: 999px;
                font-size: 11px;
                font-weight: 600;
            }

            .tag.green {
                background: rgba(34, 197, 94, 0.08);
                color: #15803d;
            }

            .tag.gray {
                background: rgba(148, 163, 184, 0.2);
                color: #475569;
            }

            .mini-loader {
                width: 22px;
                height: 22px;
                border: 3px solid #e2e8f0;
                border-top: 3px solid #667eea; /* viola / blu */
                border-radius: 50%;
                animation: spin 0.8s linear infinite;
                margin-top: 6px;
            }

            @keyframes spin {
                0% {
                    transform: rotate(0deg);
                }
                100% {
                    transform: rotate(360deg);
                }
            }

        </style>

        <script>
            // consenti solo lettere, spazi e apostrofo
            function onlyLetters(input) {
                input.value = input.value.replace(/[^A-Za-zÀ-ÖØ-öø-ÿ\s']/g, '');
            }

            const baseUrl = '<%= ctx%>/admin/users';

            async function fetchPerson(type) {
                let cfInputId = (type === 'paziente') ? 'cf_edit_paz' : 'cf_edit_med';
                let loaderId = (type === 'paziente') ? 'loader_paz' : 'loader_med';
                let cf = document.getElementById(cfInputId).value.trim();
                let loader = document.getElementById(loaderId);

                // Se CF non completo → reset e nascondi loader
                if (cf.length < 16) {

                    loader.style.display = "none";

                    if (type === 'paziente') {
                        document.getElementById('nome_edit_paz').value = '';
                        document.getElementById('cognome_edit_paz').value = '';
                        document.getElementById('mail_edit_paz').value = '';
                        document.getElementById('tel_edit_paz').value = '';
                        document.getElementById('sesso_edit_paz').value = 'M';
                        document.getElementById('data_n_edit_paz').value = '';
                    } else {
                        document.getElementById('nome_edit_med').value = '';
                        document.getElementById('cognome_edit_med').value = '';
                        document.getElementById('mail_edit_med').value = '';
                    }
                    return;
                }

                // Mostra loader
                loader.style.display = "block";

                try {
                    const res = await fetch(
                            baseUrl +
                            '?op=fetchPerson&type=' + encodeURIComponent(type) +
                            '&cf=' + encodeURIComponent(cf)
                            );

                    const data = await res.json();

                    // Nascondi loader appena arriva la risposta
                    loader.style.display = "none";

                    if (data.error) {
                        console.log(data.error);
                        return;
                    }

                    if (type === 'paziente') {
                        document.getElementById('nome_edit_paz').value = data.nome || '';
                        document.getElementById('cognome_edit_paz').value = data.cognome || '';
                        document.getElementById('mail_edit_paz').value = data.mail || '';
                        document.getElementById('tel_edit_paz').value = data.tel || '';
                        if (data.sesso)
                            document.getElementById('sesso_edit_paz').value = data.sesso;
                        if (data.data_n)
                            document.getElementById('data_n_edit_paz').value = data.data_n;
                    } else {
                        document.getElementById('nome_edit_med').value = data.nome || '';
                        document.getElementById('cognome_edit_med').value = data.cognome || '';
                        document.getElementById('mail_edit_med').value = data.mail || '';
                    }

                } catch (err) {
                    console.error(err);
                    loader.style.display = "none";
                }
            }


        </script>
    </head>
    <body>

        <div class="topbar">
            <div class="logo">Heart Monitor</div>
            <div class="subtitle">Admin · Gestione utenti</div>
            <div class="spacer"></div>
            <a href="<%= ctx%>/admin/model" class="toplink">Modello ML</a>
            <a href="<%= ctx%>/logout" class="toplink">Logout</a>
        </div>

        <div class="layout">
            <div class="sidebar">
                <a href="<%= ctx%>/admin/users" class="active">Gestione utenti</a>
                <a href="<%= ctx%>/admin/model">Modello ML</a>
            </div>

            <div class="main">
                <div class="admin-badge">
                    Pannello Admin · Pazienti, Medici, Account &amp; Assegnazioni
                </div>
                <div class="admin-section-title">Controllo completo degli utenti</div>
                <div class="admin-subtitle">
                    Registra anagrafiche, crea login solo per persone già presenti, assegna i pazienti ai medici e gestisci gli account in modo sicuro.
                </div>

                <% if (message != null) {%>
                <div class="alert-banner success">
                    ✅ <span><%= message%></span>
                </div>
                <% } %>

                <% if (error != null) {%>
                <div class="alert-banner error">
                    ⚠️ <span><%= error%></span>
                </div>
                <% }%>

                <div class="admin-grid">
                    <!-- Card 1: Nuove anagrafiche -->
                    <div class="card">
                        <div class="card-title">Registra nuova persona</div>
                        <p class="admin-subtitle">Prima si registra la persona nel database, poi si crea l'account di accesso.</p>

                        <div class="admin-pill">Paziente</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="addPerson">
                            <input type="hidden" name="type" value="paziente">

                            <div class="admin-row">
                                <div>
                                    <label>Nome</label>
                                    <input type="text" name="nome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                                <div>
                                    <label>Cognome</label>
                                    <input type="text" name="cognome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                            </div>

                            <div class="admin-row">
                                <div>
                                    <label>Data nascita</label>
                                    <input type="date" name="data_n" required>
                                </div>
                                <div>
                                    <label>Sesso</label>
                                    <select name="sesso" required>
                                        <option value="M">M</option>
                                        <option value="F">F</option>
                                    </select>
                                </div>
                            </div>

                            <label>Codice fiscale</label>
                            <input type="text" name="cf" maxlength="16" minlength="16" required>

                            <label>Email</label>
                            <input type="email" name="mail" required>

                            <label>Telefono</label>
                            <input type="text" name="tel">

                            <button type="submit" class="btn-inline">
                                + Registra paziente
                            </button>
                        </form>

                        <hr style="margin: 20px 0; border: none; border-top: 1px dashed #e2e8f0;">

                        <div class="admin-pill">Medico</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="addPerson">
                            <input type="hidden" name="type" value="medico">

                            <div class="admin-row">
                                <div>
                                    <label>Nome</label>
                                    <input type="text" name="nome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                                <div>
                                    <label>Cognome</label>
                                    <input type="text" name="cognome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                            </div>

                            <label>Codice fiscale</label>
                            <input type="text" name="cf" maxlength="16" minlength="16" required>

                            <label>Email</label>
                            <input type="email" name="mail" required>

                            <button type="submit" class="btn-inline">
                                + Registra medico
                            </button>
                        </form>
                    </div>

                    <!-- Card 2: Account login -->
                    <div class="card">
                        <div class="card-title">Account di accesso</div>
                        <p class="admin-subtitle">La registrazione di username e password è permessa solo se la persona è già censita.</p>

                        <div class="admin-pill">Crea account</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="createAccount">

                            <label>Tipo utente</label>
                            <select name="type" required>
                                <option value="paziente">Paziente</option>
                                <option value="medico">Medico</option>
                            </select>

                            <label>Codice fiscale persona</label>
                            <input type="text" name="cf" maxlength="16" minlength="16" required>

                            <label>Username</label>
                            <input type="text" name="username" required>

                            <label>Password</label>
                            <input type="password" name="password" required>

                            <button type="submit" class="btn-inline">
                                🔐 Crea account
                            </button>
                        </form>

                        <hr style="margin: 20px 0; border: none; border-top: 1px dashed #e2e8f0;">

                        <div class="admin-pill">Elimina account</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="deleteAccount">

                            <label>Tipo utente</label>
                            <select name="type" required>
                                <option value="paziente">Paziente</option>
                                <option value="medico">Medico</option>
                            </select>

                            <label>Codice fiscale persona</label>
                            <input type="text" name="cf" maxlength="16" minlength="16" required>

                            <button type="submit" class="btn-inline secondary">
                                🗑️ Elimina account
                            </button>
                        </form>
                    </div>

                    <!-- Card 3: Assegnazioni medico/paziente -->
                    <div class="card">
                        <div class="card-title">Assegnazioni paziente ↔ medico</div>
                        <p class="admin-subtitle">
                            Il paziente viene agganciato al medico tramite codice fiscale.  
                            Se è già assegnato a un medico, il sistema blocca il cambio.
                        </p>

                        <div class="admin-pill">Assegna paziente a medico</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="assignDoctor">

                            <label>CF paziente</label>
                            <input type="text" name="cf_paz" maxlength="16" minlength="16" required>

                            <label>CF medico</label>
                            <input type="text" name="cf_med" maxlength="16" minlength="16" required>

                            <button type="submit" class="btn-inline">
                                👨‍⚕️ Assegna
                            </button>
                        </form>

                        <hr style="margin: 20px 0; border: none; border-top: 1px dashed #e2e8f0;">

                        <div class="admin-pill">Disassegna paziente</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="unassignDoctor">

                            <label>CF paziente</label>
                            <input type="text" name="cf_paz" maxlength="16" minlength="16" required>

                            <button type="submit" class="btn-inline secondary">
                                ↩️ Rimuovi assegnazione
                            </button>
                        </form>
                    </div>

                    <!-- Card 4: Modifica dati anagrafici -->
                    <div class="card">
                        <div class="card-title">Modifica dati persona</div>
                        <p class="admin-subtitle">Aggiorna rapidamente i dati anagrafici di pazienti e medici partendo dal codice fiscale.</p>

                        <div class="admin-pill">Paziente</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="editPerson">
                            <input type="hidden" name="type" value="paziente">

                            <label>CF attuale paziente</label>
                            <input type="text" id="cf_edit_paz" name="cf" maxlength="16" required oninput="fetchPerson('paziente')">
                            <div id="loader_paz" class="mini-loader" style="display:none;"></div>


                            <div class="admin-row">
                                <div>
                                    <label>Nuovo nome</label>
                                    <input type="text" id="nome_edit_paz" name="nome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                                <div>
                                    <label>Nuovo cognome</label>
                                    <input type="text" id="cognome_edit_paz" name="cognome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                            </div>

                            <div class="admin-row">
                                <div>
                                    <label>Data nascita</label>
                                    <input type="date" id="data_n_edit_paz" name="data_n" required>
                                </div>
                                <div>
                                    <label>Sesso</label>
                                    <select id="sesso_edit_paz" name="sesso" required>
                                        <option value="M">M</option>
                                        <option value="F">F</option>
                                    </select>
                                </div>
                            </div>

                            <label>Nuova email</label>
                            <input type="email" id="mail_edit_paz" name="mail" required>

                            <label>Nuovo telefono</label>
                            <input type="text" id="tel_edit_paz" name="tel">

                            <label>Nuovo CF (opzionale)</label>
                            <input type="text" name="new_cf" maxlength="16" minlength="16">

                            <button type="submit" class="btn-inline">
                                ✏️ Aggiorna paziente
                            </button>
                        </form>

                        <hr style="margin: 20px 0; border: none; border-top: 1px dashed #e2e8f0;">

                        <div class="admin-pill">Medico</div>
                        <form method="post" action="<%= ctx%>/admin/users" class="admin-form">
                            <input type="hidden" name="op" value="editPerson">
                            <input type="hidden" name="type" value="medico">

                            <label>CF attuale medico</label>
                            <input type="text" id="cf_edit_med" name="cf" maxlength="16" required oninput="fetchPerson('medico')">
                            <div id="loader_med" class="mini-loader" style="display:none;"></div>


                            <div class="admin-row">
                                <div>
                                    <label>Nuovo nome</label>
                                    <input type="text" id="nome_edit_med" name="nome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                                <div>
                                    <label>Nuovo cognome</label>
                                    <input type="text" id="cognome_edit_med" name="cognome" required
                                           pattern="[A-Za-zÀ-ÖØ-öø-ÿ\s']+" oninput="onlyLetters(this)">
                                </div>
                            </div>

                            <label>Nuova email</label>
                            <input type="email" id="mail_edit_med" name="mail" required>

                            <label>Nuovo CF (opzionale)</label>
                            <input type="text" name="new_cf" maxlength="16" minlength="16">

                            <button type="submit" class="btn-inline">
                                ✏️ Aggiorna medico
                            </button>
                        </form>
                    </div>
                </div>

                <!-- Card Elenco pazienti / medici -->
                <div class="card" style="margin-top: 24px;">
                    <div class="card-title">Elenco rapido pazienti e medici</div>
                    <p class="admin-subtitle">
                        Vista di riepilogo per controllare assegnazioni, CF e presenza dell'account.
                    </p>

                    <h4>Pazienti</h4>
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Nome</th>
                                <th>CF</th>
                                <th>Email</th>
                                <th>Telefono</th>
                                <th>Medico</th>
                                <th>Account</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (pazienti != null) {
                                    for (Paziente p : pazienti) {
                            %>
                            <tr>
                                <td><%= p.getCognome()%> <%= p.getNome()%></td>
                                <td><%= p.getCf()%></td>
                                <td><%= p.getMail()%></td>
                                <td><%= p.getNTel()%></td>
                                <td>
                                    <% if (p.getIdMedico() != 0) {%>
                                    <span class="tag green">Assegnato (ID <%= p.getIdMedico()%>)</span>
                                    <% } else { %>
                                    <span class="tag gray">Non assegnato</span>
                                    <% } %>
                                </td>
                                <td>
                                    <% if (p.getIdUtente() != 0) { %>
                                    <span class="tag green">Account OK</span>
                                    <% } else { %>
                                    <span class="tag gray">Nessun account</span>
                                    <% } %>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                        </tbody>
                    </table>

                    <h4 style="margin-top: 24px;">Medici</h4>
                    <table class="admin-table">
                        <thead>
                            <tr>
                                <th>Nome</th>
                                <th>CF</th>
                                <th>Email</th>
                                <th>Account</th>
                            </tr>
                        </thead>
                        <tbody>
                            <%
                                if (medici != null) {
                                    for (Medico m : medici) {
                            %>
                            <tr>
                                <td><%= m.getCognome()%> <%= m.getNome()%></td>
                                <td><%= m.getCf()%></td>
                                <td><%= m.getMail()%></td>
                                <td>
                                    <% if (m.getIdUtente() != 0) { %>
                                    <span class="tag green">Account OK</span>
                                    <% } else { %>
                                    <span class="tag gray">Nessun account</span>
                                    <% } %>
                                </td>
                            </tr>
                            <%
                                    }
                                }
                            %>
                        </tbody>
                    </table>
                </div>
            </div>
        </div>

    </body>
</html>
