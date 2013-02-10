/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

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
	
	private String content;
	// HEADERS!!!

	public RenderTask(String url, UrlMatchRule rule) {
		this.url = url;
		this.rule = rule;
		
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
	}

	public String getContent() {
		return content;
	}

	@Override
	public String toString() {
		return "RenderTask(URL: " + url + ")";
	}
	
}
