var express = require("express");
var app = express();
//var multer = require('multer');
var bodyParser = require('body-parser');
var Polygon = require('polygon');

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

app.post('/map/update', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    var poly = new Polygon(req.body.points);
    console.log(poly.area());

    var options = {
		method:"POST",
		url: URL + "/api/polygon",
		headers:{
			'Accept':'application/json'
			// 'Content-Length': JSON.stringify(req.body).length
		}, 
		body:{
		  // 'points':req.body.points,
		  'player_id':req.body.player_id,
		  'area':poly.area()
		},
		json:true
	}
	request(options, function(error, response){
		if(error){
			console.log(error);
		}
		else{
			console.log(response.body);
			res.status(200).send('ok');
		}
	});
});

app.post('/user/login', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    
});

app.get('/user/info', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    var poly = new Polygon(req.body.points);
    console.log(poly.area());

    var options = {
		method:"POST",
		url: URL + "/api/user",
		headers:{
			'Accept':'application/json'
			// 'Content-Length': JSON.stringify(req.body).length
		}, 
		body:{
		  'username':req.body.username,
		  'area':poly.area()
		},
		json:true
	}
	request(options, function(error, response){
		if(error){
			console.log(error);
		}
		else{
			console.log(response.body);
			res.status(200).send('ok');
		}
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
    console.log(req.body);
    // console.log(JSON.stringify(req.body).length);
    res.status(200).send('ok');
});