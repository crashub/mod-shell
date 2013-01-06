package org.vertx.mods;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.vertx.java.core.json.JsonObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Properties;

/** @author <a href="mailto:julien.viet@exoplatform.com">Julien Viet</a> */
class VertxPluginLifeCycle extends PluginLifeCycle {

  /** . */
  private final ClassLoader loader;

  /** . */
  private final CRaSHBusMod verticle;

  /** . */
  private final PluginContext context;

  VertxPluginLifeCycle(ClassLoader loader, CRaSHBusMod verticle) throws Exception {

    //
    HashMap<String, Object> attributes = new HashMap<String, Object>();
    attributes.put("vertx", verticle.getVertx());
    attributes.put("container", verticle.getContainer());

    //
    FS confFS = new FS();

    //
    FS cmdFS = new FS().mount(loader, Path.get("/crash/commands/"));
    PluginContext context = new PluginContext(
        new ServiceLoaderDiscovery(loader),
        Collections.unmodifiableMap(attributes),
        cmdFS,
        confFS,
        loader);

    // Build configuration
    JsonObject verticleConfig = verticle.getContainer().getConfig();
    Properties config = new Properties();
    for (String s : verticleConfig.getFieldNames()) {
      if (s.startsWith("crash.")) {
        String value = "" + verticleConfig.getField(s);
        config.put(s, value);
      }
    }
    setConfig(config);

    //
    this.loader = loader;
    this.verticle = verticle;
    this.context = context;
  }

  void start() {
    context.refresh();
    start(context);
  }
}
