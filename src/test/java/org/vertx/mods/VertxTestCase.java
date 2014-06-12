package org.vertx.mods;

import org.junit.Test;
import org.vertx.java.core.AsyncResult;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.json.JsonObject;
import org.vertx.java.core.net.NetSocket;
import org.vertx.testtools.TestVerticle;

import static org.vertx.testtools.VertxAssert.*;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class VertxTestCase extends TestVerticle {

  @Override
  public void start() {
    initialize();
    container.deployModule(
        System.getProperty("vertx.modulename"),
        new JsonObject().
            putString("crash.auth", "simple").
            putString("crash.ssh.port", "2000").
            putString("crash.auth.simple.username", "admin").
            putString("crash.auth.simple.password", "admin"),
        new Handler<AsyncResult<String>>() {
      public void handle(AsyncResult<String> stringAsyncResult) {
        startTests();
      }
    });
  }

  @Test
  public void testFoo() throws Exception {

//    testComplete();

//    VerticleManager manager = vertx.getManager();
//    manager.deployVerticle();
//    System.out.println("vertx = " + getVertx());
//    System.out.println("manager = " + manager);

    getVertx().createHttpServer().requestHandler(new Handler<HttpServerRequest>() {
      public void handle(HttpServerRequest event) {
      }
    }).setAcceptBacklog(50).listen(8080);

    getVertx().createNetServer().connectHandler(new Handler<NetSocket>() {
      public void handle(NetSocket event) {
      }
    }).listen((8081));


    vertx.eventBus().registerHandler("FOO", new Handler<Message<String>>() {
      public void handle(Message<String> event) {
        System.out.println("Got message " + event);
        event.reply("Got your message");
      }
    });
  }
}
