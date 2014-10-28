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

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.data.builder.ProductHTMLBuilder;
import org.qiwur.scent.data.extractor.DataExtractorNotFound;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.utils.FileUtil;
import org.restlet.Context;
import org.restlet.resource.ServerResource;

@Path("/scent")
public class ScentResource extends ServerResource {
  private static final Logger logger = LogManager.getLogger(ScentResource.class);

  public static final String WWWROOT = "wwwroot";

  private final Configuration conf;
  private final String baseDir;
  private final WebExtractor extractor;

  public ScentResource() {
    this.conf = (Configuration) Context.getCurrent().getAttributes().get(ScentServer.SCENT_CONFIGURATION);

    this.baseDir = conf.get("scent.web.cache.file.dir", "/tmp/web");
    this.extractor = WebExtractor.create(conf);
  }

  @GET
  @Path("/")
  @Produces(javax.ws.rs.core.MediaType.TEXT_HTML)
  public String home() throws IOException {
    String homePage = WWWROOT + File.separator + "scent.html";
    return FileUtils.readFileToString(new File(homePage), Charset.forName("UTF-8"));
  }

  @GET
  @Path("/extract")
  @Produces(javax.ws.rs.core.MediaType.TEXT_HTML)
  public Object execute(@QueryParam("target") String target, @QueryParam("format") String format) {
    Document doc = extractor.getWebLoader().load(target);

    if (doc == null) {
      return "invalid doc";
    }

    long time = System.currentTimeMillis();

    // TODO : thread safe?
    PageEntity pageEntity = extractor.extract(new PageExtractor(doc, conf));

    logger.debug(pageEntity);

    time = System.currentTimeMillis() - time;
    logger.info("extraction time : {}s\n\n", time / 1000.0);

    if (format == null) format = "html";
    if (format.equals("txt")) {
      return "<pre>" + StringEscapeUtils.escapeHtml(buildText(pageEntity)) + "</pre>";
    }
    else if (format.equals("all")) {
      return buildAllHtml(pageEntity);
    }
    else if (format.equals("diagnosis")) {
      return getDiagnosis(target);
    }

    return buildHtml(pageEntity);
  }

  private String getDiagnosis(String url) {
    try {
      File file = new File(FileUtil.getFileForPage(url, baseDir, "diag"));
      return FileUtils.readFileToString(file, "utf-8");
    }
    catch (IOException e) {
      return "404 not found";
    }
  }

  private String buildText(PageEntity pageEntity) {
    ProductHTMLBuilder builder = new ProductHTMLBuilder(pageEntity, conf);
    builder.process();

    return builder.doc().toString();
  }

  private String buildAllHtml(PageEntity pageEntity) {
    ProductHTMLBuilder builder = new ProductHTMLBuilder(pageEntity, conf);
    builder.setFormat("All");
    builder.process();
    return builder.doc().toString();
  }

  private String buildHtml(PageEntity pageEntity) {
    ProductHTMLBuilder builder = new ProductHTMLBuilder(pageEntity, conf);
    builder.process();

    // logger.debug(builder.doc().toString());

    return builder.doc().toString();
  }
}
