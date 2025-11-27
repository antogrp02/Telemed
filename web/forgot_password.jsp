<%-- Pagina: Password dimenticata --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html lang="it">
<head>
    <meta charset="UTF-8">
    <title>Heart Monitor - Password dimenticata</title>

    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">

    <style>
        .login-wrapper {
            display: flex;
            justify-content: center;
            align-items: center;
            height: 100vh;
        }

        .login-card {
            background: #ffffff;
            padding: 30px;
            border-radius: 12px;
            width: 350px;
            box-shadow: 0 4px 15px rgba(0,0,0,0.15);
            text-align: center;
        }

        .login-card h1 {
            margin-bottom: 10px;
            font-size: 24px;
        }

        .login-card p {
            margin-bottom: 20px;
            font-size: 14px;
            color: #555;
        }

        label {
            display: block;
            text-align: left;
            margin-bottom: 5px;
            font-weight: 600;
        }

        input[type="email"] {
            width: 100%;
            padding: 10px;
            border-radius: 6px;
            border: 1px solid #ccc;
            margin-bottom: 15px;
            font-size: 14px;
        }

        .btn-primary {
            width: 100%;
            padding: 10px;
            background: #0077cc;
            color: white;
            border: none;
            border-radius: 6px;
            font-size: 16px;
            cursor: pointer;
            transition: 0.2s;
        }

        .btn-primary:hover {
            background: #005fa3;
        }

        .error {
            background: #ffdddd;
            border: 1px solid #ff9b9b;
            padding: 10px;
            border-radius: 6px;
            color: #b30000;
            margin-top: 10px;
        }

        .success {
            background: #ddffdd;
            border: 1px solid #9bff9b;
            padding: 10px;
            border-radius: 6px;
            color: #0a7a0a;
            margin-top: 10px;
        }

        .mt-2 {
            margin-top: 15px;
        }

        a {
            color: #005fa3;
            text-decoration: none;
        }
        a:hover {
            text-decoration: underline;
        }
    </style>
</head>

<body class="bg-main">

<div class="login-wrapper">
    <div class="login-card">
        <h1>Password dimenticata</h1>
        <p>Inserisci la tua email. Se esiste, riceverai un link per reimpostare la password.</p>

        <form method="post" action="<%= request.getContextPath() %>/password/forgot">
            
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required>

            <button type="submit" class="btn-primary">Invia link di reset</button>

            <% String error = (String) request.getAttribute("error"); %>
            <% if (error != null) { %>
                <div class="error"><%= error %></div>
            <% } %>

            <% String success = (String) request.getAttribute("success"); %>
            <% if (success != null) { %>
                <div class="success"><%= success %></div>
            <% } %>

        </form>

        <p class="mt-2">
            <a href="<%= request.getContextPath() %>/login.jsp">Torna al login</a>
        </p>
    </div>
</div>

</body>
</html>
