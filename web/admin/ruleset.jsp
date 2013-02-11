<%@page import="java.util.Vector"%>
<%@page import="si.unilj.nuk.switchproxy.UrlMatchRule"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page import="com.sun.jmx.remote.internal.ProxyRef"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
	
	Vector<UrlMatchRule> ruleSet = ProxyRequestFilterSingleton.getInstance().getRuleSet();

	if("add".equals(request.getParameter("action"))) {
		ruleSet.add(new UrlMatchRule(request.getParameter("urlpattern"), request.getParameter("script")));
	}
	else if("remove".equals(request.getParameter("action"))) {
		ruleSet.remove(Integer.parseInt(request.getParameter("index")));
	}
	
%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>JSP Page</title>
	</head>
	<body>
		<table border="1" cellspacing="0">
			<thead>
				<tr>
					<th></th>
					<th>Url pattern</th>
					<th>Script</th>
				</tr>
			</thead>
			<tbody>
				<%
					
					int idx = 0;
					for(UrlMatchRule r : ruleSet) {
						%>
						<tr>
							<td><a href="?action=remove&index=<%=idx++%>">Remove</a></td>
							<td><%=r.getUrlPattern().toString() %></td>
							<td><pre><%=r.getClientScript() %></pre></td>
						</tr>						
						<%
					}
					
				%>
				<tr>
					
				</tr>
			</tbody>
		</table>
		<hr>
		<form action="?action=add" method="post">
			Url pattern <input name="urlpattern" type="text"><br>
			Script:<br>
			<textarea cols="100" rows="10" name="script"></textarea><br>
			<input type="submit">
		</form>
	</body>
</html>
