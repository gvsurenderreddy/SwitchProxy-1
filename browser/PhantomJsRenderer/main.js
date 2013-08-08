var Page = require('webpage');
var system = require('system');

var PhantomRenderer = {
	Config : {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer-interface',
		POLL_INTERVAL : 1000
	},

	Log : {
		i : function(msg) {
			console.log(msg);
		}
	},

	Net : {
		next : function() {
			var p = Page.create();

			p.open(PhantomRenderer.Config.URL_GET, function(status) {
				var task = JSON.parse(p.plainText);

				if(task.valid) {
					PhantomRenderer.Log.i("New task: " + task.id + "; URL: " + task.url);
					PhantomRenderer.Renderer.render(task);
				}
				else {
					setTimeout(PhantomRenderer.Net.next, PhantomRenderer.Config.POLL_INTERVAL);
				}
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
					console.log(data.content);
					PhantomRenderer.Net.next();
					break;				
			}
		},
		errorListener : function(msg, trace) {
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


PhantomRenderer.Net.next();