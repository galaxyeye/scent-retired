package org.qiwur.scent.api;

import java.util.Set;
import java.util.logging.Level;

import javax.ws.rs.core.Application;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.net.ProxyUpdateThread;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.data.Protocol;
import org.restlet.ext.jaxrs.JaxRsApplication;
import org.restlet.service.StatusService;

import com.google.common.collect.Sets;

public class ScentServer extends Application {
  public static final String SCENT_SERVER = "SCENT_SERVER";
  public static final String SCENT_CONFIGURATION = "SCENT_CONFIGURATION";

  public static final Logger LOG = LoggerFactory.getLogger(ScentServer.class);

  private static final String DEFAULT_LOG_LEVEL = "INFO";
  private static final Integer DEFAULT_PORT = 8282;

  private static String logLevel = DEFAULT_LOG_LEVEL;
  private static Integer port = DEFAULT_PORT;

  private final Configuration conf;

  private Component component;

  private long startTime = 0;
  private boolean running = false;

  /**
   * Public constructor which accepts the port we wish to run the server on as
   * well as the logging granularity. If the latter option is not provided via
   * {@link org.apache.ScentServer.fetcher.server.FetchServer#main(String[])} then it defaults to
   * 'INFO' however best attempts should always be made to specify a logging
   * level.
   */
  public ScentServer(int port, Configuration conf) {
    this.conf = conf;

    // Create a new Component.
    component = new Component();
    component.getLogger().setLevel(Level.parse(logLevel));

    // Add a new HTTP server listening on defined port.
    component.getServers().add(Protocol.HTTP, port);

    Context childContext = component.getContext().createChildContext();
    JaxRsApplication application = new JaxRsApplication(childContext);
    application.add(this);
    application.setStatusService(new StatusService());
    childContext.getAttributes().put(SCENT_SERVER, this);
    childContext.getAttributes().put(SCENT_CONFIGURATION, conf);

    // Attach the application.
    component.getDefaultHost().attach(application);
  }

  @Override
  public Set<Class<?>> getClasses() {
    Set<Class<?>> resources = Sets.newHashSet();

    resources.add(AdminResource.class);
    resources.add(ScentResource.class);

    return resources;
  }

  public long getStartTime() {
    return startTime;
  }

  /**
   * Convenience method to determine whether a Nutch server is running.
   * 
   * @return true if a server instance is running.
   */
  public boolean isRunning() {
    return running;
  }

  /**
   * Starts the Nutch server printing some logging to the log file.
   * 
   */
  public void start() {
    LOG.info("Starting FetcherServer on port: {} with logging level: {} ...",
        port, logLevel);
    try {
      component.start();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot start server!", e);
    }
    LOG.info("Started FetcherServer on port {}", port);
    running = true;
    startTime = System.currentTimeMillis();
  }

  /**
   * Stop the Fetch server.
   * 
   * @param force
   *          boolean method to effectively kill jobs regardless of state.
   * @return true if no server is running or if the shutdown was successful.
   *         Return false if there are running jobs and the force switch has not
   *         been activated.
   */
  public boolean stop() {
    if (!running) {
      return true;
    }

    LOG.info("Stopping FetchServer on port {}...", port);
    try {
      component.stop();
    } catch (Exception e) {
      throw new IllegalStateException("Cannot stop nutch server", e);
    }
    LOG.info("Stopped FetchServer on port {}", port);
    running = false;
    return true;
  }

  public Configuration getConf() {
    return this.conf;
  }
  
  public static void main(String[] args) throws Exception {
    Configuration conf = ScentConfiguration.create();

    if (conf.getBoolean("scent.net.proxy.use.proxy.pool", false)) {
      ProxyUpdateThread updateThread = new ProxyUpdateThread(conf);
      updateThread.runAsDaemon();
    }

    int port = conf.getInt("scent.server.port", DEFAULT_PORT);
    ScentServer server = new ScentServer(port, conf);
    server.start();
  }
}
