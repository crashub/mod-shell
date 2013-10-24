package org.vertx.mods;

import org.vertx.java.busmods.BusModBase;

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
