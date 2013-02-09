<%-- 
    Document   : store
    Created on : Feb 8, 2013, 10:26:11 AM
    Author     : mitja
--%>

<%@page import="java.io.PrintWriter"%>
<%@page import="java.io.File"%>
<%

	PrintWriter pw = new PrintWriter("/tmp/test.txt");
	pw.write(request.getParameter("htmlContent"));
	pw.close();

%>