var Page = require('webpage');
var system = require('system');

var PhantomRenderer = {
	Config : {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer-interface',
		URL_STORE : 'http://localhost:8080/SwitchProxy/renderer-interface',
		POLL_INTERVAL : 1000
	},

	Log : {
		i : function(msg) {
			console.log(msg);
		}
	},

	Task : {
		current : null
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
				PhantomRenderer.Log.i("Listening for new task");
				PhantomRenderer.Net.next();
			});
		}
	},

	Renderer : {
		callbackListener : function(data) {
			switch(data.type) {
				case 'log':
					PhantomRenderer.Log.i("From page context: " + data.content);
					break;
				case 'commit':
					PhantomRenderer.Log.i("Received commit call.");
					PhantomRenderer.Log.i("Content-Length: " + data.content.length);

					data.metadata = {};
					data.headers = [];

					PhantomRenderer.Net.store(data);
					break;				
			}
		},
		errorListener : function(msg, trace) {
			PhantomRenderer.Log.i("Error occured: " + msg);
			// retry
			PhantomRenderer.Net.next();
		},

		render : function(task) {
			var page = Page.create();

			page.onCallback = PhantomRenderer.Renderer.callbackListener;
			page.open(task.url, function(status) {
				if(status === 'success') {
					page.evaluateJavaScript(task.rule.clientScript);
				}
			});
		}
	}
};


PhantomRenderer.Log.i("Listening for tasks...");
PhantomRenderer.Net.next();