BrowserHarvester.Log = {



	debug: function(message, obj) {
//			BrowserHarvester.log(message, obj);
	},
	info: function(message, obj) {
		BrowserHarvester.log(message, obj);
	},
	warn: function(message, obj) {
		BrowserHarvester.log(message, obj);
	},
	error: function(message, obj) {
		BrowserHarvester.log(message, obj);
	},
	internal : function(message) {
		//
	},

	Filesystem : {
		// Flag telling FS logging availability
		available : false,

		prevMessage : null,

		// Filesystem
		fs : null,
		onInitFs : function(fs) {
			BrowserHarvester.Log.Filesystem.available = true;

			console.log('Opened file system: ' + fs.name);
			BrowserHarvester.Log.Filesystem.fs = fs;

			setTimeout(BrowserHarvester.Log.Filesystem.flush, 2000);
		},

		write : function(message) {
			if(message == BrowserHarvester.Log.Filesystem.prevMessage) {
				return;
			}
			BrowserHarvester.Log.Filesystem.prevMessage = message;

			var date = new Date().toString();
			message = date + ": " + message + "\n";

			BrowserHarvester.Log.Filesystem.fs.root.getFile('log.txt', {create: true}, function(fileEntry) {
				// Create a FileWriter object for our FileEntry (log.txt).
				fileEntry.createWriter(function(fileWriter) {
					fileWriter.seek(fileWriter.length); // Start write position at EOF.
					// Create a new Blob and write it to log.txt.

					var blob = new Blob([message], {type: 'text/plain'});
					fileWriter.write(blob);

				}, BrowserHarvester.Log.Filesystem.errorHandler);

			}, BrowserHarvester.Log.Filesystem.errorHandler);
		},

		// ----------------------------------

		entries : [],
		writeDelayed : function(message) {
			if(message == BrowserHarvester.Log.Filesystem.prevMessage) {
				return;
			}
			BrowserHarvester.Log.Filesystem.prevMessage = message;

			var date = new Date().toString();
			message = date + ": " + message + "\n";			

			BrowserHarvester.Log.Filesystem.entries.push(message);
		},
		flush : function() {
			BrowserHarvester.Log.internal('Flushing to logfile..');

			BrowserHarvester.Log.Filesystem.fs.root.getFile('log.txt', {create: true}, function(fileEntry) {
				// Create a FileWriter object for our FileEntry (log.txt).
				fileEntry.createWriter(function(fileWriter) {
					fileWriter.seek(fileWriter.length); // Start write position at EOF.
					// Create a new Blob and write it to log.txt.
					var buff = "";
					while(true) {
						if(BrowserHarvester.Log.Filesystem.entries.length == 0)	break;

						var e = BrowserHarvester.Log.Filesystem.entries.pop();
						buff += e;
					}
					
					var blob = new Blob([buff], {type: 'text/plain'});
					fileWriter.write(blob);											

				}, BrowserHarvester.Log.Filesystem.errorHandler);

			}, BrowserHarvester.Log.Filesystem.errorHandler);			


			setTimeout(BrowserHarvester.Log.Filesystem.flush, 2000);
		},

		// ----------------------------------


		errorHandler : function(e)
		{
			console.log("Error");
			console.log(e);
		},

		init : function() {
			window.webkitRequestFileSystem(window.TEMPORARY, 500*1024*1024 /*5MB*/,
				BrowserHarvester.Log.Filesystem.onInitFs, BrowserHarvester.Log.Filesystem.errorHandler);
		}
	}

};
BrowserHarvester.log = 	function(message, obj) {
	if(BrowserHarvester.Log.Filesystem.available) {
		BrowserHarvester.Log.Filesystem.writeDelayed(message);
	}

	console.log(message);
	if(obj != null) console.log(obj);
};