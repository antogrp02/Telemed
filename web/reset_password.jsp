<%-- Pagina: reset password --%>
<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
    <title>Heart Monitor - Reset password</title>
    <link rel="stylesheet" href="<%= request.getContextPath() %>/css/style.css">
</head>
<body class="bg-main">

<%
    String token = (String) request.getAttribute("token");
%>

<div class="login-wrapper">
    <div class="login-card">
        <h1>Imposta nuova password</h1>

        <form method="post" action="<%= request.getContextPath() %>/password/reset">
            <input type="hidden" name="token" value="<%= token != null ? token : "" %>">

            <label for="password">Nuova password</label>
            <input type="password" id="password" name="password" required>

            <label for="confirm">Conferma password</label>
            <input type="password" id="confirm" name="confirm" required>

            <button type="submit" class="btn-primary">Aggiorna password</button>

            <%
                String error = (String) request.getAttribute("error");
                if (error != null) {
            %>
                <div class="error"><%= error %></div>
            <% } %>
        </form>

        <p class="mt-2">
            <a href="<%= request.getContextPath() %>/login.jsp">Torna al login</a>
        </p>
    </div>
</div>

</body>
</html>
