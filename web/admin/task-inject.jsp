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
		
		<link type="text/css" rel="Stylesheet" href="../res/codemirror.css" />
		<link type="text/css" rel="Stylesheet" href="../res/ui-lightness/jquery-ui-1.10.3.custom.css" />
		
		<script type="text/javascript" src="../res/jquery-1.9.1.js"></script>
		<script type="text/javascript" src="../res/codemirror.js"></script>
		<script type="text/javascript" src="../res/codemirror/javascript.js"></script>
		<script type="text/javascript" src="../res/jquery-ui-1.10.3.custom.js"></script>
		<script type="text/javascript">
			 $(window).load(function () {
				var editor = CodeMirror.fromTextArea($('textarea')[0], {
				  mode: "text/javascript",
				  lineNumbers : true
				});
				
				setTimeout(function() {
					$('.CodeMirror').resizable({
					resize: function() {
					  editor.setSize($(this).width(), $(this).height());
					}
				 });
				}, 1000);
			 });
		</script>
		<style>

			.CodeMirror {
				border: 1px solid black;
			}
			
		</style>		
	</head>
	<body>
		<%@include file="header.jsp" %>
		<%
		
			String url = request.getParameter("url");
			if(url == null) url = "";
			String script = request.getParameter("script");
			if(script == null) script =
			"{\n\tonPageLoaded: function() {\n\t\tPhantomRendererAPI.commit({});\n\t}\n}";
			
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
		<h3>Page context API</h3>
		<pre>
PhantomRendererAPI = {
	log : function(message),
	commit : function(options = {
		processHTML : function(html) { return html; },
		metadata : [{k:v},{k:v}...]
	})
}
		</<pre>
<h3>Client Script structure</h3><pre>
{
	injectJquery : true|false,

	onPageLoaded : function(),
	onResourceRequested : <a target="_blank" href="https://github.com/ariya/phantomjs/wiki/API-Reference-WebPage#wiki-webpage-onResourceRequested">function(requestData, networkRequest)</a>
}
		</<pre>
	</body>
</html>
