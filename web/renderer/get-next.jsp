<%@page contentType="text/json" pageEncoding="UTF-8"%>{
	"url": "http://sl.wikipedia.org",
	"valid" : true,
	"script" : {
		"type" : "plain",
		"event": "document.load",
		"content" : "alert('hello');"
	}
}
