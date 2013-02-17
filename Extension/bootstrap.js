var BrowserHarvester = {
	Config: {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer-interface',
		URL_STORE : 'http://localhost:8080/SwitchProxy/renderer-interface'
	},

	// -----------------------------------------------------------------------------------------
	Log:  {
		debug: function(message, obj) {
			BrowserHarvester.log(message, obj);
		},
		info: function(message, obj) {
			BrowserHarvester.log(message, obj);
		},
		warn: function(message, obj) {
			BrowserHarvester.log(message, obj);
		},
		error: function(message, obj) {
			BrowserHarvester.log(message, obj);
		}	
	},
	log : function(message, obj) {
		console.log(message);
		if(obj != null) console.log(obj);
	},

	// -----------------------------------------------------------------------------------------
	Service : {
		/**
		 * Commits operation to server
		 */
		commit : function(data) {
			$.ajax({
				url: BrowserHarvester.Config.URL_STORE,
				type: 'post',
				data : {
					"content"	: data.content,
					"id"		: BrowserHarvester.CurrentTaskId,
					"headers"	: data.headers,
					"metadata"	: data.metadata
				},
				contentType : "application/x-www-form-urlencoded; charset=utf-8",
				success : function(response) {
					// close tabl, cleanup
					chrome.tabs.remove(BrowserHarvester.CurrentRenderingTab.id);

					// Reset
					BrowserHarvester.CurrentTaskScript = null;
					BrowserHarvester.CurrentTaskId = null;
					BrowserHarvester.CurrentTaskScriptInjected = false;
					BrowserHarvester.CurrentRenderingTab = null;

					BrowserHarvester.Service.poll();
				},
				error: function(xhr, text, err) {
					alert([err]);
				}
			});

		},

		// Poll implementation
		// Spawns request to fetch rendering task form  server		
		poll : function() {
			$.ajax({
				url : BrowserHarvester.Config.URL_GET,
				contentType: 'application/json; charset=utf-8',
				success : function(task) {
					BrowserHarvester.log("Poll task", task);

					if(task.valid) {
						BrowserHarvester.log("New task(ID: " + task.id +
							"; scrit-lenght: " + task.rule.clientScript +
							") acquired for url: " + task.url);
						// task is valid
						// store script which would be executed when page is fully rendered
						BrowserHarvester.CurrentTaskScript = task.rule.clientScript;
						BrowserHarvester.CurrentTaskId = task.id;

						// open tab with url
						var tabId = chrome.tabs.create({
							url : task.url
						}, function(tab) {
							BrowserHarvester.CurrentRenderingTab = tab;
						});

						BrowserHarvester.Log.debug("TAB-ID", tabId);
					}
					else {
						// setTimeout
						// loop
						setTimeout(BrowserHarvester.Service.poll, 1000);
					}
				},
				error: function(xhr, text, err) {
					BrowserHarvester.Log.error("Error while polling request", [xhr, text, err]);
					setTimeout(BrowserHarvester.Service.poll, 10000);
				}
			});			
		}
	},

	TaskScriptQueue : {},
	CurrentTaskScript : null,
	CurrentTaskId : null,
	CurrentRenderingTab : null,
	CurrentTaskScriptInjected: false,

	start : function() {
		BrowserHarvester.log("Browser renderer started");

		// Listens to messages from content-script: event.urlloaded.js
		// Listens to messages from content script, which extracts and passes serialized DOM		
		chrome.extension.onMessage.addListener(function(data, sender, sendResponse) {
			BrowserHarvester.Service.commit(data);
		});

		// Register DOM ready listener
		// Here is our point to inject scripts to build desired pages for archive
		chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
			// Execute some script when the page is fully (DOM) ready
		    if (changeInfo.status == 'complete') {
		    	// No need for annother recursive injection
		    	if(BrowserHarvester.CurrentTaskScriptInjected)	return;

		    	BrowserHarvester.CurrentTaskScriptInjected = true;

		    	BrowserHarvester.Log.info("DOM ready, injecting script.");
		    	BrowserHarvester.Log.debug(BrowserHarvester.CurrentTaskScript);

		        chrome.tabs.executeScript(null, {
		        	code:BrowserHarvester.CurrentTaskScript
		        });
		    }
		});		

		BrowserHarvester.Service.poll();
	},
};

BrowserHarvester.start();










