<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Cambia password</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body>

<div class="login-wrapper">
    <div class="login-card">
        <h2 class="app-title">Cambia Password</h2>
        <p class="app-subtitle">Devi impostare una nuova password prima di continuare.</p>

        <form action="<%= request.getContextPath() %>/change-password" method="post" class="login-form">
            <label>Nuova password</label>
            <input type="password" name="newpass" required>

            <label>Conferma password</label>
            <input type="password" name="conf" required>

            <button type="submit" class="btn-primary">Conferma</button>

            <% String err = (String) request.getAttribute("err");
               if (err != null) { %>
                <div class="error"><%= err %></div>
            <% } %>
        </form>
    </div>
</div>

</body>
</html>
