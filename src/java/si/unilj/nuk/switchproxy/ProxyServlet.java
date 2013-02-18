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
import javax.servlet.annotation.WebServlet;
import si.unilj.nuk.wpgproxy.ProxySingleton;

/**
 *
 * @author mitja
 */
@WebServlet(name = "ProxyServlet", urlPatterns = {"/proxy"})
public class ProxyServlet extends HttpServlet {

	@Override
	public void init() throws ServletException {
		super.init(); //To change body of generated methods, choose Tools | Templates.
//		ProxySingleton.getInstance().start();
		
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
				getServletContext().getInitParameter("proxy.listen.address")
			};
			Proxy.main(args);
			
			resp.getOutputStream().println("Main - proxy started: " + args[1] + ":" + args[0]);
			resp.getOutputStream().close();
		}
	}
	
	
	
}
