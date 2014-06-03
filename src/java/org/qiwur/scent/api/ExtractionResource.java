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
import org.qiwur.scent.data.builder.ProductHTMLBuilder;
import org.qiwur.scent.data.extractor.DataExtractorFactory;
import org.qiwur.scent.data.extractor.DataExtractorNotFound;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.PageExtractorFactory;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.nodes.Document;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

public class ExtractionResource extends ServerResource {
  private static final Logger logger = LogManager.getLogger(ExtractionResource.class);

  public static final String PATH = "extract";
  public static final String DESCR = "Service extraction actions";

  private final Configuration conf;
  private final WebExtractor extractor;
  private final DataExtractorFactory dataExtractorFactory;

  public ExtractionResource() {
    this.conf = ScentApp.server.conf;
    dataExtractorFactory = new DataExtractorFactory(conf);
    extractor = WebExtractor.create(conf);
  }

  @Get("json|xml|html")
  public Object execute() throws DataExtractorNotFound {
    String cmd = (String) getRequestAttributes().get(Params.CMD);
    String args = (String) getRequestAttributes().get(Params.ARGS);

    if ("extract".equalsIgnoreCase(cmd)) {
      String url = getQuery().getValues("target");
      Document doc = extractor.getWebLoader().load(url);

      if (doc == null) {
        return "invalid doc";
      }

      PageExtractor extractorImpl = new PageExtractorFactory(conf).getExtractor("product", doc);
      PageEntity pageEntity = extractor.extract(extractorImpl);

      logger.debug(pageEntity);

      ProductHTMLBuilder builder = new ProductHTMLBuilder(pageEntity, conf);
      builder.process();

      return builder.doc().toString();
      // return extractor.generateWiki(pageEntity, Page.ProductPage);
    } else if ("statistics".equalsIgnoreCase(cmd)) {
      // new BlockVarianceCalculator(doc, conf).process();
    } else if ("segment".equalsIgnoreCase(cmd)) {

    } else if ("tag".equalsIgnoreCase(cmd)) {

    } else if ("wikilize".equalsIgnoreCase(cmd)) {

    } else if ("config".equalsIgnoreCase(cmd)) {
      return cmd;
    } else {
    }

    return "Unknown command " + cmd;
  }

}
