	// -----------------------------------------------------------------------------------------
BrowserHarvester.Service =  {
	/**
	 * Commits operation to server
	 * Structure data:
	 * 	* content - html content from browser
	 *	* headers - modified/added HTTP header
	 *	* metadata - metadata
	 }
	 */
	commit : function(data) {
		BrowserHarvester.Monitor.stop();

		BrowserHarvester.Log.info(
			"Received commit request with data(html content length: " + data.content.length +
			") from content script for URL: " + data.url);
		BrowserHarvester.Log.debug("data: ", data);

		// selecting headers
		BrowserHarvester.Log.info("Looking for response headers..");
		for(var i = 0; i < BrowserHarvester.Responses.length; i++) {
			var r = BrowserHarvester.Responses[i];

			if(r.url == data.url) {
				data.headers = r.responseHeaders;
				BrowserHarvester.Log.info("Response headers found! # of lines: " + r.responseHeaders.length);
			}
		}

		BrowserHarvester.Log.info("Passing content to proxy..");

		$.ajax({
			url: BrowserHarvester.Config.URL_STORE,
			type: 'post',
			data : JSON.stringify({
				"content"	: data.content,
				"id"		: BrowserHarvester.CurrentTaskId,
				"headers"	: data.headers,
				"metadata"	: data.metadata
			}),

//				contentType : "application/x-www-form-urlencoded; charset=utf-8",
			contentType : "application/json; charset=utf-8",
			dataType: "json",

			success : function(response) {
				BrowserHarvester.Log.info("Content sucessfuly passed with proxy response: ", response);

				BrowserHarvester.Log.info("Tear down...");

				// close tabl, cleanup
				chrome.tabs.remove(BrowserHarvester.CurrentRenderingTab.id);

				// Reset
				BrowserHarvester.CurrentTaskScript = null;
				BrowserHarvester.CurrentTaskId = null;
				BrowserHarvester.CurrentTaskScriptInjected = false;
				BrowserHarvester.CurrentRenderingTab = null;
				BrowserHarvester.CurrentTaskUrl = null;
				BrowserHarvester.Responses = [];

				BrowserHarvester.Service.poll();
			},
			error: function(xhr, text, err) {
				BrowserHarvester.Log.error("Error while passing content. Fix the problem and reload extension.", [xhr, text, err]);
				// TODO reset
			}
		});

	},

	cancel : function(message) {
		BrowserHarvester.Monitor.stop();

		BrowserHarvester.Log.info("Canceling task due to " + message);

		$.ajax({
			url: BrowserHarvester.Config.URL_STORE,
			type: 'post',
			data : JSON.stringify({
				"content"	: "canceled due: " + message,
				"id"		: BrowserHarvester.CurrentTaskId,
				"headers"	: [],
				"metadata"	: {}
			}),

//				contentType : "application/x-www-form-urlencoded; charset=utf-8",
			contentType : "application/json; charset=utf-8",
			dataType: "json",

			success : function(response) {
				BrowserHarvester.Log.info("Request successfully canceled: ", response);

				BrowserHarvester.Log.info("Tear down...");

				// close tabl, cleanup
				chrome.tabs.remove(BrowserHarvester.CurrentRenderingTab.id);

				// Reset
				BrowserHarvester.CurrentTaskScript = null;
				BrowserHarvester.CurrentTaskId = null;
				BrowserHarvester.CurrentTaskScriptInjected = false;
				BrowserHarvester.CurrentRenderingTab = null;
				BrowserHarvester.CurrentTaskUrl = null;
				BrowserHarvester.Responses = [];

				BrowserHarvester.Service.poll();
			},
			error: function(xhr, text, err) {
				BrowserHarvester.Log.error("Error while passing content. Fix the problem and reload extension.", [xhr, text, err]);
				// TODO reset
			}
		});
	},

	// Poll implementation
	// Spawns request to fetch rendering task form  server		
	poll : function() {
		BrowserHarvester.Log.info("Listening for new task..");				

		$.ajax({
			url : BrowserHarvester.Config.URL_GET,
			contentType: 'application/json; charset=utf-8',
			success : function(task) {
				BrowserHarvester.Log.debug("Poll task", task);

				if(task.valid) {
					BrowserHarvester.Log.info("New task fetched(ID: " + task.id +
						"; script-lenght: " + task.rule.clientScript.length +
						") acquired for url: " + task.url);
					// task is valid
					// store script which would be executed when page is fully rendered
					BrowserHarvester.CurrentTaskScript = task.rule.clientScript;
					BrowserHarvester.CurrentTaskId = task.id;
					BrowserHarvester.CurrentTaskUrl = task.url;

					BrowserHarvester.Monitor.start();

					// open tab with url
					var tabId = chrome.tabs.create({
						url : task.url
					}, function(tab) {
						BrowserHarvester.CurrentRenderingTab = tab;
					});
				}
				else {
					setTimeout(BrowserHarvester.Service.poll, 1000);
				}
			},
			error: function(xhr, text, err) {
				BrowserHarvester.Log.error("Error while polling request", [xhr, text, err]);
				setTimeout(BrowserHarvester.Service.poll, 10000);
			}
		});			
	}
};

BrowserHarvester.TaskScriptQueue = {};
BrowserHarvester.CurrentTaskScript = null;
BrowserHarvester.CurrentTaskId = null;
BrowserHarvester.CurrentRenderingTab = null;
BrowserHarvester.CurrentTaskScriptInjected= false;
BrowserHarvester.CurrentTaskUrl = null;
BrowserHarvester.Responses= [];

BrowserHarvester.RetryCount = 0;
BrowserHarvester.TimeoutMonitorId = null;

BrowserHarvester.Monitor = {
	start : function() {
		var ms = BrowserHarvester.Config.PROCESSING_TIMEOUT;
		BrowserHarvester.Log.info("Setting timeout for processing request: " + ms + " ms");

		setTimeout(BrowserHarvester.Monitor.retry, ms);
	},
	retry : function() {
		BrowserHarvester.Log.info("Retrying reqeust: " + (BrowserHarvester.RetryCount + 1) + " of " +
			BrowserHarvester.Config.PROCESSING_TIMEOUT_RETRY);

		if(BrowserHarvester.RetryCount++ < BrowserHarvester.Config.PROCESSING_TIMEOUT_RETRY) {
			BrowserHarvester.Log.info("Reloading/Retrying..");

			// reseting script injection state and reload tab
			BrowserHarvester.CurrentTaskScriptInjected = false;
			chrome.tabs.reload(BrowserHarvester.CurrentRenderingTab.id);

			setTimeout(BrowserHarvester.Monitor.retry, BrowserHarvester.Config.PROCESSING_TIMEOUT);
		}
		else {
			BrowserHarvester.Log.info("Cancelling..");
			// cancel
			BrowserHarvester.Service.cancel("Timeouts & retrying.");
		}

	},
	stop : function() {
		BrowserHarvester.Log.info("Clearing processing timeout.");

		clearTimeout(BrowserHarvester.TimeoutMonitorId);
		BrowserHarvester.RetryCount = 0;
		BrowserHarvester.TimeoutMonitorId = null;
	}
};

BrowserHarvester.start = function() {
	BrowserHarvester.Log.Filesystem.init();
	BrowserHarvester.Log.info("BrowserHarvester initializing...");


	BrowserHarvester.Log.info("Task fetch URL: " + BrowserHarvester.Config.URL_GET);
	BrowserHarvester.Log.info("Content store URL: " + BrowserHarvester.Config.URL_GET);

	// Listens to messages from content-script: event.urlloaded.js
	// Listens to messages from content script, which extracts and passes serialized DOM		
	chrome.extension.onMessage.addListener(function(data, sender, sendResponse) {
		switch(data.type) {
			case 'commit':
				BrowserHarvester.Service.commit(data);
				break;
			case 'exception':
				BrowserHarvester.Log.error("Content-script exception >> ", data.exception);
				// TODO reset
				break;
			case 'log':
				BrowserHarvester.Log.error("Content-script log >> " + data.message);
				// TODO reset
				break;					
		}
	});
	BrowserHarvester.Log.info("Registered onMessage(content-script messaging) listener.");

	// Register DOM ready listener
	// Here is our point to inject scripts to build desired pages for archive
	chrome.tabs.onUpdated.addListener(function(tabId, changeInfo, tab) {
		// Execute some script when the page is fully (DOM) ready
	    if (changeInfo.status == 'complete') {
	    	if(	BrowserHarvester.CurrentRenderingTab == null ||
	    		tab.id != BrowserHarvester.CurrentRenderingTab.id) {
	    		BrowserHarvester.Log.warn("Invalid scope(tab): " + tab.url);
	    		return;
	    	}

	    	// No need for annother recursive injection
	    	if(BrowserHarvester.CurrentTaskScriptInjected)	return;
	    	BrowserHarvester.CurrentTaskScriptInjected = true;

	    	BrowserHarvester.Log.info("DOM ready, injecting script.");
	    	BrowserHarvester.Log.info("Script:" + BrowserHarvester.CurrentTaskScript);

	        chrome.tabs.executeScript(tabId, {
	        	code:BrowserHarvester.CurrentTaskScript
	        }, function(obj) {
	        	BrowserHarvester.Log.info("Script executed.");
	        });
	    }
	});
	BrowserHarvester.Log.info("Registered onUpdated(Dom events) listener.");

	chrome.webRequest.onHeadersReceived.addListener(function(response) {
		BrowserHarvester.Responses.push(response);			
	}, {urls:["<all_urls>"]}, ["responseHeaders"]);
	BrowserHarvester.Log.info("Registered onHeadersReceived(Spying for reponse headers) listener.");		

	BrowserHarvester.Service.poll();
};

BrowserHarvester.start();










