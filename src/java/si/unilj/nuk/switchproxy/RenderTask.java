/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

/**
 *
 * @author mitja
 */
public class RenderTask {
	
	private String url;
	private UrlMatchRule rule;

	public RenderTask(String url, UrlMatchRule rule) {
		this.url = url;
		this.rule = rule;
	}

	public UrlMatchRule getRule() {
		return rule;
	}

	public String getUrl() {
		return url;
	}
	
}
