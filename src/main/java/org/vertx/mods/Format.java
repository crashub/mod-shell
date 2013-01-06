package org.vertx.mods;

import org.vertx.java.core.eventbus.EventBus;
import org.vertx.java.core.json.JsonObject;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
public enum Format {

  JSON() {

    @Override
    public void publish(EventBus bus, String address, String value) {
      JsonObject json = VertxCommand.parseJson(value);
      bus.publish(address, json);
    }

    @Override
    public void send(EventBus bus, String address, String value) {
      JsonObject json = VertxCommand.parseJson(value);
      bus.send(address, json);
    }
  },

  STRING() {

    @Override
    public void publish(EventBus bus, String address, String value) {
      bus.publish(address, value);
    }

    @Override
    public void send(EventBus bus, String address, String value) {
      bus.send(address, value);
    }
  };

  public abstract void publish(EventBus bus, String address, String value);

  public abstract void send(EventBus bus, String address, String value);
}
