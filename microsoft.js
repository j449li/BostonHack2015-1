var request = require('request');
var geolib = require('geolib');
var URL = 'https://turfwarz.azure-mobile.net';

var LOWER = 'lower';
var HIGHER = 'higher';
var modifier = Math.pow(10, 5);
var y = function(){
	return Math.round(10/111111 * modifier); 
}//per degree of latitude
var x = function(){
	var latitude = 42.345618;

	return Math.round(10/(111111 * Math.cos(latitude * (Math.PI / 180))) * modifier);
}//per degree of longitude
// var deltaLat = y();
// var deltaLng = x();

var deltaLat = 18;
var deltaLng = 24;

var multipleLat = function(number, bound){
	if(number % deltaLat === 0){
		return number;
	}
	else{
		var remainder = number % deltaLat;
		if(bound === 'lower'){
			return number - remainder;
		}
		else{
			return number - remainder + deltaLat;
		}
	}
}
var multipleLng = function(number, bound){
	if(number % deltaLng === 0){
		return number;
	}
	else{
		var remainder = number % deltaLng;
		if(bound === 'lower'){
			return number - remainder;
		}
		else{
			return number - remainder + deltaLng;
		}
	}
}

var login = function(username, password, callback){
	var options = {
		method:"POST",
		url: URL + "/api/user",
		headers:{
			'Accept':'application/json'
		}, 
		body:{
		  'username':username,
		  'password':password
		},
		json:true
	}
	request(options, function(error, response){
		callback(response);
	});
}

var update = function(points, player_id, callback){
	// console.log(deltaLat);
	// console.log(deltaLng);

	var azureTablePoints = [];
	points.forEach(function(elem, index, arr){
		azureTablePoints.push({
			latitude: multipleLat(Math.round(elem.latitude*modifier, LOWER))/modifier,
			longitude: multipleLat(Math.round(elem.longitude*modifier, LOWER))/modifier
		});
	});

    var min_lat = points[0].latitude;
    var max_lat = points[0].latitude;
    var min_lng = points[0].longitude;
    var max_lng = points[0].longitude;
    for(var i = 1; i < points.length; i++){
    	if(points[i].latitude < min_lat){
    		min_lat = points[i].latitude;
    	}
    	if(points[i].latitude > max_lat){
    		max_lat = points[i].latitude;
    	}
    	if(points[i].longitude < min_lng){
    		min_lng = points[i].longitude;
    	}
    	if(points[i].longitude > max_lng){
    		max_lng = points[i].longitude;
    	}
    }

	var bottomLeft = {latitude: multipleLat(Math.round(min_lat*modifier), LOWER), longitude: multipleLng(Math.round(min_lng*modifier), LOWER)};
    var topLeft = {latitude: multipleLat(Math.round(min_lat*modifier), LOWER), longitude: multipleLng(Math.round(max_lng*modifier), HIGHER)};
    var bottomRight = {latitude: multipleLat(Math.round(max_lat*modifier), HIGHER), longitude: multipleLng(Math.round(min_lng*modifier), LOWER)};
    var topRight = {latitude: multipleLat(Math.round(max_lat*modifier), HIGHER), longitude: multipleLng(Math.round(max_lng*modifier), HIGHER)};

    // console.log(bottomLeft);
    // console.log(topLeft);
    // console.log(bottomRight);
    // console.log(topRight);

    for(var i = bottomLeft.latitude + deltaLat; i < bottomRight.latitude - deltaLat; i = i + deltaLat){
    	for(var j = bottomLeft.longitude + deltaLng; j < topLeft.longitude - deltaLng; j = j + deltaLat){
    		var testPoint = {latitude: i / modifier, longitude: j / modifier};
    		var inPoly = geolib.isPointInside(testPoint, points);
    		// console.log(testPoint);
    		// console.log(inPoly);
			if(inPoly){
				azureTablePoints.push(testPoint);
			}
    	}
    }

    var options = {
		method:"POST",
		url: URL + "/api/polygon",
		headers:{
			'Accept':'application/json'
		}, 
		body:{
		  'points':azureTablePoints,
		  'player_id':player_id
		},
		json:true
	}
	request(options, function(error, response){
		callback(response);
	});
}

var info = function(player_id, callback){
	var options = {
		method:"POST",
		url: URL + "/api/area",
		headers:{
			'Accept':'application/json'
		}, 
		body:{
		  'player_id': player_id
		},
		json:true
	}

	request(options, function(error, response){
		callback(response);
	});
}

module.exports = {
	login: login,
	update: update,
	info: info
}