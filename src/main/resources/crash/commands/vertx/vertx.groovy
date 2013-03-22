package crash.commands.vertx

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage;
import org.vertx.java.core.http.impl.DefaultHttpServer
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.net.impl.DefaultNetServer
import org.vertx.java.core.net.impl.ServerID
import org.vertx.mods.VertxCommand

@Usage("Interact with vert.x")
public class vertx extends VertxCommand {

  @Command
  @Usage("List existing http servers")
  public void http() {
    Map<ServerID, DefaultHttpServer> servers = getVertx().sharedHttpServers();
    servers.each { id, server ->
      context.provide([
        ID: "$id",
        TCPKeepAlive: "${server.TCPKeepAlive?:'-'}",
        TCPNoDelay: "${server.TCPNoDelay?:'-'}",
        SSL: "${server.SSL?:'-'}",
        ReceiveBufferSize: "${server.receiveBufferSize?:'-'}",
        SendBufferSize: "${server.sendBufferSize?:'-'}",
        SoLinger: "${server.soLinger?:'-'}",
        AcceptBacklog: "${server.acceptBacklog?:'-'}",
        TrafficClass: "${server.trafficClass?:'-'}",
        KeyStorePath: "${server.keyStorePath?:'-'}",
        KeyStorePassword: "${server.keyStorePassword?:'-'}",
        TrustStorePath: "${server.trustStorePath?:'-'}",
        TrustStorePassword: "${server.trustStorePassword?:'-'}",
      ]);
    }
  }

  @Command
  @Usage("List existing net servers")
  public void net() {
    Map<ServerID, DefaultNetServer> servers = getVertx().sharedNetServers();
    servers.each { id, server ->
      context.provide([
        ID: "$id",
        TCPKeepAlive: "${server.TCPKeepAlive?:'-'}",
        TCPNoDelay: "${server.TCPNoDelay?:'-'}",
        SSL: "${server.SSL?:'-'}",
        ReceiveBufferSize: "${server.receiveBufferSize?:'-'}",
        SendBufferSize: "${server.sendBufferSize?:'-'}",
        SoLinger: "${server.soLinger?:'-'}",
        AcceptBacklog: "${server.acceptBacklog?:'-'}",
        TrafficClass: "${server.trafficClass?:'-'}",
        KeyStorePath: "${server.keyStorePath?:'-'}",
        KeyStorePassword: "${server.keyStorePassword?:'-'}",
        TrustStorePath: "${server.trustStorePath?:'-'}",
        TrustStorePassword: "${server.trustStorePassword?:'-'}",
        ReuseAddress: "${server.reuseAddress?:'-'}",
      ]);
    }
  }

  @Command
  @Usage("List existing deployments")
  public void deployments() {
    deployments.each { name, deployment ->
      context.provide([
          id: name,
          modName: "${deployment.modName}",
          modDir: "${deployment.modDir}",
          config: "${deployment.config}"
      ]);
    }
  }

  @Command
  @Usage("Undeploy a deployment")
  public void undeploy(
      @Usage("The deployment id")
      @Argument(name =  "id")
      @Required String id) {
    manager.undeploy(id, null);
  }

  @Command
  @Usage("Display vert.x config")
  public void config() {
    JsonObject config = container.config;
    if (config != null) {
      config.toMap().each { key, value ->
        context.provide([
            key: key,
            value: value
        ]);
      }
    }
  }

}