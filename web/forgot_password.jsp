<%-- Pagina: password dimenticata --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Heart Monitor - Password dimenticata</title>
    <link rel="stylesheet" href="../css/style.css">
</head>
<body class="bg-main">

<div class="login-wrapper">
    <div class="login-card">
        <h1>Password dimenticata</h1>
        <p>Inserisci il tuo indirizzo email. Se presente nei nostri sistemi, riceverai un link per resettare la password.</p>

        <form method="post" action="password/forgot">
            <label for="email">Email</label>
            <input type="email" id="email" name="email" required>

            <button type="submit" class="btn-primary">Invia link di reset</button>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
                <div class="error"><%= error %></div>
            <% } %>

            <%
                String success = (String) request.getAttribute("success");
                if (success != null) {
            %>
                <div class="success"><%= success %></div>
            <% } %>
        </form>

        <p class="mt-2">
            <a href="../login.jsp">Torna al login</a>
        </p>
    </div>
</div>

</body>
</html>
