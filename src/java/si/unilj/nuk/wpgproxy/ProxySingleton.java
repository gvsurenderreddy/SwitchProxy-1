/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package si.unilj.nuk.wpgproxy;

import com.wpg.proxy.Proxy;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author mitja
 */
public class ProxySingleton {
	
	private int port = 9999;
	
	private static ProxySingleton instance;
	
	private Proxy proxy;
	
	public static ProxySingleton getInstance() {
		synchronized(ProxySingleton.class) {
			if(instance == null) {
				instance = new ProxySingleton();
			}
		}
		
		return instance;
	}

	public ProxySingleton() {
	}

	public Proxy getProxy() {
		return proxy;
	}
	
	public void stop() {
		proxy.stop();
	}

	public void restart() {
		stop();
		start();
	}

	public void setPort(int port) {
		this.port = port;
	}
	
	public void start() {
		try {
			proxy = new Proxy(InetAddress.getByName("127.0.0.1"), port, 50);
			proxy.start();
		}
		catch (UnknownHostException ex) {
			Logger.getLogger(ProxySingleton.class.getName()).log(Level.SEVERE, null, ex);
		}
		catch(Exception e) {
			Logger.getLogger(ProxySingleton.class.getName()).log(Level.SEVERE, null, e);
		}
	}
	
}
