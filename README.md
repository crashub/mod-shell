The module mod-shell provides an embedded shell for Vert.x based on CRaSH shell.

# Features

* List Vert.x http and net servers
* List Vert.x deployments
* Interact with Vert.x shared maps
* Hot deploy a module or a verticle
* Hot undeploy a Vert.x deployment
* Interact with the event bus: send or receive messages
* Extend Vertx with custom commands

# Usage

## In your application

Deploy the module org.crashub.shell-v1.1 in your verticle.

## Standalone

    echo '{"crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin","crash.ssh.port":2000}' > conf.json
    vertx runmod org.crashub.shell-v1.1 -conf conf.json

## As a Vert.x module

Deploy the org.crashub.shell-v1.1 module from your verticle.

# Configuration

Default config:

    {
        "crash.auth": "simple",
        "crash.auth.simple.username": "admin",
        "crash.auth.simple.password": "admin",
        "crash.ssh.port": "2000"
    }
    
    
Configuration is mostly based on CRaSH configuration explained in CRaSH [documentation](http://www.crashub.org/#doc):

# Walkthrough

## Install

Install the shell module:

    vertx install org.crashub.shell-v1.1
    echo '{"crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin"}' > conf.json
    vertx runmod org.crashub.shell-v1.1 -conf conf.json

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

    load('vertx.js');
    vertx.deployModule("vertx.mailer-v1.1", {
        "address": "test.my_mailer",
        "host": "smtp.googlemail.com",
        "port": 465,
        "ssl": true,
        "auth": true,
        "username": "julien.viet@gmail.com",
        "password": "XXXXXXXXXXX"
    });
    vertx.deployModule("org.crashub.shell-v1.1", {
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

    load('vertx.js');
    vertx.deployModule("com.bloidonia.jdbc-persistor-v1.1", {
        "address": "db",
        "driver": "org.hsqldb.jdbcDriver",
        "url"      : "jdbc:hsqldb:mem:test",
        "username" : "",
        "password": ""
    });
    vertx.deployModule("org.crashub.shell-v1.1", {
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

## Creating custom commands in Groovy

Pretty much like the first example, however we add the current directory under the "cmd" key in the configuration:

    echo '{"cmd":".","crash.auth": "simple","crash.auth.simple.username": "admin","crash.auth.simple.password": "admin"}' > conf.json

Edit hello.groovy

    return "Hello from ${context.attributes.vertx}"

Run Vert.x:

    vertx runmod org.crashub.shell-v1.1 -conf conf.json

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

For more information about CRaSH commands please read the [documentation](http://www.crashub.org/#doc)

# Vert.x commands

The module embeds an SSH server to an embedded shell in Vert.x . CRaSH comes out of the box with a useful bunch of commands,
however the Vert.x integration provides commands for Vert.x. You can look or modify the existing commands in the mods directory
($VERTX_MODS) as resources under $VERTX_MODS/org.crashub.shell-v1.1/crash/commands/vertx :

    (! 561)-> ls -l $VERTX_MODS/org.crashub.shell-v1.1/crash/commands/vertx
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
    bus       Interact with the vert.x event bus                                                                                                                                                                           
    module    Interact with vert.x modules                                                                                                                                                                                 
    sharedmap Interact with the vert.x shared map                                                                                                                                                                          
    verticle  Interact with vert.x verticles                                                                                                                                                                               
    vertx     Interact with vert.x                                                                                                                                                                                         

## vertx command

    usage: vertx[-h | --help] COMMAND [ARGS]

    The most commonly used vertx commands are:
       config           Display vert.x config
       deployments      List existing deployments
       undeploy         Undeploy a deployment
       net              List existing net servers
       http             List existing http servers

## module command

    usage: module [-h | --help] deploy [-i | --instances] main ... config

       [-h | --help]      command usage
       [-i | --instances] The number of instances
       main               The main to deploy
       ... config         The module config

## sharedmap command

    usage: sharedmap[-h | --help] COMMAND [ARGS]

    The most commonly used sharedmap commands are:
       get              Get a value
       put              Put a value
       clear            Clear a map
       destroy          Destroy a shared map
       keys             List content of a map
       rm               Remove a value

## verticle command

    usage: verticle [-h | --help] deploy [-i | --instances] [-w | --worker] main ... config

       [-h | --help]      command usage
       [-i | --instances] The number of instances
       [-w | --worker]    Specify the verticle to be a worker
       main               The main to deploy
       ... config         The verticle config

## bus command

    usage: bus[-h | --help] COMMAND [ARGS]

    The most commonly used bus commands are:
       publish          Publish a JSON object as a message
       send             Send a message on the bus
       subscribe        Read message from the bus

