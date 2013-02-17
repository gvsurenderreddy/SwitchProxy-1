/**
 * Script which is injected into DOM of harvesteing page to provide 
 * utility function.
 */

 /**
  * Pass serialized DOM to extension, which then send content to Proxy server
  */
var BrowserHarvester = {
	ClientScript : {
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
		commit : function(options) {
//			var content = document.getElementsByTagName('html')[0].innerHTML;
			var data = {
				content : BrowserHarvester.ClientScript.serializeDOM(),
				headers : [],
				metadata : {}
			}

			if(options != null) {
				// Post process Serialized DOM
				if( (typeof options.processHTML === 'function') ) {
					data.content = options.processHTML(data.content);
				}
				// todo metadata
				if(options.metadata != null) {
					//append
					data.metadata = options.metadata;
				}
			}

			chrome.extension.sendMessage(data);			
		}
	}
};
