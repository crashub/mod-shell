load('vertx.js');
vertx.deployModule("com.bloidonia.jdbc-persistor-v1.1", {
    "address": "db",
    "driver": "org.hsqldb.jdbcDriver",
    "url"      : "jdbc:hsqldb:mem:test",
    "username" : "",
    "password": ""
});
vertx.deployModule("vertx.mailer-v1.1", {
    "address": "test.my_mailer",
    "host": "smtp.googlemail.com",
    "port": 465,
    "ssl": true,
    "auth": true,
    "username": "julien.viet@gmail.com",
    "password": "gr1b0u1ll3"
});
vertx.deployModule("org.crashub.shell-v1.1", {
    "cmd": ".",
	"crash.auth": "simple",
	"crash.auth.simple.username": "admin",
	"crash.auth.simple.password": "admin",
        "crash.ssh.port": 2000
});
