var express = require("express");
var app = express();
//var multer = require('multer');
var bodyParser = require('body-parser');
var polygon = require('polygon');
var request = require('request');

var URL = 'https://turfwarz.azure-mobile.net';
app.set('port', (process.env.PORT || 5000));

var server = app.listen(app.get("port"), function () {
    var host = server.address().address;
    var port = server.address().port;

    console.log('Example app listening at http://%s:%s', host, port);
});

app.use(express.static(__dirname + '/public'));
//app.use(multer());
app.use(bodyParser.json());
app.use(bodyParser.urlencoded());

app.post('/polygon', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    var p = new Polygon(req.body.points);

    var options = {
		method:"POST",
		url: URL + "/tables/Map",
		headers:{
			'Accept':'application/json',
			'Content-Length':''
		}, 
		body:{
		  'points':req.body.points,
		  'player_id':req.body.player_d
		},
		json:true
	}
	request(options, function(error, response){

	});
});

app.get('/_ah/health', function(req, res) {
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });
    console.log(req.headers);
    res.status(200).send('ok');
});

app.post('/test', function(req, res) {
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });
    res.status(200).send('ok');
});