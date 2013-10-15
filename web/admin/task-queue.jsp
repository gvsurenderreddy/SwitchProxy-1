<%@page import="java.util.HashMap"%>
<%@page import="java.util.Queue"%>
<%@page import="si.unilj.nuk.switchproxy.RenderTask"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%

	if("takenext".equals(request.getParameter("action"))) {
		RenderTask task = ProxyRequestFilterSingleton.getInstance().nextTask();
				  		
		response.sendRedirect("queue.jsp");
	}
	
%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Task queue</title>
	</head>
	<body>
		<%@include file="header.jsp" %>
		<button onclick="location.href='?action=takenext'">Take next</button>
		<hr>
		<table width="100%" cellpadding="10" border="1" cellspacing="0">
			<thead>
				<tr>
					<th witdh="200">Created</th>
					<th witdh="200">UUID</th>
					<th>Task description</th>
				</tr>
			</thead>
			<tbody>
				<%

					Queue<RenderTask> taskQueue = ProxyRequestFilterSingleton.getInstance().getTaskQueue();
					
					for(RenderTask t : taskQueue) {
						%>
						<tr>
							<td><%= t.getCreateDate() %></td>
							<td><%= t.getId() %></td>
							<td>
								<%= t.getUrl()%><br>
								<b><%= t.getRule().getUrlPattern() %></b>
								<hr>
								<%= t.getRule().getClientScript()%>
							</td>
						</tr>
						<%
					}

				%>
			</tbody>
		</table>
	</body>
</html>
