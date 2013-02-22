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
				
				// content-encoding:
				// proxy sends content as plain text, thus any content (different as gzip)
				// encoding is undesirable
				// content-length:
				// this header bear the information about plain html from which is
				// base but not the only source for DOM construction and thus
				// serialization back would definitely generate string longer(rarely shorter)
				// than string length noted in this flag
				if(key.toLowerCase().equals("content-encoding") ||
					key.toLowerCase().equals("transfer-encoding") ||
					key.toLowerCase().equals("content-length")) {
					key = "X-ProxyRemove-" + key;
				}

				response.addHeader(key, p.getValue());
			}
			
			// proxy also returns content as chunked. If header is not set content length
			// is in first line
			if(!task.getHeaders().containsKey("Transfer-Encoding") && false) {
				response.addHeader("Transfer-Encoding", "chunked");
			}
			
			
		}
		else {
			response.addHeader("Content-Type", "text/html");
//			response.addHeader("Transfer-Encoding", "chunked");		
		}
		
		// new content length
		response.addHeader("Content-Length", String.valueOf(task.getContent().getBytes().length));
		
		return response;
	}
}
