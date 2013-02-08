var BrowserHarvester = {
	Config: {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer/get-twitter.jsp',
		URL_STORE : 'http://localhost:8080/SwitchProxy/renderer/store.jsp'
	},

	Diagnostics : {
		log : function(message, obj) {
//			alert(message + ": " + obj);
		}
	},

	Service : {
		commit : function() {}
	},

	TaskScriptQueue : {},
	CurrentTaskScript : null,

	start : function() {

	},
};

// Register DOM ready listener
// Here is our point to inject scripts to build desired pages for archive
chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
	// Execute some script when the page is fully (DOM) ready
    if (changeInfo.status == 'complete') {
        chrome.tabs.executeScript(null, {
        	code:BrowserHarvester.CurrentTaskScript
        });
    }
});


// Poll implementation
// Spawns request to fetch rendering task form  server
$.ajax({
	url : BrowserHarvester.Config.URL_GET,
	contentType: 'application/json; charset=utf-8',
	success : function(task) {
		BrowserHarvester.Diagnostics.log("Fetched task", task);

		if(task.valid) {
			// task is valid
			// store script which would be executed when page is fully rendered
			BrowserHarvester.CurrentTaskScript = task.script.content;

			// open tab with url
			var tabId = chrome.tabs.create({
				url : task.url
			});

			BrowserHarvester.Diagnostics.log("TAB-ID", tabId);
		}
	},
	error: function(xhr, text, err) {
		alert([err]);
	}
});

