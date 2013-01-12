package org.vertx.mods;

import org.crsh.plugin.PluginContext;
import org.crsh.plugin.PluginLifeCycle;
import org.crsh.plugin.PropertyDescriptor;
import org.crsh.plugin.ServiceLoaderDiscovery;
import org.crsh.vfs.FS;
import org.crsh.vfs.Path;
import org.vertx.java.core.json.JsonArray;
import org.vertx.java.core.json.JsonObject;

import java.io.File;
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
    JsonObject verticleConfig = verticle.getContainer().getConfig();

    // Build configuration
    Properties config = new Properties();
    for (String s : verticleConfig.getFieldNames()) {
      if (s.startsWith("crash.")) {
        String value = "" + verticleConfig.getField(s);
        config.put(s, value);
      }
    }
    setConfig(config);

    //
    FS confFS = new FS();

    //
    FS cmdFS = new FS();
    cmdFS.mount(loader, Path.get("/crash/commands/"));
    Object o = verticleConfig.getField("cmd");
    if (o instanceof String) {
      cmdFS.mount(new File((String)o));
    } else if (o instanceof JsonArray) {
      JsonArray array = (JsonArray)o;
      for (Object e : array) {
        if (e instanceof String) {
          cmdFS.mount(new File((String)e));
        }
      }
    }

    //
    PluginContext context = new PluginContext(
        new ServiceLoaderDiscovery(loader),
        Collections.unmodifiableMap(attributes),
        cmdFS,
        confFS,
        loader);

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
