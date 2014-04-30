package crash.commands.vertx

import org.crsh.cli.Argument
import org.crsh.cli.Command
import org.crsh.cli.Option
import org.crsh.cli.Required
import org.crsh.cli.Usage
import org.vertx.java.core.Handler
import org.vertx.java.core.eventbus.EventBus
import org.vertx.java.core.eventbus.Message
import org.vertx.mods.Format
import org.vertx.mods.VertxCommand

import java.util.concurrent.atomic.AtomicReference

@Usage("Interact with the vert.x event bus")
public class bus extends VertxCommand {

  @Usage("Send a message on the bus")
  @Command
  public void send(
      @Usage("The address to send to")
      @Argument(name =  "address")
      @Required String address,
      @Usage("The message format")
      @Option(names = ["f","format"])
      Format format,
      @Usage("Wait for a reply and publish it on the console")
      @Option(names= ["r","reply"])
      Boolean reply,
      @Usage("The message")
      @Argument(name =  "message", unquote = false)
      @Required List<String> parts) {
    String value = join(parts);
    EventBus bus = getVertx().eventBus();
    if (reply) {
      final AtomicReference<Message> responseRef = new AtomicReference<Message>(null);
      def replyHandler = new Handler<Message<Object>>() {
        void handle(Message message) {
          synchronized (responseRef) {
            responseRef.set(message);
            responseRef.notifyAll();
          }
        }
      }
      (format?:Format.STRING).send(bus, address, value, replyHandler);
      synchronized (responseRef) {
        if (responseRef.get() == null) {
          try {
            responseRef.wait();
          }
          catch (InterruptedException cancelled) {
          }
        }
      }
      if (responseRef.get() != null) {
        Message<Object> response = responseRef.get();
        out << response.body;
      }
    } else {
      (format?:Format.STRING).send(bus, address, value, null);
    }
  }

  @Usage("Publish a JSON object as a message")
  @Command
  public void publish(
      @Usage("The address to send to")
      @Argument(name =  "address")
      @Required String address,
      @Option(names = ["f","format"])
      Format format,
      @Usage("The message")
      @Argument(name =  "message", unquote = false)
      @Required List<String> parts) {
    String value = join(parts);
    EventBus bus = getVertx().eventBus();
    (format?:Format.STRING).publish(bus, address, value);
  }

  @Usage("Read message from the bus")
  @Command
  public void subscribe(
      @Usage("The address to receive from")
      @Argument(name =  "addresses")
      @Required List<String> addresses) {

    //
    def bus = getVertx().eventBus()
    Map<Handler<Message>, String> handlers = [:]

    //
    try {

      // Create and register handlers
      addresses.each { address ->
        def handler = new Handler<Message>() {
          public void handle(Message message) {
            out.print(address);
            out.print(':');
            out.println(message.body);
            out.flush();
          }
        }
        handlers[handler] = address;
        bus.registerHandler(address, handler);
      }

      // Block until ctrl-c
      def o = new Object()
      synchronized (o) {
        o.wait();
      }
    }
    catch (InterruptedException ignore) {
      // Done
    }
    finally {
      handlers.each { handler, address ->
        bus.unregisterHandler(address, handler);
      }
    }
  }
}