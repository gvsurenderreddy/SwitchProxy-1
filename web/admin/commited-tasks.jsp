<%@page import="java.util.Date"%>
<%@page import="java.text.SimpleDateFormat"%>
<%@page import="java.text.DateFormat"%>
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
		<title>Commited tasks</title>
		<style>
			.headers {
				padding:0 !important;
			}
			.headers pre {
				margin:0 !important;
				padding:10px;
				height:20px;
				overflow: hidden;
				border-left: 5px solid white;
				cursor: pointer;
				max-width: 400px;
			}
			.headers pre:hover {
				border-left: 5px solid blue;
			}
			.headers pre:focus {
				border-left: 5px solid red;
				height:auto;
				overflow: hidden;
				max-width: none;
			}
			th {
				background: #898989;
				color:white;
			}
			
		</style>		
	</head>
	<body>
		Showing: <%= commitedTasks.size() %> of max <%= ProxyRequestFilterSingleton.getInstance().getCommitedTasksMaxSize() %>
		<button onclick="location.href='?action=clear'">Clear</button>
		<hr>
		<table width="100%" cellpadding="10" border="1" cellspacing="0">
			<thead>
				<tr>
					<th></th>
					<th witdh="200">UUID</th>
					<th witdh="200">Created</th>
					<th witdh="200">Commited</th>
					<th witdh="200">ms</th>
					<th>URL</th>
					<th>Content-length</th>
					<th>Headers</th>
				</tr>
			</thead>
			<tbody>
				<%
	
					DateFormat dateFormat = new SimpleDateFormat("dd.MM H:m:s:S");
					
					int index = 0;
					for(RenderTask t : commitedTasks) {
						StringBuilder buff = new StringBuilder();
						for(Entry<String, String> p : t.getHeaders().entrySet()) {
							buff.append(p.getKey() + ": " + p.getValue() + "\n");
						}
						
						Date created = t.getCreateDate();
						Date commited = t.getCommitDate();
						
						%>
						<tr>
							<td><a href="task-content.jsp?index=<%= index++ %>">Ogled vsebine</a></td>
							<td><%= t.getId() %></td>
							<td><%= dateFormat.format(created) %></td>
							<td><%= dateFormat.format(commited) %></td>
							<td><%= commited.getTime() - created.getTime() %></td>
							<td><%= t.getUrl() %></td>
							<td><%= t.getContent().length() %></td>
							<td class="headers"><pre tabindex="<%=index%>"><%= buff.toString() %></pre></td>
						</tr>
						<%
					}

				%>
			</tbody>
		</table>
	</body>
</html>
