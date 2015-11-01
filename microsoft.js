var request = require('request');
var URL = 'https://turfwarz.azure-mobile.net';

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
		if(error){
			console.log(error);
		}
		else{
			console.log(response.body);
			callback(response.body);
		}
	});
}

module.exports = {
	login: login
}