package org.vertx.mods;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellFactory;
import org.vertx.java.busmods.BusModBase;
import org.vertx.java.core.Handler;
import org.vertx.java.core.eventbus.Message;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.util.LinkedList;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public class CRaSHBusMod extends BusModBase {

  /** . */
  private VertxPluginLifeCycle lifeCycle;

  @Override
  public void start() {
    try {
      ClassLoader loader = Thread.currentThread().getContextClassLoader();
      lifeCycle = new VertxPluginLifeCycle(loader, this);
      lifeCycle.start();
      getVertx().eventBus().registerHandler("crash.execute", new Handler<Message<JsonObject>>() {
        @Override
        public void handle(Message<JsonObject> event) {
          JsonArray requestsArray = event.body().getArray("requests");
          String replyTo = event.body().getString("replyTo");
          LinkedList<String> requests = new LinkedList<>();
          for (Object o : requestsArray) {
            requests.add(o.toString());
          }
          ShellFactory factory = lifeCycle.getContext().getPlugin(ShellFactory.class);
          Shell shell = factory.create(null);
          VertxProcessContext context = new VertxProcessContext(
              getVertx().eventBus(),
              shell,
              requests,
              replyTo);
          context.run();
        }
      });
    }
    catch (Exception e) {
      throw new RuntimeException("Could not start mod", e);
    }
  }

  @Override
  public void stop() {
    if (lifeCycle != null) {
      lifeCycle.stop();
      lifeCycle = null;
    }
  }
}
