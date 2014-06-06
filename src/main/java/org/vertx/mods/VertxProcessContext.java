package org.vertx.mods;

import org.crsh.shell.Shell;
import org.crsh.shell.ShellProcess;
import org.crsh.shell.ShellProcessContext;
import org.crsh.shell.ShellResponse;
import org.crsh.text.ScreenBuffer;
import org.crsh.text.Screenable;
import org.crsh.text.Style;
import org.crsh.text.Format;
import org.vertx.java.core.eventbus.EventBus;

import java.io.IOException;
import java.util.LinkedList;

/**
 * @author Julien Viet
 */
public class VertxProcessContext implements ShellProcessContext {

  /** . */
  private final ScreenBuffer screen = new ScreenBuffer();

  /** . */
  private final LinkedList<String> requests;

  /** . */
  private final Shell shell;

  /** . */
  private final String replyTo;

  /** . */
  private final EventBus bus;

  public VertxProcessContext(EventBus bus, Shell shell, LinkedList<String> requests, String replyTo) {
    this.bus = bus;
    this.replyTo = replyTo;
    this.shell = shell;
    this.requests = requests;
  }

  public void run() {
    if (requests.size()  > 0) {
      ShellProcess process = shell.createProcess(requests.peekFirst());
      process.execute(this);
    }
  }

  @Override
  public void end(ShellResponse response) {

    // Remove what we executed
    String request = requests.removeFirst();

    //
    if (replyTo != null) {
      try {
        // For now render to text
        StringBuilder buffer = new StringBuilder();

        //
        buffer.append(request).append(":\n");

        // For now render to text
        screen.format(Format.TEXT, buffer);

        // Append response message if any
        String msg = response.getMessage();
        if (msg != null) {
          buffer.append(msg);
        }

        // Publish message
        bus.publish(replyTo, buffer.toString());
      }
      catch (IOException e) {
        e.printStackTrace();
      }
    }

    // Should be a "reset" but for now we don't care about style
    screen.clear();

    // Execute next if any
    run();
  }

  @Override
  public boolean takeAlternateBuffer() throws IOException {
    return false;
  }

  @Override
  public boolean releaseAlternateBuffer() throws IOException {
    return false;
  }

  @Override
  public String getProperty(String propertyName) {
    return null;
  }

  @Override
  public String readLine(String msg, boolean echo) throws IOException, InterruptedException, IllegalStateException {
    return null;
  }

  @Override
  public int getWidth() {
    return 80;
  }

  @Override
  public int getHeight() {
    return 40;
  }

  @Override
  public Appendable append(char c) throws IOException {
    screen.append(c);
    return this;
  }

  @Override
  public Appendable append(CharSequence s) throws IOException {
    screen.append(s);
    return this;
  }

  @Override
  public Appendable append(CharSequence csq, int start, int end) throws IOException {
    screen.append(csq, start, end);
    return null;
  }

  @Override
  public Screenable append(Style style) throws IOException {
    screen.append(style);
    return this;
  }

  @Override
  public Screenable cls() throws IOException {
    screen.cls();
    return this;
  }

  @Override
  public void flush() throws IOException {
  }
}
