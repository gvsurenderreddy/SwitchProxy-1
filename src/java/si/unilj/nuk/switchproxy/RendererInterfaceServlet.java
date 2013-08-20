/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.codec.binary.Base64InputStream;
import org.apache.log4j.Logger;

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
	
	private final static Logger log = Logger.getLogger(RendererInterfaceServlet.class);
	
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
		log.debug("Task poll");
		
		response.setContentType("application/json;charset=UTF-8");
		PrintWriter out = response.getWriter();
		try {
			Object o = ProxyRequestFilterSingleton.getInstance().nextTask();
			if(o == null) {
				o = new DummyTask();
			}
			out.println(gson.toJson(o));
		}
		catch(Exception e) {
			log.error("Error while getting new task", e);
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
			log.info("Commiting executed task");
			
			BufferedReader reader = null;
			if("base64".equalsIgnoreCase(request.getHeader("Content-Transfer-Encoding"))) {
				reader = new BufferedReader(
					new InputStreamReader(
						new Base64InputStream(request.getInputStream()), "UTF-8"));
			}
			else {
				reader = request.getReader();
			}

			JsonElement root = new JsonParser().parse(reader);
			JsonObject obj = root.getAsJsonObject();
			
			// json 
			String content = obj.get("content").getAsString();
			String id = obj.get("id").getAsString();
			
			log.info("Result parsed, Task id: " + id);
			
			HashMap<String, String> headers = new HashMap<String, String>();
			JsonArray headersArray = obj.get("headers").getAsJsonArray();
			log.info("Processing headers, count: " + headersArray.size());
			for(int i = 0; i < headersArray.size(); i++) {
				JsonObject pair = headersArray.get(i).getAsJsonObject();
				headers.put(pair.get("name").getAsString(), pair.get("value").getAsString());
			}

			log.info("Passing content.");
			ProxyRequestFilterSingleton.getInstance().passContent(id, content, headers);
		
			out.println(gson.toJson(new ContentStoreResponse()));
			log.info("Done");
		}
		catch(Exception e) {
			log.error("Error while comitting task", e);
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
