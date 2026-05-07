<%@ page language="java" contentType="text/html; charset=UTF-8"
    pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
<meta charset="UTF-8">
<title>Login Page</title>
</head>
<body>
<center>
    <form action="LoginServlet" method="post">
        <table border=1>
        
            <tr>
                <th colspan="2">Login Form</th>
            </tr>
            
            <tr>
                <td>Enter User Id:</td>
                <td><input type="text" name="userId" required></td>
            </tr>
            
            <tr>
                <td>Enter Password:</td>
                <td><input type="password" name="pwd" required></td>
            </tr>
            
            <tr>
                <td><input type="submit" value="Submit"></td>
                <td><input type="reset" value="Refresh"></td>
            </tr>
            
        </table>
    </form>
</center>
</body>
</html>