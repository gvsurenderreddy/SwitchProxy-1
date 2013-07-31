/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import com.wpg.proxy.Proxy;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebServlet;
import net.sourceforge.jhttpp2.Jhttpp2Launcher;
import net.sourceforge.jhttpp2.Jhttpp2Server;
import si.unilj.nuk.wpgproxy.ProxySingleton;

/**
 *
 * @author mitja
 */
@WebServlet(name = "ProxyServlet", urlPatterns = {"/proxy"}, loadOnStartup = 1)
public class ProxyServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init(); //To change body of generated methods, choose Tools | Templates.
//		ProxySingleton.getInstance().start();
		
		System.out.println("Proxy initializing..");
		
		ProxyRequestFilter filter = ProxyRequestFilterSingleton.getInstance();
		try {
			filter.setCommitedTasksMaxSize(Integer.parseInt(getServletContext().getInitParameter("filter.commitedTasks.maxSize")));
		}
		catch(NumberFormatException e) {}
//		startProxy();
	}

//	@Override
//	public void destroy() {
//		super.destroy(); //To change body of generated methods, choose Tools | Templates.
//		
////		ProxySingleton.getInstance().stop();
//	}
	
	
	
	protected void startProxy() {
		Proxy.main(new String[0]);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		if("restart".equals(req.getParameter("action"))) {
			int port = -1;
			try {
				port = Integer.parseInt(req.getParameter("port"));
			}
			catch(Exception e) {}
			
			ProxySingleton proxy = ProxySingleton.getInstance();
			if(port > 0) {
				proxy.setPort(port);
			}
			proxy.restart();
			
			resp.getOutputStream().println("Proxy restarted on port(-1 not changed): " + port);
			resp.getOutputStream().close();
		}
		else if("start".equals(req.getParameter("action"))) {
			int port = -1;
			try {
				port = Integer.parseInt(req.getParameter("port"));
			}
			catch(Exception e) {}
			
			ProxySingleton proxy = ProxySingleton.getInstance();
			if(port > 0) {
				proxy.setPort(port);
			}
			proxy.start();
			
			resp.getOutputStream().println("Proxy restarted on port(-1 not changed): " + port);
			resp.getOutputStream().close();
		}
		else if("main".equals(req.getParameter("action"))) {
			String[] args = new String[] {
				getServletContext().getInitParameter("proxy.listen.port"),
				getServletContext().getInitParameter("proxy.listen.address"),
				req.getServletContext().getRealPath("WEB-INF/exproxy-keystore")
			};
			Proxy.main(args);
			
			resp.getOutputStream().println("Main - proxy started: " + args[1] + ":" + args[0]);
			resp.getOutputStream().close();
		}
		else if("main-jhttpp2".equals(req.getParameter("action"))) {
				Jhttpp2Server	server = new Jhttpp2Server();
				server.SERVER_PROPERTIES_FILE = req.getServletContext().getRealPath("WEB-INF/jhttpp2/server.properties");
				server.DATA_FILE = req.getServletContext().getRealPath("WEB-INF/jhttpp2/server.data");
				server.MAIN_LOGFILE = req.getServletContext().getRealPath("WEB-INF/jhttpp2/server.log");
				server.log_access_filename = req.getServletContext().getRealPath("WEB-INF/jhttpp2/access.log");
				server.init();
				
				if (server.fatalError) {
					System.out.println("Error: " +  server.getErrorMessage());
				}
				else {
					new Thread(server).start();
						System.out.println("Running on port " + server.port);
				}
			
			resp.getOutputStream().println("Main - proxy started: ");
			resp.getOutputStream().close();
		}
	}


	
}
