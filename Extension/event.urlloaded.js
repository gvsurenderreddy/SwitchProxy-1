/**
 * Script which is injected into DOM of harvesteing page to provide 
 * utility function.
 */

 /**
  * Pass serialized DOM to extension, which then send content to Proxy server
  */
function commit() {
	var content = document.getElementsByTagName('html')[0].innerHTML;
	chrome.extension.sendMessage(content);
}


