<%-- 
    Document   : task-inject
    Created on : Feb 11, 2013, 7:41:37 AM
    Author     : mitja
--%>

<%@page import="si.unilj.nuk.switchproxy.UrlMatchRule"%>
<%@page import="si.unilj.nuk.switchproxy.RenderTask"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilter"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>JSP Page</title>
	</head>
	<body>
		<%
		
		if(request.getMethod().equals("POST")) {
			ProxyRequestFilterSingleton.getInstance().getTaskQueue().add(
				new RenderTask(
					  request.getParameter("url"),
					  new UrlMatchRule(
							"injection",
							request.getParameter("script"))));
			
			%>
			Injected!
			<hr>
			<%
		}
		
		%>
		<form method="post">
			url: <input type="text" name="url"><br>
			script: <textarea name="script"></textarea><br>
			<input type="submit">
		</form>
	</body>
</html>
