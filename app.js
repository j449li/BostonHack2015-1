var express = require("express");
var app = express();
//var multer = require('multer');
var bodyParser = require('body-parser');

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

app.get('/locations', function(req, res){
    res.set({
        "Content-Type": "application/json",
        "Access-Control-Allow-Origin": "*"
    });
    res.status(200).send('ok'); 
});

app.get('/_ah/health', function(req, res) {
    res.status(200).send('ok');
});