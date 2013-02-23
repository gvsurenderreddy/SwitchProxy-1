<%@page import="si.unilj.nuk.switchproxy.RulesetLoader"%>
<%@page import="nu.xom.XPathContext"%>
<%@page import="nu.xom.Nodes"%>
<%@page import="nu.xom.Builder"%>
<%@page import="java.io.FileOutputStream"%>
<%@page import="org.apache.commons.io.FileUtils"%>
<%@page import="javax.xml.XMLConstants"%>
<%@page import="nu.xom.Attribute"%>
<%@page import="nu.xom.Text"%>
<%@page import="nu.xom.Document"%>
<%@page import="nu.xom.Serializer"%>
<%@page import="nu.xom.Element"%>
<%@page import="java.util.Vector"%>
<%@page import="si.unilj.nuk.switchproxy.UrlMatchRule"%>
<%@page import="si.unilj.nuk.switchproxy.ProxyRequestFilterSingleton"%>
<%@page import="com.sun.jmx.remote.internal.ProxyRef"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>
<%
	
	Vector<UrlMatchRule> ruleSet = ProxyRequestFilterSingleton.getInstance().getRuleSet();
	
	String urlPattern = "";
	String clientScript = "";
	int currentIndex = -1;

	if("set".equals(request.getParameter("action"))) {
		try { currentIndex = Integer.parseInt(request.getParameter("index")); }
		catch(NumberFormatException e) {}
		
		if(currentIndex < 0) {
			ruleSet.add(new UrlMatchRule(request.getParameter("urlpattern"), request.getParameter("script")));
		}
		else {
			ruleSet.set(currentIndex, new UrlMatchRule(request.getParameter("urlpattern"), request.getParameter("script")));
		}
		
		response.sendRedirect("ruleset.jsp");
	}
	else if("remove".equals(request.getParameter("action"))) {
		ruleSet.remove(Integer.parseInt(request.getParameter("index")));
		
		response.sendRedirect("ruleset.jsp");
	}
	else if("store".equals(request.getParameter("action"))) {
		try {
			RulesetLoader.SaveToXml(ruleSet, pageContext.getServletContext());
			
			response.sendRedirect("ruleset.jsp");
		}
		catch(Exception e) {
			response.getWriter().print(e.toString());
		}			
	}
	else if("load".equals(request.getParameter("action"))) {
		try {
			RulesetLoader.LoadFromXml(ruleSet, pageContext.getServletContext());
			response.sendRedirect("ruleset.jsp");
		}
		catch(Exception e) {
			throw e;
//			response.getWriter().print(e.toString());
		}			
	}
	else if("edit".equals(request.getParameter("action"))) {
		currentIndex = Integer.parseInt(request.getParameter("index"));
		
		UrlMatchRule umr = ruleSet.get(currentIndex);
		clientScript = umr.getClientScript();
		urlPattern = umr.getUrlPattern().toString();
	}
	else if("move-up".equals(request.getParameter("action"))) {
		currentIndex = Integer.parseInt(request.getParameter("index"));
		
		UrlMatchRule r1 = ruleSet.get(currentIndex);
		UrlMatchRule r2 = ruleSet.get(currentIndex - 1);
		
		ruleSet.set(currentIndex - 1, r1);
		ruleSet.set(currentIndex, r2);
	}
	else if("move-down".equals(request.getParameter("action"))) {
		currentIndex = Integer.parseInt(request.getParameter("index"));
		
		UrlMatchRule r1 = ruleSet.get(currentIndex);
		UrlMatchRule r2 = ruleSet.get(currentIndex + 1);
		
		ruleSet.set(currentIndex + 1, r1);
		ruleSet.set(currentIndex, r2);
	}

%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
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
		<style>
			.script {
				padding:0 !important;
			}
			.script pre {
				margin:0 !important;
				padding:10px;
				height:20px;
				overflow: hidden;
				border-left: 5px solid white;
				cursor: pointer;
			}
			.script pre:hover {
				border-left: 5px solid blue;
			}
			.script pre:focus {
				border-left: 5px solid red;
				height:auto;
				overflow: hidden;
			}
			th {
				background: #898989;
				color:white;
			}
			
		</style>
		<title>JSP Page</title>
	</head>
	<body>
		<h2>Ruleset</h2>
		<table border="1" cellspacing="0" width="100%">
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
							<td>
								<%
						
									if(idx > 0) {
										%><a href="?action=move-up&index=<%=idx%>">Up</a><%
									}
									if(idx < ruleSet.size() - 1) {
										%><a href="?action=move-down&index=<%=idx%>">Down</a><%
									}
						
								%>								
								<a href="?action=remove&index=<%=idx%>">Remove</a>
								<a href="?action=edit&index=<%=idx%>#form">Edit</a>
							</td>
							<td><%=r.getUrlPattern().toString() %></td>
							<td class="script"><pre tabindex="<%=idx%>"><%=r.getClientScript() %></pre></td>
						</tr>						
						<%
						
						idx++;
					}
					
				%>
			</tbody>
		</table>
		<hr>
		<h2>Rule editor</h2>
		<a name="form"></a>
		<form action="?action=set" method="post">
			<input name="index" type="hidden" value="<%= currentIndex %>">
			Url pattern <input name="urlpattern" type="text" size="100" value="<%= urlPattern %>"><br>
			Script:<br>
			<textarea cols="100" rows="20" name="script"><%= clientScript %></textarea><br>
			<input type="submit">
		</form>
		<hr>
		<h2>Persistence</h2>
		Xml file:  <b><%= pageContext.getServletContext().getRealPath("WEB-INF/ruleset.xml") %></b> |
		<button onclick="location.href='?action=store'">Save</button>
		<button onclick="location.href='?action=load'">Load</button>
	</body>
</html>
