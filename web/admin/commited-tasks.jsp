<%@page import="java.util.Vector"%>
<%@page import="java.util.Map"%>
<%@page import="java.util.Map.Entry"%>
<%@page import="si.unilj.nuk.switchproxy.RenderTask"%>
<%@page import="java.util.Hashtable"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
	
	Vector<RenderTask> commitedTasks = ProxyRequestFilterSingleton.getInstance().getCommitedTasks();
	
	if("clear".equals(request.getParameter("action"))) {
		commitedTasks.clear();
		
		response.sendRedirect("commited-tasks.jsp");
	}
	
%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>JSP Page</title>
	</head>
	<body>
		<table width="100%" cellpadding="10" border="1" cellspacing="0">
			<thead>
				<tr>
					<th></th>
					<th witdh="200">UUID</th>
					<th>URL</th>
					<th>Content-length</th>
					<th>Headers</th>
				</tr>
			</thead>
			<tbody>
				<%
					
					int index = 0;
					for(RenderTask t : commitedTasks) {
						StringBuilder buff = new StringBuilder();
						for(Entry<String, String> p : t.getHeaders().entrySet()) {
							buff.append(p.getKey() + ": " + p.getValue() + "\n");
						}
						
						%>
						<tr>
							<td><a href="task-content.jsp?index=<%= index++ %>">Ogled vsebine</a></td>
							<td><%= t.getId() %></td>
							<td><%= t.getUrl() %></td>
							<td><%= t.getContent().length() %></td>
							<td><pre><%= buff.toString() %></pre></td>
						</tr>
						<%
					}

				%>
			</tbody>
		</table>
		<hr>
		<button onclick="location.href='?action=clear'">Clear</button>
	</body>
</html>
