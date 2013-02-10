/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.switchproxy;

/**
 *
 * @author mitja
 */
public class ProxyRequestFilterSingleton {
	private static ProxyRequestFilter instance;
	
	public static ProxyRequestFilter getInstance() {
		synchronized(instance) {
			if(instance == null) {
				instance = new ProxyRequestFilter();
				
				setup(instance);
			}
		}
		
		return instance;
	}	
	
	public static void setup(ProxyRequestFilter prf) {
		prf.getRuleSet().add(new UrlMatchRule("^http://www.twitter.com/[A-z]+$", 
				  "var currElCount = 0; function loadMoarAndMoar() { commit(); if(document.getElementsByTagName('*').length > currElCount) { currElCount = document.getElementsByTagName('*').length; window.scrollTo(0, document.height); } setTimeout(loadMoarAndMoar, 500); } setTimeout(loadMoarAndMoar, 500);"));
	}
}
