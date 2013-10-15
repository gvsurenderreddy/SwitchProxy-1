package net.sourceforge.jhttpp2;

/* Written and copyright 2001-2011 Benjamin Kohl.
 * Distributed under the GNU General Public License; see the README file.
 * This code comes with NO WARRANTY.
 * More Information and documentation: HTTP://jhttp2.sourceforge.net/
 */

import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStreamWriter;
import java.net.BindException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Date;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Jhttpp2Server implements Runnable {

	private final String VERSION = "0.4.80b1";
	private final String HTTP_VERSION = "HTTP/1.1";

	public String MAIN_LOGFILE = "server.log";
	public String DATA_FILE = "server.data";
	public String SERVER_PROPERTIES_FILE = "server.properties";

	private String httpUserAgent = "Mozilla/4.0 (compatible; MSIE 5.0; WindowsNT 5.1)";
	private ServerSocket listen;
	private BufferedWriter logFile;
	private BufferedWriter accessLogFile;
	private Properties serverproperties = null;

	private volatile long bytesread;
	private volatile long byteswritten;
	private volatile int numconnections;

	private boolean enable_cookies_by_default = true;
	private WildcardDictionary dic = new WildcardDictionary();
	private Vector<OnURLAction> urlactions = new Vector<OnURLAction>();

	public final int DEFAULT_SERVER_PORT = 8088;
	public final String WEB_CONFIG_FILE = "admin/jp2-config";

	public int port = DEFAULT_SERVER_PORT;
	public InetAddress proxy;
	public int proxy_port = 0;

	public long config_auth = 0;
	public long config_session_id = 0;
	public String config_user = "";
	public String config_password = "";

	public boolean fatalError;
	private String errorMessage;
	private boolean serverRunning = false;

	public boolean useProxy = false;
	public boolean block_urls = false;
	public boolean filter_http = false;
	public boolean debug = false;
	public boolean log_access = true;
	public String log_access_filename = "access.log";
	public boolean webconfig = false;
	public boolean www_server = true;

	public void init() {
		// create new BufferedWriter instance for logging to file
		try {
//			logFile = new BufferedWriter(new FileWriter(MAIN_LOGFILE, true));
			logFile = new BufferedWriter(new OutputStreamWriter(System.out));
		} catch (Exception e_logfile) {
			setErrorMsg("Unable to open the main log file.");
			if (logFile == null)
				setErrorMsg("jHTTPp2 need write permission for the file "
						+ MAIN_LOGFILE);
			errorMessage += " " + e_logfile.getMessage();
		}
		writeLog("jHTTPp2 proxy server startup...");

		// restore settings from file. If this fails, default settings will be used
		restoreSettings();
		
		// create now server socket
		try {
			listen = new ServerSocket(port);
		} catch (BindException e_bind_socket) {
			setErrorMsg("The socket " + port
					+ " is already in use (Another jHTTPp2 proxy running?) "
					+ e_bind_socket.getMessage());
		} catch (IOException e_io_socket) {
			setErrorMsg("IO Exception occured while creating server socket on port "
					+ port + ". " + e_io_socket.getMessage());
		}

		if (fatalError) {
			writeLog(errorMessage);
			return;
		}

	}

	public Jhttpp2Server() {
	}

	public Jhttpp2Server(boolean b) {
		System.out
				.println("jHTTPp2 HTTP Proxy Server Version "
						+ getServerVersion()
						+ "\r\n"
						+ "Copyright (c) 2001-2011 Benjamin Kohl\r\n"
						+ "This software comes with ABSOLUTELY NO WARRANTY OF ANY KIND.\r\n"
						+ "http://jhttp2.sourceforge.net/");
		init();
	}

	/**
	 * calls init(), sets up the server port and starts for each connection new
	 * Jhttpp2Connection
	 */
	void serve() {
		
		serverRunning = true;
		writeLog("Server running on port " + this.port);
		try {
			while (serverRunning) {
				Socket client = listen.accept();
				new Jhttpp2HTTPSession(this, client);
			}
		} catch (Exception e) {
			e.printStackTrace();
			writeLog("Exception in Jhttpp2Server.serve(): " + e.toString());
		}
	}

	public void run() {
		serve();
	}

	public void setErrorMsg(String a) {
		fatalError = true;
		errorMessage = a;
	}

	/**
	 * Tests what method is used with the reqest
	 * 
	 * @return -1 if the server doesn't support the method
	 */
	public int getHttpMethod(String d) {
		if (startsWith(d, "GET") || startsWith(d, "HEAD"))
			return 0;
		if (startsWith(d, "POST") || startsWith(d, "PUT"))
			return 1;
		if (startsWith(d, "CONNECT"))
			return 2;
		if (startsWith(d, "OPTIONS"))
			return 3;

		return -1;/*
				 * No match...
				 * 
				 * Following methods are not implemented: ||
				 * startsWith(d,"TRACE")
				 */
	}

	public boolean startsWith(String a, String what) {
		int l = what.length();
		int l2 = a.length();
		return l2 >= l ? a.substring(0, l).equals(what) : false;
	}

	/**
	 * @return the Server response-header field
	 */
	public String getServerIdentification() {
		return "jHTTPp2/" + getServerVersion();
	}

	public String getServerVersion() {
		return VERSION;
	}
	
	/**
	 * saves all settings with a ObjectOutputStream into a file
	 * 
	 * @since 0.2.10
	 */
	public void saveSettings() {
		
		Boolean propertiesFileSaved = false;
		Boolean objectFileSaved = false;
		
		if (serverproperties == null)
			return;
		
		serverproperties.setProperty("server.http-proxy",
				new Boolean(useProxy).toString());
		serverproperties.setProperty("server.http-proxy.hostname",
				proxy.getHostAddress());
		serverproperties.setProperty("server.http-proxy.port", new Integer(
				proxy_port).toString());
		serverproperties.setProperty("server.filter.http", new Boolean(
				filter_http).toString());
		serverproperties.setProperty("server.filter.url", new Boolean(
				block_urls).toString());
		serverproperties.setProperty("server.filter.http.useragent",
				httpUserAgent);
		serverproperties.setProperty("server.enable-cookies-by-default",
				new Boolean(enable_cookies_by_default).toString());
		serverproperties.setProperty("server.debug-logging",
				new Boolean(debug).toString());
		serverproperties.setProperty("server.port",
				new Integer(port).toString());
		serverproperties.setProperty("server.access.log", new Boolean(
				log_access).toString());
		serverproperties.setProperty("server.access.log.filename",
				log_access_filename);
		serverproperties.setProperty("server.webconfig",
				new Boolean(webconfig).toString());
		serverproperties.setProperty("server.www",
				new Boolean(www_server).toString());
		serverproperties.setProperty("server.webconfig.username", config_user);
		serverproperties.setProperty("server.webconfig.password",
				config_password);
		
		try {
			serverproperties
					.store(new FileOutputStream(SERVER_PROPERTIES_FILE),
							"Jhttpp2Server main properties. Look at the README file for further documentation.");
			propertiesFileSaved = true;
		} catch (IOException IOExceptProperties) {
			writeLog("storeServerProperties(): " + IOExceptProperties.getMessage());
		}
		
		try {
			ObjectOutputStream file = new ObjectOutputStream(new FileOutputStream(
				DATA_FILE));
			file.writeObject(dic);
			file.writeObject(urlactions);
			file.close();
			objectFileSaved = true;
		}
		catch(IOException IOExceptObjectStream){
			writeLog("storeServerProperties(): " + IOExceptObjectStream.getMessage());
		}
		
		if (objectFileSaved && propertiesFileSaved)
			writeLog("Configuration saved successfully");
		else
			writeLog("Failure during saving server properties or object stream");
		
	}

	/**
	 * restores all Jhttpp2 options from the configuration file
	 * 
	 * @since 0.2.10
	 */
	public void restoreSettings()
	{
		Boolean propertiesFileLoaded = false;
		Boolean objectFileLoaded = false;
		
		if (serverproperties == null) {
			serverproperties = new Properties();
			try {
				serverproperties.load(new DataInputStream(new FileInputStream(
						SERVER_PROPERTIES_FILE)));
				propertiesFileLoaded = true;
			} catch (IOException e) {
				writeLog("getServerProperties(): " + e.getMessage());
			
			}
		}
		
		useProxy = new Boolean(serverproperties.getProperty(
				"server.http-proxy", "false")).booleanValue();
		try {
			proxy = InetAddress.getByName(serverproperties.getProperty(
					"server.http-proxy.hostname", "127.0.0.1"));
		} catch (UnknownHostException e) {
		}
		proxy_port = new Integer(serverproperties.getProperty(
				"server.http-proxy.port", "8080")).intValue();
		block_urls = new Boolean(serverproperties.getProperty(
				"server.filter.url", "false")).booleanValue();
		httpUserAgent = serverproperties.getProperty(
				"server.filter.http.useragent",
				"Mozilla/4.0 (compatible; MSIE 4.0; WindowsNT 5.0)");
		filter_http = new Boolean(serverproperties.getProperty(
				"server.filter.http", "false")).booleanValue();
		enable_cookies_by_default = new Boolean(serverproperties.getProperty(
				"server.enable-cookies-by-default", "true")).booleanValue();
		debug = new Boolean(serverproperties.getProperty(
				"server.debug-logging", "false")).booleanValue();
		port = new Integer(serverproperties.getProperty("server.port", "8088"))
				.intValue();
		log_access = new Boolean(serverproperties.getProperty(
				"server.access.log", "true")).booleanValue();
		if(false) {
		log_access_filename = serverproperties.getProperty(
				"server.access.log.filename", "access.log");
		}
		webconfig = new Boolean(serverproperties.getProperty(
				"server.webconfig", "false")).booleanValue();
		www_server = new Boolean(serverproperties.getProperty("server.www",
				"false")).booleanValue();
		config_user = serverproperties.getProperty("server.webconfig.username",
				"admin");
		// create random password with 16 characters as default value for the web configuration module 
		config_password = serverproperties.getProperty(
				"server.webconfig.password", Jhttpp2Utils.randomString(16));
		
		try {

			accessLogFile = new BufferedWriter(new FileWriter(
					log_access_filename, true));
			// Restore the WildcardDioctionary and the URLActions with the
			// ObjectInputStream (settings.dat)...
			ObjectInputStream objInputStream;
			File file = new File(DATA_FILE);
			if (file.exists()) {
				objInputStream = new ObjectInputStream(new FileInputStream(file));
				dic = (WildcardDictionary) objInputStream.readObject();
				urlactions = (Vector<OnURLAction>) objInputStream.readObject();
				objInputStream.close();
				// loading successful 
				objectFileLoaded = true;
			}
			
		} catch (Exception exceptObjectInput) {
			setErrorMsg("restoreSettings(): " + exceptObjectInput.getMessage());
		}
		
		if (!objectFileLoaded || !propertiesFileLoaded) {
			writeLog("Error occured during configuration read, trying to save configuration...");
			saveSettings();
		}	
		
	}

	/**
	 * @return the HTTP version used by jHTTPp2
	 */
	public String getHttpVersion() {
		return HTTP_VERSION;
	}

	/**
	 * the User-Agent header field
	 * 
	 * @since 0.2.17
	 * @return User-Agent String
	 */
	public String getUserAgent() {
		return httpUserAgent;
	}

	public void setUserAgent(String ua) {
		httpUserAgent = ua;
	}

	/**
	 * writes into the server log file and adds a new line
	 * 
	 * @since 0.2.21
	 */
	public void writeLog(String s) {
		writeLog(s, true);
	}

	/**
	 * writes to the server log file
	 * 
	 * @since 0.2.21
	 */
	public void writeLog(String s, boolean new_line) {
		try {
			s = new Date().toString() + " " + s;
			logFile.write(s, 0, s.length());
			if (new_line)
				logFile.newLine();
			logFile.flush();
			if (debug)
				System.out.println(s);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void closeLog() {
		try {
			writeLog("Server shutdown.");
			logFile.flush();
			logFile.close();
			accessLogFile.close();
		} catch (Exception e) {
		}
	}

	public void addBytesRead(long read) {
		bytesread += read;
	}

	/**
	 * Functions for the jHTTPp2 statistics: How many connections Bytes
	 * read/written
	 * 
	 * @since 0.3.0
	 */
	public void addBytesWritten(int written) {
		byteswritten += written;
	}

	public int getServerConnections() {
		return numconnections;
	}

	public long getBytesRead() {
		return bytesread;
	}

	public long getBytesWritten() {
		return byteswritten;
	}

	public void increaseNumConnections() {
		numconnections++;
	}

	public void decreaseNumConnections() {
		numconnections--;
	}

	public void AuthenticateUser(String u, String p) {
		if (config_user.equals(u) && config_password.equals(p)) {
			config_auth = 1;
		} else
			config_auth = 0;
	}

	public String getGMTString() {
		return new Date().toString();
	}

	public Jhttpp2URLMatch findMatch(String url) {
		return (Jhttpp2URLMatch) dic.get(url);
	}

	public WildcardDictionary getWildcardDictionary() {
		return dic;
	}

	public Vector<OnURLAction> getURLActions() {
		return urlactions;
	}

	public boolean enableCookiesByDefault() {
		return this.enable_cookies_by_default;
	}

	public void enableCookiesByDefault(boolean a) {
		enable_cookies_by_default = a;
	}

	public void resetStat() {
		bytesread = 0;
		byteswritten = 0;
	}

	/**
	 * @since 0.4.10a
	 */
	public void logAccess(String s) {
		try {
			accessLogFile.write("[" + new Date().toString() + "] " + s
					+ "\r\n");
			accessLogFile.flush();
		} catch (Exception e) {
			writeLog("Jhttpp2Server.access(String): " + e.getMessage());
		}
	}
	
	public String getErrorMessage() {
		return errorMessage;
	}

	public void shutdownServer() {
		closeLog();
		System.exit(0);
	}
	
	public void stop() {
		System.out.println("Stoping proxy!");
		
		closeLog();
		serverRunning = false;
		try {
			listen.close();
		}
		catch (IOException ex) {
			Logger.getLogger(Jhttpp2Server.class.getName()).log(Level.SEVERE, null, ex);
		}
	}

}