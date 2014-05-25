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

import org.restlet.Application;
import org.restlet.Restlet;
import org.restlet.routing.Router;

public class ScentApp extends Application {
  public static ScentServer server;
  public static long started;

  /**
   * Creates a root Restlet that will receive all incoming calls.
   */
  @Override
  public synchronized Restlet createInboundRoot() {
      getTunnelService().setEnabled(true);
      getTunnelService().setExtensionsTunnel(true);
      Router router = new Router(getContext());
      //router.getLogger().setLevel(Level.FINEST);

      // admin
      router.attach("/", APIInfoResource.class);
      router.attach("/" + AdminResource.PATH, AdminResource.class);
      router.attach("/" + AdminResource.PATH + "/{" + Params.CMD + "}", AdminResource.class);

      // jobs
      router.attach("/" + ExtractionResource.PATH, ExtractionResource.class);
      router.attach("/" + ExtractionResource.PATH + "/{" + Params.CMD + "}", ExtractionResource.class);
      router.attach("/" + ExtractionResource.PATH + "/{" + Params.CMD + "}/{" + Params.ARGS + "}", ExtractionResource.class);

      return router;
  }
}
