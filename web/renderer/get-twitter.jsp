<%@page contentType="text/json" pageEncoding="UTF-8"%>{
	"url": "https://twitter.com/TinaMaze",
	"valid" : true,
	"rule": {
		"script" : "var currElCount = 0; function loadMoarAndMoar() { commit(); if(document.getElementsByTagName('*').length > currElCount) { currElCount = document.getElementsByTagName('*').length; window.scrollTo(0, document.height); } setTimeout(loadMoarAndMoar, 500); } setTimeout(loadMoarAndMoar, 500);"
	}
}
