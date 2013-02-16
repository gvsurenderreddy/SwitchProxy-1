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
		synchronized(ProxyRequestFilterSingleton.class) {
			if(instance == null) {
				instance = new ProxyRequestFilter();
			}
		}
		
		return instance;
	}	
	
	public static void test_setup(ProxyRequestFilter prf) {
		UrlMatchRule rule = new UrlMatchRule("^http://www.twitter.com/[A-z]+$", 
				  "var currElCount = 0; function loadMoarAndMoar() { commit(); if(document.getElementsByTagName('*').length > currElCount) { currElCount = document.getElementsByTagName('*').length; window.scrollTo(0, document.height); } setTimeout(loadMoarAndMoar, 500); } setTimeout(loadMoarAndMoar, 500);");
		prf.getRuleSet().add(rule);

		
		// Testing
		RenderTask rt = new RenderTask("http://twitter.com/TinaMaze", rule);
		
		// TEST#1 add to active tasks
//		prf.getActiveTasks().put(rt.getId(), rt);
	
		// TEST#3: Empty task queue, dummy task
		
		// TEST#2 add to task queue
//		prf.getTaskQueue().add(rt);
	}
	
	public static void setup(ProxyRequestFilter prf) {
		
	}
}
