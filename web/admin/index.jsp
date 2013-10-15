<%@page contentType="text/html" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
	<head>
		<meta http-equiv="Content-Type" content="text/html; charset=UTF-8">
		<title>Admin tooling</title>
	</head>
	<body>
		<h1>Tools</h1>
		<ul>
			<li>
				<a href="active-tasks.jsp">Active tasks</a><br>
				Currently processing task.
			</li>
			<li>
				<a href="commited-tasks.jsp">Commited tasks</a><br>
				Currently processing task.
			</li>
			<li>
				<a href="ruleset.jsp">Ruleset</a><br>
				Rules for dispatcher.
			</li>
			<li style="margin-top:20px">
				Testing(processing workflow)<br>
				<br>
				<ol>
					<li><a href="task-inject.jsp">Task inject</a>: Injects task into queue;</li>
					<li><a href="task-queue.jsp">Task queue</a>: Holds list of queued task waiting for renderer to fetch;</li>
					<li><a href="active-tasks.jsp">Active tasks</a>: After browser fetched task, its copy is stored in this list where it can be canceled;</li>
					<li><a href="commited-tasks.jsp">Commited tasks</a>: Task completed or canceled are moved to this list.</li>
				</ol>			
			</li>
		</ul>
		<h2>Proxy</h2>
		<ul>
			<li>
				<a href="../proxy?action=main-jhttpp2">Start jHTTPp2</a><br>
				Starts proxy
			</li>
			<li>
				<a href="proxy-man.jsp">Proxy manager</a><br>
			</li>			
		</ul>
		
	</body>
</html>
