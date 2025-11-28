<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String ctx = request.getContextPath();
    Integer role = (Integer) session.getAttribute("role");
    String dashUrl = null;

    if (role != null) {
        if (role == 0) {
            dashUrl = ctx + "/patient/dashboard";
        } else if (role == 1) {
            dashUrl = ctx + "/doctor/dashboard";
        } else if (role == 2) {
            dashUrl = ctx + "/admin/users";
        }
    }
%>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Heart Monitor - Monitora il tuo cuore, ovunque tu sia</title>

    <style>
        * {
            margin: 0;
            padding: 0;
            box-sizing: border-box;
        }

        :root {
            --primary: hsl(221.2 83.2% 53.3%);
            --primary-hover: hsl(221.2 83.2% 45%);
            --background: hsl(0 0% 100%);
            --foreground: hsl(222.2 84% 4.9%);
            --card: hsl(0 0% 98%);
            --border: hsl(214.3 31.8% 91.4%);
            --muted-foreground: hsl(215.4 16.3% 46.9%);
            --secondary: hsl(210 40% 96.1%);
        }

        body {
            font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'Roboto', 'Oxygen', 'Ubuntu', 'Cantarell', sans-serif;
            line-height: 1.6;
            color: var(--foreground);
            background-color: var(--background);
            -webkit-font-smoothing: antialiased;
        }

        .container {
            max-width: 1200px;
            margin: 0 auto;
            padding: 0 1rem;
        }

        .navbar {
            position: sticky;
            top: 0;
            z-index: 1000;
            background-color: rgba(255, 255, 255, 0.8);
            backdrop-filter: blur(12px);
            border-bottom: 1px solid var(--border);
        }

        .nav-content {
            display: flex;
            justify-content: space-between;
            align-items: center;
            padding: 1rem 0;
        }

        .logo {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-size: 1.25rem;
            font-weight: 700;
        }

        .icon-heart {
            width: 1.5rem;
            height: 1.5rem;
            color: var(--primary);
        }

        .btn {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            padding: 0.625rem 1.5rem;
            font-size: 0.875rem;
            font-weight: 500;
            border-radius: 0.375rem;
            text-decoration: none;
            cursor: pointer;
            transition: all 0.2s;
            border: none;
        }

        .btn-primary {
            background-color: var(--primary);
            color: white;
        }

        .btn-primary:hover {
            background-color: var(--primary-hover);
        }

        .btn-outline {
            background-color: var(--background);
            color: var(--foreground);
            border: 1px solid var(--border);
        }

        .btn-outline:hover {
            background-color: var(--secondary);
        }

        .btn-lg {
            padding: 1.25rem 2.5rem;
            font-size: 1rem;
        }

        .hero {
            position: relative;
            overflow: hidden;
        }

        .hero-bg {
            position: absolute;
            inset: 0;
            background: linear-gradient(135deg, hsla(221.2, 83.2%, 53.3%, 0.05) 0%, var(--background) 50%, hsla(210, 40%, 96.1%, 0.1) 100%);
        }

        .hero-content {
            position: relative;
            padding: 8rem 1rem;
        }

        .hero-inner {
            max-width: 56rem;
            margin: 0 auto;
            text-align: center;
            animation: fadeIn 0.8s ease-out;
        }

        @keyframes fadeIn {
            from {
                opacity: 0;
                transform: translateY(20px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .hero-icon {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            width: 5rem;
            height: 5rem;
            border-radius: 50%;
            background-color: hsla(221.2, 83.2%, 53.3%, 0.1);
            margin-bottom: 2rem;
        }

        .hero-icon svg {
            width: 2.5rem;
            height: 2.5rem;
            color: var(--primary);
        }

        .hero-title {
            font-size: 3rem;
            font-weight: 700;
            line-height: 1.2;
            margin-bottom: 1.5rem;
        }

        .text-primary {
            color: var(--primary);
        }

        .hero-description {
            font-size: 1.25rem;
            color: var(--muted-foreground);
            margin-bottom: 2.5rem;
            max-width: 42rem;
            margin-left: auto;
            margin-right: auto;
            line-height: 1.8;
        }

        .hero-cta {
            display: flex;
            justify-content: center;
        }

        .features {
            padding: 5rem 0;
            background-color: var(--card);
        }

        .section-header {
            text-align: center;
            margin-bottom: 4rem;
        }

        .section-title {
            font-size: 2.25rem;
            font-weight: 700;
            margin-bottom: 1rem;
        }

        .section-description {
            font-size: 1.125rem;
            color: var(--muted-foreground);
        }

        .features-grid {
            display: grid;
            grid-template-columns: repeat(auto-fit, minmax(300px, 1fr));
            gap: 2rem;
            max-width: 72rem;
            margin: 0 auto;
        }

        .feature-card {
            padding: 2rem;
            background-color: var(--background);
            border: 1px solid var(--border);
            border-radius: 0.5rem;
            transition: box-shadow 0.3s ease;
        }

        .feature-card:hover {
            box-shadow: 0 10px 30px rgba(0, 0, 0, 0.1);
        }

        .feature-icon {
            width: 3.5rem;
            height: 3.5rem;
            border-radius: 0.5rem;
            background-color: hsla(221.2, 83.2%, 53.3%, 0.1);
            display: flex;
            align-items: center;
            justify-content: center;
            margin-bottom: 1.5rem;
        }

        .feature-icon svg {
            width: 1.75rem;
            height: 1.75rem;
            color: var(--primary);
        }

        .feature-title {
            font-size: 1.25rem;
            font-weight: 600;
            margin-bottom: 0.75rem;
        }

        .feature-description {
            color: var(--muted-foreground);
            line-height: 1.7;
        }

        .cta {
            padding: 6rem 0;
            background: linear-gradient(135deg, hsla(221.2, 83.2%, 53.3%, 0.05) 0%, var(--background) 50%, hsla(210, 40%, 96.1%, 0.1) 100%);
        }

        .cta-content {
            max-width: 48rem;
            margin: 0 auto;
            text-align: center;
        }

        .cta-title {
            font-size: 2.5rem;
            font-weight: 700;
            margin-bottom: 1.5rem;
        }

        .cta-description {
            font-size: 1.25rem;
            color: var(--muted-foreground);
            margin-bottom: 2.5rem;
        }

        .cta-buttons {
            display: flex;
            flex-wrap: wrap;
            gap: 1rem;
            justify-content: center;
        }

        .footer {
            padding: 2rem 0;
            border-top: 1px solid var(--border);
            background-color: var(--card);
        }

        .footer-content {
            display: flex;
            flex-direction: column;
            align-items: center;
            gap: 1rem;
        }

        .footer-logo {
            display: flex;
            align-items: center;
            gap: 0.5rem;
            font-weight: 600;
        }

        .footer-copyright {
            font-size: 0.875rem;
            color: var(--muted-foreground);
        }

        @media (max-width: 768px) {
            .hero-title {
                font-size: 2rem;
            }
            
            .hero-description {
                font-size: 1rem;
            }
            
            .section-title {
                font-size: 1.75rem;
            }
            
            .cta-title {
                font-size: 1.75rem;
            }
            
            .features-grid {
                grid-template-columns: 1fr;
            }
            
            .cta-buttons {
                flex-direction: column;
            }
        }

        @media (min-width: 768px) {
            .footer-content {
                flex-direction: row;
                justify-content: space-between;
            }
        }
    </style>
</head>

<body>

    <!-- NAVBAR -->
    <nav class="navbar">
        <div class="container">
            <div class="nav-content">

                <div class="logo">
                    <svg class="icon-heart" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                    <span>Heart Monitor</span>
                </div>

                <div style="display: flex; gap: 1rem;">
                    <% if (role == null) { %>
                        <!-- Utente NON loggato -->
                        <a href="<%= ctx %>/login.jsp" class="btn btn-primary">Accedi</a>
                    <% } else { %>
                        <!-- Utente loggato -->
                        <a href="<%= dashUrl %>" class="btn btn-outline">Vai alla Dashboard</a>
                        <a href="<%= ctx %>/logout" class="btn btn-primary">Logout</a>
                    <% } %>
                </div>

            </div>
        </div>
    </nav>

    <!-- HERO -->
    <section class="hero">
        <div class="hero-bg"></div>
        <div class="container hero-content">
            <div class="hero-inner">
                <div class="hero-icon">
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                </div>

                <h1 class="hero-title">
                    Monitora il tuo cuore,<br />
                    <span class="text-primary">ovunque tu sia</span>
                </h1>

                <p class="hero-description">
                    Heart Monitor è l'app professionale per il monitoraggio cardiaco in tempo reale. 
                    Controllo continuo, analisi avanzate e notifiche istantanee per la tua salute.
                </p>

                <div class="hero-cta">
                    <% if (role == null) { %>
                        <a href="<%= ctx %>/login" class="btn btn-primary btn-lg">Accedi</a>
                    <% } else { %>
                        <a href="<%= dashUrl %>" class="btn btn-primary btn-lg">Vai alla Dashboard</a>
                    <% } %>
                </div>
            </div>
        </div>
    </section>

    <!-- FEATURES -->
    <section class="features">
        <div class="container">
            <div class="section-header">
                <h2 class="section-title">Funzionalità avanzate</h2>
                <p class="section-description">Tutto ciò che ti serve per monitorare la tua salute cardiaca</p>
            </div>
            
            <div class="features-grid">
                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="22 12 18 12 15 21 9 3 6 12 2 12"></polyline>
                        </svg>
                    </div>
                    <h3 class="feature-title">Monitoraggio in tempo reale</h3>
                    <p class="feature-description">Traccia la tua frequenza cardiaca 24/7 con precisione medica e ricevi dati istantanei sul tuo smartphone.</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z"></path>
                        </svg>
                    </div>
                    <h3 class="feature-title">Notifiche intelligenti</h3>
                    <p class="feature-description">Ricevi avvisi immediati in caso di anomalie o valori fuori norma per intervenire tempestivamente.</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <polyline points="23 6 13.5 15.5 8.5 10.5 1 18"></polyline>
                            <polyline points="17 6 23 6 23 12"></polyline>
                        </svg>
                    </div>
                    <h3 class="feature-title">Analisi avanzate</h3>
                    <p class="feature-description">Visualizza grafici dettagliati, tendenze e statistiche per comprendere meglio la tua salute nel tempo.</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path>
                            <circle cx="9" cy="7" r="4"></circle>
                            <path d="M23 21v-2a4 4 0 0 0-3-3.87"></path>
                            <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                        </svg>
                    </div>
                    <h3 class="feature-title">Condivisione con medici</h3>
                    <p class="feature-description">Condividi i tuoi dati direttamente con il tuo medico per un follow-up professionale e accurato.</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <circle cx="12" cy="12" r="10"></circle>
                            <polyline points="12 6 12 12 16 14"></polyline>
                        </svg>
                    </div>
                    <h3 class="feature-title">Storico completo</h3>
                    <p class="feature-description">Accedi a tutti i tuoi dati storici e confronta i risultati per monitorare i tuoi progressi.</p>
                </div>

                <div class="feature-card">
                    <div class="feature-icon">
                        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                        </svg>
                    </div>
                    <h3 class="feature-title">Certificato medicalmente</h3>
                    <p class="feature-description">Dispositivo certificato e conforme agli standard medici internazionali per la massima affidabilità.</p>
                </div>
            </div>
        </div>
    </section>

    <!-- CTA -->
    <section class="cta">
        <div class="container">
            <div class="cta-content">
                <h2 class="cta-title">Inizia a prenderti cura del tuo cuore oggi</h2>
                <p class="cta-description">Unisciti a migliaia di utenti che hanno già scelto Heart Monitor per la loro salute cardiaca</p>
            </div>
        </div>
    </section>

    <!-- FOOTER -->
    <footer class="footer">
        <div class="container">
            <div class="footer-content">
                <div class="footer-logo">
                    <svg class="icon-heart" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
                        <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"></path>
                    </svg>
                    <span>Heart Monitor</span>
                </div>
                <p class="footer-copyright">© 2025 Heart Monitor. Tutti i diritti riservati.</p>
            </div>
        </div>
    </footer>

</body>
</html>
