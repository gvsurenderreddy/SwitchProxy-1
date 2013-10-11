<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page contentType="text/plain" pageEncoding="UTF-8"%>
<%=
	
	ProxyRequestFilterSingleton.getInstance().getCommitedTasks().get(
		Integer.parseInt(request.getParameter("index"))).getContent()

%>