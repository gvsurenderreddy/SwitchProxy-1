SwitchProxy
===========
SwitchProxy is a prototype of DOM manipulating proxy using browser to parse HTML, execute predefined scripts and finally returns built DOM as HTML to client.

Some usage scenarios:

* Enabling crawlers to be aware of Javascript page manipulations (ajax loaded content on page load, endless scroll ...)
* Content filtering (removing DOM blocks from pages, decorating ... )

Prerequisite
----------------

Project depends on:

* Java 5+
* Servlet container; tested only on Apache Tomcat 7 (http://tomcat.apache.org/download-70.cgi)
* Chromium/Chrome browser (http://www.chromium.org/Home)

Installation & configuration
--------------------------------------

After project is built, deploy war file.
For the installation of browser extension follow these steps: http://developer.chrome.com/extensions/getstarted.html#unpacked. Extension is located inside 'browser/Extension' directory. Inside config.js set URL_GET and URL_SET to point correctly to previously deployed webapp. If the extension is already loaded, you'll need to reload it to refresh configuration.

Next, run the proxy from admin interface: http://host:port/SwitchProxy/admin.
NOTE: proxy port is currently hardcoded to 8088

Proxy is now running.

NOTE: every time container is restarted, proxy and ruleset must be loaded manualy.

Rules
--------

If we test installation by using proxy, all requests pass through. To redirect requests to browser to do the rendering we need rules which matches request's URL and pass it to browser.

From the admin interface click Ruleset link(/SwitchProxy/admin/ruleset.jsp).

Rule is composed from URL regex pattern and script which is executed when browser loads the page.

Most basic script would be:

  BrowserHarvester.ContentScript.commit();

Which tells the browser when it loads the page to pack the content and pass it back to client via proxy.

After the rule is created, we can test installation by configuring client to use our proxy. If everything work, after the client issues the request, in browser window a new tab should be created and after page is loaded the tab should close(depends on the script browser is given to execute) and the client should receive processed content.

Troubleshooting:
-----------------------

If proxy stops working, try following steps:

1. Look inside Active task section (/SwitchProxy/admin/active-tasks.jsp) and cancel any active tasks. If client is still connected it will get "Canceled!!" text.
2. Reload extensions inside chrome://extensions.

TODO:
---------

List is large and growing. Just a few items listed below:

* Proxy implementation which will handle HTTPS protocol (currently just pass through)
* Loading ruleset at startup
* Starting proxy at startup
* Additional rule conditions(currently only URL matching)
* Exposing Chrome network apis to rule scripts
* Employing different rendering implementaitons (PhantomJS, Gecko)
* ...

