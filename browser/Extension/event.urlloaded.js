/**
 * Script which is injected into DOM of harvesteing page to provide 
 * utility function.
 */

 /**
  * Pass serialized DOM to extension, which then send content to Proxy server
  */
var BrowserHarvester = {
	ContentScript : {
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
			chrome.extension.sendMessage({
				type 	: 'log',
				message	: message
			});
		},

		commit : function(options) {
			try {
				BrowserHarvester.ContentScript.log("Starting commit..");

				BrowserHarvester.ContentScript.log("Packing(DOM serialization)..");
				var data = {
					type 		: 'commit',
					url 		: location.href,
					content		: BrowserHarvester.ContentScript.serializeDOM(),
					headers		: [],
					metadata	: {}
				}

				if(options != null) {
					// Post process Serialized DOM
					if( (typeof options.processHTML === 'function') ) {
						BrowserHarvester.ContentScript.log("Executin serialization Post-process function..");
						data.content = options.processHTML(data.content);
					}
					// todo metadata
					if(options.metadata != null) {
						BrowserHarvester.ContentScript.log("Appending meta data.");
						//append
						data.metadata = options.metadata;
					}
				}

				BrowserHarvester.ContentScript.log("Sending message..");

				chrome.extension.sendMessage(data);
			}
			catch(e) {
				chrome.extension.sendMessage({
					type 		: 'exception',
					exception	: e.toString()
				});
			}
		}
	}
};
