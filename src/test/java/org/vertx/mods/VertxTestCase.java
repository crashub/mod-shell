package org.vertx.mods;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.http.HttpServerRequest;
import org.vertx.java.core.net.NetSocket;
import org.vertx.java.deploy.impl.VerticleManager;
import org.vertx.java.test.TestModule;
import org.vertx.java.test.VertxConfiguration;
import org.vertx.java.test.VertxTestBase;
import org.vertx.java.test.junit.VertxJUnit4ClassRunner;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
@RunWith(VertxJUnit4ClassRunner.class)
@VertxConfiguration()
@TestModule(name="crash-v1.0", jsonConfig = "{" +
    "\"crash.auth\":\"simple\"," +
    "\"crash.auth.simple.username\":\"admin\"," +
    "\"crash.auth.simple.password\":\"admin\"" +
    "}")
public class VertxTestCase extends VertxTestBase {

  static {

  }

  @Test
  public void testFoo() throws Exception {



    VerticleManager manager = getManager();
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


    getEventBus().registerHandler("FOO", new Handler<Message<String>>() {
      public void handle(Message<String> event) {
        System.out.println("Got message " + event);
        event.reply("Got your message");
      }
    });

    System.out.println("SLEEPING");
    Thread.sleep(100 * 1000);

  }

}
