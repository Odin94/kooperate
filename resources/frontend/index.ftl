<!doctype html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport"
          content="width=device-width, user-scalable=no, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0">
    <meta http-equiv="X-UA-Compatible" content="ie=edge">
    <link rel="stylesheet" href="css/index.css">
    <link rel="shortcut icon" type="image/x-icon" href="static/favicon.ico">
    <title>Document</title>
</head>
<body>
<header>
    <h1 style="position: center; text-align: center; margin-top: 50px;">Kooperate</h1>
</header>
<section id="section-login">
    <input type="checkbox" id="form-switch">
    <form id="login-form" action="/login" method="post">
        <input name="name" type="text" placeholder="Username" required>
        <input name="password" type="password" placeholder="Password" required>
        <button type="submit">Login</button>
        <label for="form-switch"><span>Register</span></label>
    </form>
    <form id="register-form" action="/register" method="post">
        <input name="name" type="text" placeholder="Username" required>
        <input name="email" type="email" placeholder="Email" required>
        <input name="password" type="password" placeholder="Password" required>
        <button type="submit">Register</button>
        <label for="form-switch">Already Member ? Sign In Now..</label>
    </form>
</section>
</body>
</html>