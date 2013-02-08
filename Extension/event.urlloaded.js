function commit() {
	var content = document.getElementsByTagName('html')[0].innerHTML;
	chrome.extension.sendMessage(content);
}


