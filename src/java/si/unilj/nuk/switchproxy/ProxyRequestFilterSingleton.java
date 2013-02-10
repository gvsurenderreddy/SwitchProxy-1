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
			}
		}
		
		return instance;
	}	
	
	
	public static void setup(ProxyRequestFilter prf) {
		
	}
}
