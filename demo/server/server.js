// A small server setup with mod-shell

var vertx = require('vertx');
var console = require('vertx/console');
var container = require('vertx/container');

// Shared map
var map = vertx.getMap('demo.mymap');
map.put('some-key', 'some-value');

// Deploy mod-shell and additional modules
container.deployModule("com.bloidonia~mod-jdbc-persistor~2.0.0-beta5", {
    "address" : "db",
    "driver" : "org.hsqldb.jdbcDriver",
    "url" : "jdbc:hsqldb:mem:test",
    "username" : "",
    "password" : ""
});
container.deployModule("io.vertx~mod-mailer~2.0.0-beta2", {
    "address": "test.my_mailer",
    "host": "smtp.googlemail.com",
    "port": 465,
    "ssl": true,
    "auth": true,
    "username": "username",
    "password": "password"
});
container.deployModule("org.crashub~vertx.shell~2.0.6", {
    "cmd": "../commands",
    "crash.auth": "simple",
    "crash.auth.simple.username": "admin",
    "crash.auth.simple.password": "admin",
    "crash.ssh.port": 2000
});

