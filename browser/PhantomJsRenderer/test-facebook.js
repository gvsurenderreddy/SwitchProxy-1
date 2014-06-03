var page = require('webpage').create();

page.open('https://www.facebook.com/arstechnica', function(status) {
	console.log(status);

	console.log(page.evaluate(function() { return document.documentElement.outerHTML; }));

	phantm.exit();
});

// PhantomRendererAPI.Task.page.evaluate(function() { return document.documentElement.outerHTML; });


{
	pageLoaded : function() {
		PhantomRendererAPI.commit({
			processHTML : function(html) {
				return "<!DOCTYPE html>\n" + html;
			}
		});
	},
	onRequest : function(requestData, netwrkRequest) {
		console.log("HELLO from onRequest");
	}
}

---------------------

{
	pageLoaded : function() {
		PhantomRendererAPI.commit({
			processHTML : function(html) {
				return "<!DOCTYPE html>\n" + html;
			}
		});
	},
	onRequest : function(requestData, networkRequest) {
		console.log(">>>>>>>>HELLO from onRequest --- " + requestData.id);
	}
}

---------------------------
http://www.facebook.com/NarodnaUniverzitetnaKnjiznica

{
	injectJquery : true,

	onPageLoaded  : function() {
		var scroll = function() {
			if(
				! $('h3.uiHeaderTitle').filter(function() {
					return this.innerHTML == 'Founded';
				} )	.parents('.fbTimelineTimePeriod')
					.hasClass('fbTimelineTimePeriodUnexpanded')
				) {
				
				PhantomRendererAPI.log("Last time period expanded - Founded");
			
				PhantomRendererAPI.commit({
					processHTML : function(html) {
						return "<!DOCTYPE html>\n" + html;
					}
				});
				PhantomRendererAPI.log("Done!");			
			}
			else {
				PhantomRendererAPI.log("Scrolling down.");

				var rndOffset = Math.round(Math.random() * 100);
				var offset = document.height - window.innerHeight;
				window.scrollTo(0, offset - rndOffset );
				
				setTimeout(scroll, 500);
			}   
		}
		 
		scroll();
 	},
	onResourceRequested : function(requestData, networkRequest) {
		if(requestData.id == 1) {
			networkRequest.changeUrl(requestData.url.replace('http:', 'https:'));
		}
	}

 }