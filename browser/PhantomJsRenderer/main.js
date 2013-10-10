var Page = require('webpage');
var system = require('system');

require('./date.js');
var R = require('./base64v1_0.js');

var PhantomRenderer = {
	Config : {
		URL_GET : 'http://localhost:9080/SwitchProxy/renderer-interface',
		URL_STORE : 'http://localhost:9080/SwitchProxy/renderer-interface',
		POLL_INTERVAL : 1000,

		PROCESSING_TIMEOUT : 10*60*1000,
		PROCESSING_TIMEOUT_RETRY : 3
	},

	Log : {
		_date : function () {
			return new Date().toString('yyyy-MM-dd HH:mm:ss');
		},
		i : function(msg) {
			console.log(PhantomRenderer.Log._date() + "|INFO: " + msg);
		},
		d : function(msg) {
			console.log(PhantomRenderer.Log._date() + "|DEBUG: " + msg);
		},
		e : function(msg) {
			console.log(PhantomRenderer.Log._date() + "|ERROR: " + msg);
		},
		w : function(msg) {
			console.log(PhantomRenderer.Log._date() + "|WARN: " + msg);
		}		
	},

	Task : {
		current : null,
		headers : [],
		page : null,
		url : null,

		retry : 0,
		retryTimeoutId : null
	},

	Net : {
		next : function() {
			var p = Page.create();

			p.open(PhantomRenderer.Config.URL_GET, function(status) {
				var rawContent = p.plainText;
				p.close();
			
				if(status === 'success') {
					var task = JSON.parse(rawContent);

					if(task.valid) {
						PhantomRenderer.Log.i("New task: " + task.id + "; URL: " + task.url);

						PhantomRenderer.Task.current = task;

						PhantomRenderer.Renderer.render(task);
						
						return;
					}
				}

				setTimeout(PhantomRenderer.Net.next, PhantomRenderer.Config.POLL_INTERVAL);
			});
		},
		store : function(data) {
			task = PhantomRenderer.Task.current;
			PhantomRenderer.Log.i("Preparing: " + task.id + "; URL: " + task.url);

			var req = Page.create();
			req.customHeaders = {
				'Content-Type' : 'application/json; charset=utf-8',
				'Content-Transfer-Encoding' : 'base64'
			};

			PhantomRenderer.Log.i("Encoding to JSON");
			var jsonData = JSON.stringify({
				"content"	: data.content,
				"id"		: PhantomRenderer.Task.current.id,
				"headers"	: data.headers,
				"metadata"	: data.metadata
			});

			PhantomRenderer.Log.i("Encoding to base64");
			var base64Data = phantom.B64.encode(jsonData);

			PhantomRenderer.Log.i("Storing..");

			req.open(PhantomRenderer.Config.URL_GET, 'POST', base64Data, function(status) {
				PhantomRenderer.Log.i("Content stored: " + status);

				req.close();

				PhantomRenderer.Task.current = null;
				PhantomRenderer.Task.headers = [];

				PhantomRenderer.Log.i("Listening for new task..");
				PhantomRenderer.Net.next();
			});
		},
		cancel : function(reason) {
			var task = PhantomRenderer.Task.current;

			PhantomRenderer.Log.i("Canceling: " + task.id + "; URL: " + task.url + ", due: " + reason);

			var req = Page.create();
			req.customHeaders = {
				'Content-Type' : 'application/json; charset=utf-8'
			};

			var jsonData = JSON.stringify({
				"content"	: "canceled due: " + reason,
				"id"		: task.id,
				"headers"	: [],
				"metadata"	: {}
			});

			req.open(PhantomRenderer.Config.URL_GET, 'POST', jsonData, function(status) {
				PhantomRenderer.Log.i("Task canceled: " + status);

				req.close();

				PhantomRenderer.Task.current = null;
				PhantomRenderer.Task.headers = [];

				PhantomRenderer.Log.i("Listening for new task..");
				PhantomRenderer.Net.next();
			});			
		}
	},

	Renderer : {
		callbackListener : function(data) {
			switch(data.type) {
				case 'api-log':
					PhantomRenderer.Log.i(data.content);
					break;				
				case 'log':
					PhantomRenderer.Log.i("FROM PAGE: " + data.content);
					break;
				case 'commit':
					PhantomRenderer.Log.i("Received commit call.");

					PhantomRenderer.Monitor.stop();

					PhantomRenderer.Log.i("Content-Length: " + data.content.length);

					data.metadata = {};
					data.headers = PhantomRenderer.Task.headers;

					PhantomRenderer.Net.store(data);

					PhantomRenderer.Task.page.close();
					PhantomRenderer.Task.page = null;
					PhantomRenderer.Log.i("Page closed.");
					break;				
			}
		},
		errorListener : function(msg, trace) {
			PhantomRenderer.Log.e("Error occured: " + msg);

			PhantomRenderer.Task.page.close();
			PhantomRenderer.Task.page = null;
			PhantomRenderer.Log.i("Page closed.");

			// WHAT TO DO NOW:
			// * retry
			// * return error
			PhantomRenderer.Net.next();
		},
		responseListener : function(response) {
			PhantomRenderer.Log.i("Response from " + response.url);

			if(response.url == PhantomRenderer.Task.url) {
		    	PhantomRenderer.Task.headers = response.headers;
		    }
		},
		navigationListener : function(url, type, willNavigate, main) {
			PhantomRenderer.Log.d("Navigation request to " + url + "; type: " + type +
				", willNavigate: " + willNavigate + "; main: " + main);

			// Handling page redirection
			if(main) {
				PhantomRenderer.Task.url = url;
			}
		},

		render : function(task) {
			if(PhantomRenderer.Task.page != null) {
				PhantomRenderer.Task.page.close();
			}

			var page = Page.create();

			PhantomRenderer.Task.page = page;

			page.onCallback = PhantomRenderer.Renderer.callbackListener;
			page.onResourceReceived = PhantomRenderer.Renderer.responseListener;
			page.onNavigationRequested = PhantomRenderer.Renderer.navigationListener;

			PhantomRenderer.Log.i("Opening page at url: " + PhantomRenderer.Task.current.url);
			page.open(task.url, function(status) {
				PhantomRenderer.Log.i("Page loaded with status " + status);

				if(status === 'success') {
					PhantomRenderer.Log.i('Injecting API..');
					page.injectJs('./pageapi.js');

					PhantomRenderer.Log.i("Running task script..");

					PhantomRenderer.Monitor.start();

					page.evaluateJavaScript(task.rule.clientScript);
				}
			});
		}
	},

	Monitor : {
		start : function() {
			PhantomRenderer.Log.d("Setting timeout");

			PhantomRenderer.Task.retryTimeoutId = 
				setTimeout(PhantomRenderer.Monitor.retry, PhantomRenderer.Config.PROCESSING_TIMEOUT);
		},
		retry : function() {
			PhantomRenderer.Log.w("Timeout occured");

			PhantomRenderer.Task.page.close();
			PhantomRenderer.Task.page = null;
			PhantomRenderer.Log.i("Page closed.");
			
			if(++PhantomRenderer.Task.retry < PhantomRenderer.Config.PROCESSING_TIMEOUT_RETRY) {
				PhantomRenderer.Log.w("Retrying(" + PhantomRenderer.Task.retry + ")...");

				PhantomRenderer.Renderer.render(PhantomRenderer.Task.current);
			}
			else {
				PhantomRenderer.Monitor.stop();

				PhantomRenderer.Net.cancel("Max retries.");
			}
		},
		stop : function() {
			PhantomRenderer.Log.d("Clearing timeout");

			clearTimeout(PhantomRenderer.Task.retryTimeoutId);
			PhantomRenderer.Task.retry = 0;
			PhantomRenderer.Task.retryTimeoutId = null;
		}
	}
};


PhantomRenderer.Log.i("Listening for tasks...");
PhantomRenderer.Net.next();
