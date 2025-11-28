<%-- 
    Document   : newjsp
    Created on : 28 nov 2025, 15:22:02
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<%
    String msg = (String) request.getAttribute("err");
    if (msg == null) msg = "Operazione non disponibile.";
%>

<!DOCTYPE html>
<html>
<head>
    <title>Avviso - Heart Monitor</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
    <style>
        .center-wrapper {
            min-height: calc(100vh - 80px);
            display: flex;
            align-items: center;
            justify-content: center;
            padding: 40px 20px;
        }

        .alert-card {
            background: #ffffff;
            border-radius: 20px;
            padding: 32px 40px;
            max-width: 480px;
            width: 100%;
            box-shadow: 0 12px 40px rgba(15,23,42,0.12);
            text-align: center;
            animation: fadeSlide 0.4s ease-out;
            border: 1px solid #e2e8f0;
        }

        @keyframes fadeSlide {
            from {
                opacity: 0;
                transform: translateY(10px);
            }
            to {
                opacity: 1;
                transform: translateY(0);
            }
        }

        .alert-icon-emoji {
            font-size: 48px;
            margin-bottom: 16px;
            animation: pulseIcon 1.8s ease-in-out infinite;
        }

        @keyframes pulseIcon {
            0%,100% { transform: scale(1); }
            50% { transform: scale(1.08); }
        }

        .alert-title {
            font-size: 22px;
            font-weight: 700;
            color: #0f172a;
            margin-bottom: 16px;
        }

        .alert-text {
            font-size: 15px;
            color: #475569;
            line-height: 1.6;
            margin-bottom: 28px;
        }

        .btn-return {
            display: inline-block;
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            color: #ffffff;
            padding: 12px 22px;
            border-radius: 12px;
            font-weight: 700;
            font-size: 14px;
            text-decoration: none;
            box-shadow: 0 4px 16px rgba(102,126,234,0.35);
            transition: all 0.3s ease;
        }

        .btn-return:hover {
            transform: translateY(-2px);
            box-shadow: 0 8px 24px rgba(102,126,234,0.45);
        }
    </style>
</head>

<body>

    <!-- TOPBAR RIUTILIZZATA -->


    <div class="center-wrapper">
        <div class="alert-card">

            <div class="alert-icon-emoji">⚠️</div>

            <div class="alert-title">Avviso</div>

            <div class="alert-text">
                <%= msg %>
            </div>

            <a href="<%= request.getContextPath() %>/patient/dashboard" class="btn-return">
                Torna alla dashboard
            </a>

        </div>
    </div>

</body>
</html>

