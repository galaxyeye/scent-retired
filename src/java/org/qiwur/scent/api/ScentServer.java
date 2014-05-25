/*******************************************************************************
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/
package org.qiwur.scent.api;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.net.ProxyUpdateThread;
import org.restlet.Component;
import org.restlet.data.Protocol;

public class ScentServer {
  private static final Logger logger = LogManager.getLogger(ScentServer.class);
  public final Configuration conf;

  private Component component;
  private ScentApp app;
  private int port;
  private boolean running;

  public ScentServer(int port) {
    conf = ScentConfiguration.create();

    this.port = port;
    component = new Component();

    component.getServers().add(Protocol.HTTP, port);

    // Attach the application.
    app = new ScentApp();
    component.getDefaultHost().attach("/scent", app);
    ScentApp.server = this;
  }

  public boolean isRunning() {
    return running;
  }

  public void start() throws Exception {
    logger.info("Starting NutchServer on port " + port + "...");
    component.start();
    logger.info("Started NutchServer on port " + port);
    running = true;
    ScentApp.started = System.currentTimeMillis();
  }

  public boolean canStop() throws Exception {
    return true;
  }

  public boolean stop(boolean force) throws Exception {
    if (!running) {
      return true;
    }

    if (!canStop() && !force) {
      logger.warn("Running jobs - can't stop now.");
      return false;
    }

    logger.info("Stopping NutchServer on port " + port + "...");
    component.stop();
    logger.info("Stopped NutchServer on port " + port);
    running = false;
    return true;
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = ScentConfiguration.create();

    if (conf.getBoolean("scent.net.proxy.use.proxy.pool", false)) {
      ProxyUpdateThread updateThread = new ProxyUpdateThread(conf);
      updateThread.runAsDaemon();
    }

    int port = conf.getInt("scent.server.port", 8282);
    ScentServer server = new ScentServer(port);
    server.start();
  }
}
