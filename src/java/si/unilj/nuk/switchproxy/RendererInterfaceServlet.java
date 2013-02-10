/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *	REST interface for rendering task polling mechanism
 * 
 * 
 * @author mitja
 */
@WebServlet(name = "RendererInterfaceServlet", urlPatterns = {"/renderer-interface"})
public class RendererInterfaceServlet extends HttpServlet {
	private static class DummyTask {
		private final boolean valid = false;
	}
	private static class ExceptionTask extends DummyTask {
		
		private String message;
		private List<String> stacktrace = new ArrayList<String>();
		
		public ExceptionTask(Exception e) {
			message = e.toString();
			for(StackTraceElement se : e.getStackTrace()) {
				stacktrace.add(se.toString());
			}
		}
	}
	private static class ContentStoreResponse extends DummyTask {
		private String message = "OK";
	}
	
	private Gson gson = new Gson();

	/**
	 * Retrieves next task.
	 * 
	 * <code>GET</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		response.setContentType("text/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			Object o = ProxyRequestFilterSingleton.getInstance().nextTask();
			if(o == null) {
				o = new DummyTask();
			}
			
			out.println(gson.toJson(o));
		}
		catch(Exception e) {
			out.println(gson.toJson(new ExceptionTask(e)));
		}
		finally {			
			out.close();
		}
	}

	/**
	 * Handles the HTTP
	 * <code>POST</code> method.
	 *
	 * @param request servlet request
	 * @param response servlet response
	 * @throws ServletException if a servlet-specific error occurs
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	protected void doPost(HttpServletRequest request, HttpServletResponse response)
			  throws ServletException, IOException {
		PrintWriter out = response.getWriter();
		try {
			String content = request.getParameter("content");
			String id = request.getParameter("id");

			ProxyRequestFilterSingleton.getInstance().passContent(id, content);
		
			out.println(gson.toJson(new ContentStoreResponse()));
		}
		catch(Exception e) {
			out.println(gson.toJson(new ExceptionTask(e)));
		}
		finally {			
			out.close();
		}		
	}

	/**
	 * Returns a short description of the servlet.
	 *
	 * @return a String containing servlet description
	 */
	@Override
	public String getServletInfo() {
		return "Short description";
	}
}
