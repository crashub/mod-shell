package crash.commands.vertx

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.command.ScriptException
import org.vertx.java.core.json.JsonObject
import org.vertx.mods.VertxCommand

@Usage("Interact with vert.x verticles")
public class verticle extends VertxCommand {

  @Command
  @Usage("Deploy a verticle")
  public void deploy(
      @Usage("The main to deploy")
      @Argument(name =  "main")
      @Required String main,
      @Usage("The number of instances")
      @Option(names = ["i", "instances"])
      Integer instances,
      @Usage("Specify the verticle to be a worker")
      @Option(names = ["w", "worker"])
      boolean worker,
      @Usage("The verticle config")
      @Argument(name =  "config", unquote = false)
      List<String> parts) {
    JsonObject config = parts != null ? parseJson(parts) : null;
    try {
      if (worker) {
        if (instances == null) {
          if (config == null) {
            container.deployWorkerVerticle(main);
          } else {
            container.deployWorkerVerticle(main, config);
          }
        } else {
          if (config == null) {
            container.deployWorkerVerticle(main, instances);
          } else {
            container.deployWorkerVerticle(main, config, instances);
          }
        }
      } else {
        if (instances == null) {
          if (config == null) {
            container.deployVerticle(main);
          } else {
            container.deployVerticle(main, config);
          }
        } else {
          if (config == null) {
            container.deployVerticle(main, instances);
          } else {
            container.deployVerticle(main, config, instances);
          }
        }
      }
    }
    catch (Exception e) {
      throw new ScriptException("Could not deploy verticle $main: $e.message", e)
    }
  }
}