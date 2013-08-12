var Page = require('webpage');
var system = require('system');
require('./date.js');

var PhantomRenderer = {
	Config : {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer-interface',
		URL_STORE : 'http://localhost:8080/SwitchProxy/renderer-interface',
		POLL_INTERVAL : 1000
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
		url : null
	},

	Net : {
		next : function() {
			var p = Page.create();

			p.open(PhantomRenderer.Config.URL_GET, function(status) {
				var task = JSON.parse(p.plainText);

				if(task.valid) {
					PhantomRenderer.Log.i("New task: " + task.id + "; URL: " + task.url);

					PhantomRenderer.Task.current = task;

					PhantomRenderer.Renderer.render(task);
				}
				else {
					setTimeout(PhantomRenderer.Net.next, PhantomRenderer.Config.POLL_INTERVAL);
				}
			});
		},
		store : function(data) {
			task = PhantomRenderer.Task.current;
			PhantomRenderer.Log.i("Storing: " + task.id + "; URL: " + task.url);

			var req = Page.create();

			req.open(PhantomRenderer.Config.URL_GET, 'POST', JSON.stringify({
				"content"	: data.content,
				"id"		: PhantomRenderer.Task.current.id,
				"headers"	: data.headers,
				"metadata"	: data.metadata
			}), function(status) {
				PhantomRenderer.Log.i("Content stored: " + status);

				PhantomRenderer.Task.current = null;
				PhantomRenderer.Task.headers = [];

				PhantomRenderer.Log.i("Listening for new task");
				PhantomRenderer.Net.next();
			});
		}
	},

	Renderer : {
		callbackListener : function(data) {
			switch(data.type) {
				case 'log':
					PhantomRenderer.Log.i("FROM PAGE: " + data.content);
					break;
				case 'commit':
					PhantomRenderer.Log.i("Received commit call.");
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
			var page = Page.create();

			PhantomRenderer.Task.page = page;

			page.onCallback = PhantomRenderer.Renderer.callbackListener;
			page.onResourceReceived = PhantomRenderer.Renderer.responseListener;
			page.onNavigationRequested = PhantomRenderer.Renderer.navigationListener;

			PhantomRenderer.Log.i("Opening page at url: " + PhantomRenderer.Task.current.url);
			page.open(task.url, function(status) {
				PhantomRenderer.Log.i("Page loaded with status " + status);

				if(status === 'success') {
					PhantomRenderer.Log.i("Running task script..");
					page.evaluateJavaScript(task.rule.clientScript);
				}
			});
		}
	}
};


PhantomRenderer.Log.i("Listening for tasks...");
PhantomRenderer.Net.next();