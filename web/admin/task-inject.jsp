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
		<script type="text/javascript" src="../res/jquery-1.9.1.js"></script>
		<script type="text/javascript">
			// from: http://stackoverflow.com/questions/6637341/use-tab-to-indent-in-textarea
			$(document).delegate('textarea', 'keydown', function(e) {
				var keyCode = e.keyCode || e.which;

				if (keyCode == 9) {
				  e.preventDefault();
				  var start = $(this).get(0).selectionStart;
				  var end = $(this).get(0).selectionEnd;

				  // set textarea value to: text before caret + tab + text after caret
				  $(this).val($(this).val().substring(0, start)
								  + "\t"
								  + $(this).val().substring(end));

				  // put caret at right position again
				  $(this).get(0).selectionStart =
				  $(this).get(0).selectionEnd = start + 1;
				}
			 });
		</script>		
	</head>
	<body>
		<%
		
			String url = request.getParameter("url");
			if(url == null) url = "";
			String script = request.getParameter("script");
			if(script == null) script = "";
			
			
		if(request.getMethod().equals("POST")) {
			ProxyRequestFilterSingleton.getInstance().getTaskQueue().add(
				new RenderTask(
					  url,
					  new UrlMatchRule(
							"injection",
							script)));
			
			%>
			Injected!
			<hr>
			<%
		}
		
		%>
		<form method="post">
			url: <input type="text" name="url" size="100" value="<%= url %>"><br>
			script:<br>
			<textarea name="script" cols="100" rows="20"><%= script %></textarea><br>
			<input type="submit">
		</form>
	</body>
</html>
