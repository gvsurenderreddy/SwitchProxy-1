var BrowserHarvester = {
	Config: {
		URL_GET : 'http://localhost:8080/SwitchProxy/renderer/get-next.jsp',
		URL_STORE : 'http://localhost:8080/SwitchProxy/renderer/store.jsp'
	},

	Diagnostics : {
		log : function(message, obj) {
			alert(message, obj);
		}
	},

	Service : {},

	start : function() {

	}
};

// Poll implementation
$.ajax({
	url : BrowserHarvester.Config.URL_GET,
	contentType: 'application/json; charset=utf-8',
	success : function(task) {
		BrowserHarvester.Diagnostics.log(task);

		if(task.valid) {
			chrome.tabs.create({
				url : task.url
			});
		}
	},
	error: function(xhr, text, err) {
		alert([err]);
	}
});

