var express = require("express");
var app = express();
//var multer = require('multer');
var bodyParser = require('body-parser');
var azure = require('./microsoft');

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

    azure.update(req.body.points, req.body.player_id, function(data){
    	if(data.statusCode != 200){
    		res.status(400).send(data.body);
    	}
    	else{
    		res.status(200).send(data.body);
    	}
    });
});

app.post('/user/login', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    azure.login(req.body.username, req.body.password, function(userData){
    	if(userData.statusCode != 200){
    		res.status(400).send(userData.body);
    	}
    	else{
    		res.status(200).send(userData.body);
    	}
    });
});

app.post('/user/info', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    azure.info(req.body.player_id, function(areaData){
    	if(areaData.statusCode != 200){
    		res.status(400).send(areaData.body);
    	}
    	else{
    		res.status(200).send(areaData.body);
    	}
    });
});

app.post('/enemy/info', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });

    azure.info(req.body.player_id, function(areaData){
    	if(areaData.statusCode != 200){
    		res.status(400).send(areaData.body);
    	}
    	else{
    		res.status(200).send(areaData.body);
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