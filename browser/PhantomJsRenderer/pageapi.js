/**
 * Script which is injected into DOM of harvesteing page to provide 
 * utility function.
 */

 /**
  * Pass serialized DOM to extension, which then send content to Proxy server
  */
var PhantomRendererAPI = {
	apiLog : function(message) {
		window.callPhantom({ type: 'api-log', content : message });
	},

	serializeDOM : function() {
		var roots = document.childNodes;
		var buffer = "";
		for(var i = 0; i < roots.length; i++) {
			var r = roots[i];
			
			switch(r.nodeType) {
				case document.ELEMENT_NODE:
					if(r.nodeName == "HTML") {
						buffer += r.outerHTML;
					}
					break;
				case document.COMMENT_NODE:
					buffer += "<!--" + r.nodeValue + "-->";
					break;
				case document.DOCUMENT_TYPE_NODE:
					break;
			}

		}

		return buffer;
	},

	log : function(message) {
		window.callPhantom({ type: 'log', content : message });
	},

	commit : function(options) {
		try {
			PhantomRendererAPI.apiLog("Starting commit..");

			PhantomRendererAPI.apiLog("Packing(DOM serialization)..");
			var data = {
				type 		: 'commit',
				url 		: location.href,
				content		: PhantomRendererAPI.serializeDOM(),
				headers		: [],
				metadata	: {}
			}

			if(options != null) {
				// Post process Serialized DOM
				if( (typeof options.processHTML === 'function') ) {
					PhantomRendererAPI.apiLog("Executin serialization Post-process function..");
					data.content = options.processHTML(data.content);
				}
				// todo metadata
				if(options.metadata != null) {
					PhantomRendererAPI.apiLog("Appending meta data.");
					//append
					data.metadata = options.metadata;
				}
			}

			PhantomRendererAPI.apiLog("Sending message..");

			window.callPhantom(data);
		}
		catch(e) {
			PhantomRendererAPI.apiLog(e.toString());
		}
	},

	pausedExecution : null,
	resumeExecution : function() {
		PhantomRendererAPI.apiLog("Resumed execution(calling back) inside page context");

		if(PhantomRendererAPI.pausedExecution != null) {
			PhantomRendererAPI.pausedExecution();
			PhantomRendererAPI.pausedExecution = null;
		}
		else {
			PhantomRendererAPI.apiLog("ERR: callback pausedExecution not defined.");
		}
	},
	includeJavascript : function(url, callback) {
		PhantomRendererAPI.apiLog("Including in page [PAGE] --> SRV: " + url);

		PhantomRendererAPI.pausedExecution = callback;
		window.callPhantom({
			type : 'include-js',
			content : url
		});
	},
	injectJquery : function(callback) {
		PhantomRendererAPI.apiLog("Injecting jquery into page [PAGE] --> SRV");

		PhantomRendererAPI.pausedExecution = callback;
		window.callPhantom({
			type : 'inject-jquery'
		});
	}	

};
