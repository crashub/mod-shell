package crash.commands.vertx

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Man
import org.crsh.cli.Named
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.crsh.text.ui.BorderStyle
import org.crsh.text.ui.LabelElement
import org.crsh.text.ui.RowElement
import org.crsh.text.ui.TableElement
import org.crsh.text.ui.TreeElement;
import org.vertx.java.core.http.impl.DefaultHttpServer
import org.vertx.java.core.json.JsonArray
import org.vertx.java.core.json.JsonObject
import org.vertx.java.core.net.impl.DefaultNetServer
import org.vertx.java.core.net.impl.ServerID
import org.vertx.java.platform.impl.Deployment
import org.vertx.mods.VertxCommand

@Usage("interact with vert.x")
public class vertx extends VertxCommand {

  @Command
  @Usage("list existing http servers")
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
  @Usage("list existing net servers")
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
  @Usage("list existing deployments")
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
  @Usage("stop vertx")
  public String stop() {
    System.exit(0)
  }

  @Command
  @Usage("undeploy a deployment")
  public void undeploy(
      @Usage("The deployment id")
      @Argument(name =  "id")
      @Required String id) {
    manager.undeploy(id, null);
  }

  @Command
  @Usage("Undeploy all deployments containing the given module id (owner~name~version)")
  public TreeElement undeployByModId(
      @Usage("The module id")
      @Argument(name =  "id")
      @Required String id) {

    if(id == "null") return new TreeElement("Cannot undeploy null module id")

    String result = ""
    for(deployment in deployments)
    {
      if(deployment.value.modID.toString().equals(id))
      {
        undeploy(deployment.value.name)
        result += "Undeployed " + deployment.value.name +"\n"
      }
    }
    if(result.equals("")) return new TreeElement("No existing deployments contain " + id)
    return result;
  }

  @Command
  @Usage("Undeploy and uninstall all deployments containing the given module id (owner~name~version)")
  public TreeElement undeployUninstallByModId(
     @Usage("The module id")
      @Argument(name =  "id")
      @Required String id) {

    if(id == "null") return new TreeElement("Cannot undeploy null module id")

    String result = ""
    for(deployment in deployments)
    {
      if(deployment.value.modID.toString().equals(id))
      {
        undeploy(deployment.value.name)
        result += "Undeployed " + deployment.value.name +"\n"
        manager.uninstallModule(deployment.value.modID.toString(),null)
        result += "Uninstalled " + deployment.value.modID.toString() + "\n"
      }
    }
    if(result.equals("")) return new TreeElement("No existing deployments contain " + id)
    return result;
  }

  @Command
  @Usage("Undeploy all deployments containing the given module owner and name")
  public TreeElement undeployByModName(
      @Usage("The module Owner~Name, not including version number")
      @Argument(name =  "ownerName")
      @Required String ownerName) {

    if(ownerName == "null") return new TreeElement("Cannot undeploy null module id")

    String result = ""
    for(deployment in deployments)
    {
      if(!(deployment.value.modID == null))
      {
        def combinedName = deployment.value.modID.owner + "~" + deployment.value.modID.name
        if(combinedName.equals(ownerName))
        {
          undeploy(deployment.value.name) // deployment name (ex: deployment-f4e43882-131d-465a-a12b-051a5f54e851)
          result += "Undeployed " + deployment.value.name +"\n"
        }
      }
    }

    if(result.equals("")) return new TreeElement("No existing deployments contain " + ownerName)
    return new TreeElement(result)
  }

  @Command
  @Usage("Undeploy and uninstall all deployments containing the given module owner and name")
  public TreeElement undeployUninstallByModName(
      @Usage("The module Owner~Name, not including version number")
      @Argument(name =  "ownerName")
      @Required String ownerName) {

    if(ownerName == "null") return new TreeElement("Cannot undeploy null module id")

    String result = ""
    for(deployment in deployments)
    {
      if(!(deployment.value.modID == null))
      {
        def combinedName = deployment.value.modID.owner + "~" + deployment.value.modID.name
        if(combinedName.equals(ownerName))
        {
          undeploy(deployment.value.name) // deployment name (ex: deployment-f4e43882-131d-465a-a12b-051a5f54e851)
          result += "Undeployed " + deployment.value.name +"\n"
          manager.uninstallModule(deployment.value.modID.toString(),null)
          result += "Uninstalled " + deployment.value.modID.toString() + "\n"
        }
      }
    }

    if(result.equals("")) return new TreeElement("No existing deployments contain " + ownerName)
    return new TreeElement(result)
  }

  @Command
  @Usage("Undeploy all deployments")
  public TreeElement undeployAll(){

    String result = ""
    for(deployment in deployments)
    {
      if(!(deployment.value.modID == null) && !deployment.value.modID.name.equals("vertx.shell"))
      {
        undeploy(deployment.value.name) // deployment name (ex: deployment-f4e43882-131d-465a-a12b-051a5f54e851)
        result += "Undeployed " + deployment.value.name + "\n"
      }
    }

    if(result.equals("")) return new TreeElement("No existing deployments")
    return new TreeElement(result)
  }

  @Command
  @Usage("Undeploy and uninstall all deployments")
  public TreeElement undeployUninstallAll(){

    String result = ""
    for(deployment in deployments)
    {
      if(!(deployment.value.modID == null) && !deployment.value.modID.name.equals("vertx.shell"))
      {
        undeploy(deployment.value.name) // deployment name (ex: deployment-f4e43882-131d-465a-a12b-051a5f54e851)
        result += "Undeployed " + deployment.value.name + "\n"
        manager.uninstallModule(deployment.value.modID.toString(),null)
        result += "Uninstalled " + deployment.value.modID.toString() + "\n"
      }
    }

    if(result.equals("")) return new TreeElement("No existing deployments")
    return new TreeElement(result)
  }

  @Command
  @Usage("display vert.x config")
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

  @Command
  @Named("execute")
  @Usage("execute a shell request")
  @Man("""\
Execute requests by publishing events to the "crash.execute" address. CRaSH has an event handler that execute
requests:

% vertx execute help

Each request should be quoted or at least white spaces should be properly escaped:

% vertx execute thread\\ ls "thread ls"

When several requests are specified, they will execute sequentially:

% vertx execute "repl groovy" "1+1"

The optional reply-to argument can be used to receive the responses:

% vertx execute --reply-to screen "thread ls"

""")
  public void invoke(
      @Option(names = ["reply-to"]) @Usage("the optional reply to address for the response events") String replyTo,
      @Argument @Required @Usage("the requests to execute") List<String> requests) {
    def bus = getVertx().eventBus()
    JsonArray requestsArray = new JsonArray();
    requests.each { String request -> requestsArray.add(request); };
    def event = new JsonObject().putArray("requests", requestsArray);
    if (replyTo != null) {
      event.putString("replyTo", replyTo);
    }
    bus.publish("crash.execute", event);
  }
}