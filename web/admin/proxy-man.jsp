<%-- 
    Document   : proxy-man
    Created on : Oct 11, 2013, 11:52:36 AM
    Author     : Mitja Kovačič(mitja.kovacic@nuk.uni-lj.si)
--%>

<%@page import="net.sourceforge.jhttpp2.Jhttpp2Server"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyInstance"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
	if("stop".equals(request.getParameter("action"))) {
		((Jhttpp2Server)ProxyInstance.instance).stop();
		
		response.sendRedirect("proxy-man.jsp");
	}
%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>JSP Page</title>
	</head>
	<body>
		<%@include file="header.jsp" %>
		Proxy running: <%= ProxyInstance.instance %> 
		<button onclick="location.href='?action=stop'">Stop</button>
	</body>
</html>
