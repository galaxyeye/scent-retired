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
import org.qiwur.scent.classifier.statistics.BlockVarianceCalculator;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.ProductExtractor;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.data.extractor.WebExtractorFactory;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.wiki.Page;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;

public class ExtractionResource extends ServerResource {
  private static final Logger logger = LogManager.getLogger(ExtractionResource.class);

  public static final String PATH = "extract";
  public static final String DESCR = "Service extraction actions";

  private final Configuration conf;
  private final WebExtractor extractor;

  public ExtractionResource() {
    this.conf = ScentApp.server.conf;
    extractor = new WebExtractorFactory(conf).getWebExtractor();
  }

  @Get("json|xml")
  public Object execute() {
    String cmd = (String) getRequestAttributes().get(Params.CMD);
    String args = (String) getRequestAttributes().get(Params.ARGS);

    String url = args;
    Document doc = extractor.getWebLoader().load(url);

    if (doc == null) {
      return "invalid doc";
    }

    if ("extract".equalsIgnoreCase(cmd)) {
      PageExtractor extractorImpl = new ProductExtractor(doc, conf);
      PageEntity pageEntity = extractor.extract(extractorImpl);

      return "";
      // return extractor.generateWiki(pageEntity, Page.ProductPage);
    } else if ("statistics".equalsIgnoreCase(cmd)) {
      new BlockVarianceCalculator(doc, conf).process();
    } else if ("segment".equalsIgnoreCase(cmd)) {

    } else if ("tag".equalsIgnoreCase(cmd)) {

    } else if ("wikilize".equalsIgnoreCase(cmd)) {

    } else if ("config".equalsIgnoreCase(cmd)) {
      return cmd;
    } else {
    }

    return "Unknown command " + cmd;
  }

  @Post("json")
  public Object acceptPost(String content) {
    String cmd = (String) getRequestAttributes().get(Params.CMD);
    return cmd;
  }
}
