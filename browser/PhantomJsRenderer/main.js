var Page = require('webpage');

var PhantomRenderer = {
	Config : {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer-interface',
		POLL_INTERVAL : 1000
	},

	Net : {
		next : function() {
			var p = Page.create();

			p.open(PhantomRenderer.Config.URL_GET, function(status) {
				var task = JSON.parse(p.plainText);

				if(task.valid) {

				}
				else {
					setTimeout(PhantomRenderer.Net.next, PhantomRenderer.Config.POLL_INTERVAL);
				}
			});
		}
	}

};


console.log(PhantomRenderer.Config.URL_GET);
PhantomRenderer.Net.next();

