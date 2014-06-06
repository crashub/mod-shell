The module mod-shell provides an embedded shell for Vert.x based on [CRaSH shell](https://github.com/crashub/crash).

# Features

* Extend Vertx with a command line interface
    * Script Vert.x
    * Write your own commands in Groovy or Java (more languages later...)
* Advanced Vert.x management
    * List Vert.x http and net servers
    * List Vert.x deployments
    * Interact with Vert.x shared maps
    * Hot deploy a module or a verticle
    * Hot undeploy a Vert.x deployment
    * Interact with the event bus: send or receive messages
* Advanced shell features
    * Powerfull REPL
    * JVM commands : jdbc, thread, log, ...
    * Various plugin : mail, cron, ...
    * Writing custom commands is easy
    * Advance completion
    * SSH access

# Documentation

* This page
* Vertx.shell [screencast](https://vimeo.com/67557338)
* Official CRaSH [documentation](http://www.crashub.org/beta/reference.html)

# Examples

* [Server demo](https://github.com/crashub/mod-shell/tree/master/demo/server) : deploy vertx.shell as a module
* [Standalone demo](https://github.com/crashub/mod-shell/tree/master/demo/standalone) : run vert.x shell as a module

# Status

* 2.0 stable
* works with Vert.x 2.0.x (older version 1.x work with Vert.x 1.3.x)
* Published in Maven Central

# Usage

## In your application

Deploy the module org.crashub~vertx.shell in your verticle.

## Standalone

    echo '{"crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin","crash.ssh.port":2000}' > conf.json
    vertx runmod org.crashub~vertx.shell~2.0.5 -conf conf.json

## As a Vert.x module

Deploy the org.crashub~vertx.shell~2.0.5 module from your verticle.

# Configuration

Default config:

    {
        "crash.auth": "simple",
        "crash.auth.simple.username": "admin",
        "crash.auth.simple.password": "admin",
        "crash.ssh.port": "2000"
    }
    
    
Configuration is mostly based on CRaSH configuration explained in CRaSH [documentation](http://www.crashub.org/beta/reference.html):

# Walkthrough

## Install

Install the shell module:

    vertx install org.crashub~vertx.shell~2.0.5
    echo '{"crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin"}' > conf.json
    vertx runmod org.crashub~vertx.shell~2.0.5 -conf conf.json

## Receive and send messages

Create a message subscriber

    (! 576)-> ssh -p 2000 admin@localhost
    admin@localhost's password: 
    Welcome to Juliens-MacBook-Pro.local + !
    It is Sat Jan 12 15:47:50 CET 2013 now
    % bus subscribe the_address

Log in with another console and send a message on the_address:

    (! 501)-> ssh -p 2000 admin@localhost
    admin@localhost's password: 
    Welcome to Juliens-MacBook-Pro.local + !
    It is Sat Jan 12 15:48:52 CET 2013 now
    % bus send the_address Hello


## Send an email with the Mailer module

Create the file server.js:

    var container = require('vertx/container');
    container.deployModule("io.vertx~mod-mailer~2.0.3-beta2", {
        "address": "test.my_mailer",
        "host": "smtp.googlemail.com",
        "port": 465,
        "ssl": true,
        "auth": true,
        "username": "username",
        "password": "password"
    });
    container.deployModule("org.crashub~vertx.shell~2.0.5", {
        "cmd": ".",
        "crash.auth": "simple",
        "crash.auth.simple.username": "admin",
        "crash.auth.simple.password": "admin",
        "crash.ssh.port": 2000
    });

Run Vert.x:

    vertx run server.js

Use the shell:

    (! 569)-> ssh -p 2000 admin@localhost
    admin@localhost's password: 
    Welcome to Juliens-MacBook-Pro.local + !
    It is Sat Jan 12 15:28:37 CET 2013 now
    % bus send -f JSON test.my_mailer {"from":"julien.viet@gmail.com","to":"julien@julienviet.com","subject":"test","body":"sent from Vert.x"}

## Interact with the JDBC module

Create the server.js file:

    var container = require('vertx/container');
    container.deployModule("com.bloidonia~mod-jdbc-persistor~2.0.3-beta5", {
        "address" : "db",
        "driver" : "org.hsqldb.jdbcDriver",
        "url" : "jdbc:hsqldb:mem:test",
        "username" : "",
        "password" : ""
    });
    container.deployModule("org.crashub~vertx.shell~2.0.5", {
        "cmd": ".",
        "crash.auth": "simple",
        "crash.auth.simple.username": "admin",
        "crash.auth.simple.password": "admin",
        "crash.ssh.port": 2000
    });

Copy the HSLQDB jar in your $VERTX_HOME/lib.

Run Vert.x:

    vertx run server.js

Use the JDBC module:

    (! 575)-> ssh -p 2000 admin@localhost
    admin@localhost's password: 
    It is Sat Jan 12 15:44:58 CET 2013 now
    % bus send -f JSON -r db { "action": "select", "stmt":   "SELECT * FROM INFORMATION_SCHEMA.SYSTEM_USERS" }
    {"result":[{"INITIAL_SCHEMA":null,"ADMIN":true,"USER_NAME":""}],"status":"ok"}

The -r option stands for reply and tell the command to wait and block until a reply is provided after sending the message.
This is useful with the jdbc module as it sends the result of statement in a response.

## Request executor

CRaSH registers an event handler to the "crash.execute" address which process shell requests. This feature is experimental
at the moment (feedback welcome).

The format of the event message is Json and contains the mandatory _requests_ String array field.

    % bus publish --format JSON crash.execute {"requests":["help"]}

The optional _reply-to_ field can be used, when specified the handler sends response events to the _replyTo_ address:

    % bus publish --format JSON crash.execute {"requests":["help"],"replyTo":"screen"}

The _vertx execute_ command can be used to make this easier, it will _publish_ the message:

    % vertx execute help

Special care should be ported to the request arguments whitespaces:

    % vertx execute "thread ls"

Several requests can be specified, when a requests ends, the response is sent and the next request is processed. Therefore
 the execution order is sequential.

    % vertx execute "repl groovy" "1+1"

The _bus subscribe_ command can be used to receive the responses, of course this should be done in another
terminal:

    % bus subscribe screen

## Creating custom commands in Groovy

Pretty much like the first example, however we add the current directory under the "cmd" key in the configuration:

    echo '{"cmd":".","crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin"}' > conf.json

Edit hello.groovy

    return "Hello from ${context.attributes.vertx}"

Run Vert.x:

    vertx runmod org.crashub~vertx.shell~2.0.5 -conf conf.json

Use the hello command:

    (! 505)-> ssh -p 2000 admin@localhost
    admin@localhost's password:
    Welcome to Juliens-MacBook-Pro.local + !
    It is Sat Jan 12 16:41:50 CET 2013 now
    % hello
    Hello from org.vertx.java.core.impl.DefaultVertx@5e6b6477

Inside a Groovy command the current Vertx and Container objects are available under

    def vertx = context.attributes.vertx
    def container = context.attributes.container

For more information about CRaSH commands please read the [documentation](http://www.crashub.org/beta/reference.html)

Commands located in _cmd_ are live reloaded. Note that commands located under _crash/commands_ are loaded only once as they are
 considered as classpath commands (and classpath is not supposed to change).

# Vert.x commands

The module embeds an SSH server to an embedded shell in Vert.x . CRaSH comes out of the box with a useful bunch of commands,
however the Vert.x integration provides commands for Vert.x. You can look or modify the existing commands in the mods directory
($VERTX_MODS) as resources under $VERTX_MODS/org.crashub~vertx.shell~2.0.5/crash/commands/vertx :

    (! 561)-> ls -l $VERTX_MODS/org.crashub~vertx.shell~2.0.5/crash/commands/vertx
    total 40
    -rw-r--r--  1 julien  staff  3463 Jan 12 16:17 bus.groovy
    -rw-r--r--  1 julien  staff  1213 Jan 12 16:17 module.groovy
    -rw-r--r--  1 julien  staff  2212 Jan 12 16:17 sharedmap.groovy
    -rw-r--r--  1 julien  staff  2016 Jan 12 16:17 verticle.groovy
    -rw-r--r--  1 julien  staff  3150 Jan 12 16:17 vertx.groovy

You can modify the existing commands if you like or add new commands, such commands will be visible each time the module
is deployed.

## help command

    (! 566)-> ssh -p 2000 admin@localhost
    admin@localhost's password: 
    It is Sat Jan 12 15:09:46 CET 2013 now
    % help
    Try one of these commands with the -h or --help switch:                                                                                                                                                                
                                                                                                                                                                                                                       
    NAME      DESCRIPTION                                                                                                                                                                                                  
    bus       interact with the vert.x event bus
    module    interact with vert.x modules
    sharedmap interact with the vert.x shared map
    verticle  interact with vert.x verticles
    vertx     interact with vert.x

## vertx command

    usage: vertx [-h | --help] COMMAND [ARGS]

    The most commonly used vertx commands are:
       execute          execute a shell request
       net              list existing net servers
       config           display vert.x config
       undeploy         undeploy a deployment
       deployments      list existing deployments
       deployment       Provide more info about an existing deployment
       http             list existing http servers


## module command

    usage: module [-h | --help] COMMAND [ARGS]

     The most commonly used module commands are:
        deploy           deploy a module

## sharedmap command

    usage: sharedmap [-h | --help] COMMAND [ARGS]

    The most commonly used sharedmap commands are:
       get              get a value
       put              put a value
       clear            clear a map
       destroy          destroy a shared map
       keys             list content of a map
       rm               remove a value

## verticle command

    usage: verticle [-h | --help] COMMAND [ARGS]

    The most commonly used verticle commands are:
       deploy           deploy a verticle

## bus command

    usage: bus [-h | --help] COMMAND [ARGS]

    The most commonly used bus commands are:
       publish          publish a JSON object as a message
       send             send a message on the bus
       subscribe        read message from the bus
