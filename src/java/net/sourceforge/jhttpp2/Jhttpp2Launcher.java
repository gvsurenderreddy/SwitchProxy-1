package net.sourceforge.jhttpp2;

/* Written and copyright 2001-2011 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 */


/**
 * Title:        jHTTPp2: Java HTTP Filter Proxy
 * Description: starts proxy server
 * Copyright:    Copyright (c) 2001-2011
 *
 * @author Benjamin Kohl
 */

public class Jhttpp2Launcher {

  static Jhttpp2Server server;

  public static void main(String[] args)
  {
		server = new Jhttpp2Server(true);
    	if (server.fatalError) {
    		System.out.println("Error: " +  server.getErrorMessage());
		}
    	else {
    		new Thread(server).start();
    	   	System.out.println("Running on port " + server.port);
    	}
  }
}