/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

import java.util.regex.Pattern;

/**
 *
 * @author mitja
 */
public class UrlMatchRule {
	private Pattern urlPattern;
	private String clientScript;

	public UrlMatchRule(String urlPattern, String clientScript) {
		this.urlPattern = Pattern.compile(urlPattern);
		this.clientScript = clientScript;
	}

	public String getClientScript() {
		return clientScript;
	}

	public Pattern getUrlPattern() {
		return urlPattern;
	}
	
	public boolean isMatched(String url) {
		return urlPattern.matcher(url).matches();
	}
	
}
