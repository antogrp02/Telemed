<%-- 
    Document   : login
    Created on : 14 nov 2025, 18:09:42
    Author     : Antonio
--%>

<%@ page contentType="text/html; charset=UTF-8" %>
<!DOCTYPE html>
<html>
<head>
  <title>Heart Monitor - Login</title>
  <link rel="stylesheet" href="css/style.css">
</head>
<body class="bg-main">

<div class="login-wrapper">
  <div class="login-card">
    <h1 class="app-title">Heart Monitor</h1>
    <p class="app-subtitle">Telemonitoraggio Scompenso Cardiaco</p>

    <form method="post" action="login" class="login-form">
      <label>Username</label>
      <input type="text" name="username" required />

      <label>Password</label>
      <input type="password" name="password" required />

      <button type="submit" class="btn-primary">Accedi</button>

      <%
        String error = (String) request.getAttribute("error");
        if (error != null) {
      %>
        <div class="error"><%= error %></div>
      <% } %>
    </form>
  </div>
</div>

</body>
</html>
