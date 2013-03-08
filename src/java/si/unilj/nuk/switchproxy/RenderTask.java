/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.util.Date;
import java.util.HashMap;
import java.util.UUID;

/**
 *
 * @author mitja
 */
public class RenderTask {
	
	/**
	 * Flag indicating renderer should proceed with task
	 */
	private final boolean valid = true;
	
	private String id;
	
	private String url;
	
	private UrlMatchRule rule;
	
	private boolean complete;
	
	private String content = null;
	private HashMap<String, String> headers = new HashMap<String, String>();
	
	private Date createDate;
	private Date commitDate;

	public RenderTask(String url, UrlMatchRule rule) {
		this.url = url;
		this.rule = rule;
		
		createDate = new Date();
		
		this.id = UUID.randomUUID().toString();
	}

	public UrlMatchRule getRule() {
		return rule;
	}

	public String getUrl() {
		return url;
	}

	public String getId() {
		return id;
	}

	public boolean isComplete() {
		return complete;
	}

	public void setContent(String content) {
		this.content = content;
		this.complete = true;
		commitDate = new Date();
	}

	public String getContent() {
		return content;
	}

	public HashMap<String, String> getHeaders() {
		return headers;
	}

	public void setHeaders(HashMap<String, String> headers) {
		this.headers = headers;
	}
	
	public void addHeaderLine(String key, String value) {
		headers.put(key, value);
	}

	public Date getCommitDate() {
		return commitDate;
	}

	public Date getCreateDate() {
		return createDate;
	}

	@Override
	public String toString() {
		return "RenderTask(URL: " + url + ")";
	}
	
}
