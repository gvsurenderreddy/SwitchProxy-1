/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.wpgproxy;

import com.wpg.proxy.HttpMessageResponse;
import java.util.Map;
import si.unilj.nuk.switchproxy.RenderTask;

/**
 *
 * @author mitja
 */
public class WpgProxyUtil {
	public static HttpMessageResponse createFromTask(RenderTask task) {
		HttpMessageResponse response = new HttpMessageResponse();
		byte[] cnt = task.getContent().getBytes();
		response.addToBody(cnt, cnt.length);
		response.setStartLine("HTTP/1.1 200 OK");
		response.setStatusCode(200);
		response.setReasonPhrase("OK");
	
		if(task.getHeaders().size() > 0) {
			for(Map.Entry<String, String> p : task.getHeaders().entrySet()) {
				String key = p.getKey();
				if(key.toLowerCase().equals("content-encoding") ||
					key.toLowerCase().equals("content-length")) {
					key = "X-ProxyRemove-" + key;
				}

				response.addHeader(key, p.getValue());
			}
		}
		else {
			response.addHeader("Content-Type", "text/html");
			response.addHeader("Transfer-Encoding", "chunked");		
		}
		
		return response;
	}
}
