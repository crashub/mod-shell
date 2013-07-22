package crash.commands.vertx

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.text.ui.BorderStyle
import org.crsh.text.ui.LabelElement
import org.crsh.text.ui.RowElement
import org.crsh.text.ui.TableElement
import org.crsh.text.ui.TreeElement;
import org.vertx.java.core.http.impl.DefaultHttpServer
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.net.impl.DefaultNetServer
import org.vertx.java.core.net.impl.ServerID
import org.vertx.java.platform.impl.Deployment
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
  public TreeElement deployments() {

    //
    Map<String, TreeElement> nodes = [:];
    TreeElement root = new TreeElement()
    def deployments = new HashMap<String, Deployment>(deployments);

    // Build the tree
    while (deployments.size() > 0) {
      def i = deployments.entrySet().iterator()
      def entry = i.next();
      def name = entry.getKey();
      def deployment = entry.getValue();
      i.remove();

      //
      def value = new TableElement();
      value.setRightCellPadding(1);
      value.add(new RowElement().add(new LabelElement("name"), new LabelElement(deployment.name)));
      value.add(new RowElement().add(new LabelElement("id"), new LabelElement(deployment.modID)));

      //
      TreeElement node = nodes[name] = new TreeElement(value);

      //
      for (String childName : deployment.childDeployments) {
        def child = nodes[childName];
        if (child != null) {
          node.addChild(child);
        }
      }

      //
      def parentName = deployment.parentDeploymentName;
      if (parentName != null) {
        def parent = nodes[parentName];
        if (parent != null) {
          parent.addChild(node);
        }
      } else {
        root.addChild(node);
      }
    }

    //
    return root;
  }

  @Command
  @Usage("Provide more info about an existing deployment")
  public void deployment(@Argument @Required @Usage("the deployment name") String name) {
    def deployment = deployments[name];
    if (deployment == null) {
      throw new ScriptException("Deployment $name does not exist");
    }

    //
    context.provide(["property":"name", "value":deployment.name]);
    context.provide(["property":"parent", "value":deployment.parentDeploymentName]);
    context.provide(["property":"modID", "value":deployment.modID]);
    context.provide(["property":"instances", "value":deployment.instances]);
    context.provide(["property":"autoRedeploy", "value":deployment.autoRedeploy]);
    context.provide(["property":"classPath", "value":Arrays.asList(deployment.classpath)]);
    context.provide(["property":"childDeployments", "value":deployment.childDeployments]);
    context.provide(["property":"config", "value":deployment.config]);
    context.provide(["property":"main", "value":deployment.main]);
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
    JsonObject config = container.config();
    if (config != null) {
      config.toMap().each { key, value ->
        context.provide([
            name: key,
            value: value
        ]);
      }
    }
  }

}