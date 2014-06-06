package crash.commands.vertx

import org.crsh.cli.*
import org.crsh.command.ScriptException
import org.vertx.java.core.json.JsonObject
import org.vertx.mods.VertxCommand

@Usage("interact with vert.x modules")
public class module extends VertxCommand {

  @Command
  @Usage("deploy a module")
  public void deploy(
      @Usage("the main to deploy")
      @Argument(name =  "main")
      @Required String main,
      @Usage("the number of instances")
      @Option(names = ["i", "instances"])
      Integer instances,
      @Usage("the module config")
      @Argument(name =  "config", unquote = false)
      List<String> parts) {
    JsonObject config = parts != null ? parseJson(parts) : null;
    try {
      if (instances == null) {
        if (config == null) {
          container.deployModule(main);
        } else {
          container.deployModule(main, config);
        }
      } else {
        if (config == null) {
          container.deployModule(main, instances);
        } else {
          container.deployModule(main, config, instances);
        }
      }
    }
    catch (Exception e) {
      throw new ScriptException("Could not deploy verticle $main: $e.message", e)
    }
  }
}