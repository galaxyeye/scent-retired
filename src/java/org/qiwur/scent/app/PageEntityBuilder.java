package org.qiwur.scent.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.gora.query.Query;
import org.apache.gora.query.Result;
import org.apache.gora.store.DataStore;
import org.apache.gora.util.GoraException;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.nutch.storage.Bytes;
import org.apache.nutch.storage.StorageUtils;
import org.apache.nutch.storage.WebPage;
import org.qiwur.scent.data.builder.EntityBuilder;
import org.qiwur.scent.data.builder.ProductWikiBuilder;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.extractor.DataExtractorNotFound;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.data.wiki.Page;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.learning.WordsLearnerFactory;
import org.qiwur.scent.utils.FetchListManager;
import org.qiwur.scent.utils.FileUtil;
import org.qiwur.scent.utils.ScentConfiguration;

public class PageEntityBuilder {

  private static final Logger LOG = LogManager.getLogger(PageEntityBuilder.class);

  private final Configuration conf;
  private final String targetUri;
  private final boolean generateFetchList;
  private final String targetMode;
  private final WebExtractor extractor;
  private final boolean cachePage;
  private final boolean cacheWiki;
  private final boolean uploadWiki;
  private final String cacheDir;

  private DataStore<String, WebPage> goraStore = null;
  private FetchListManager fetchListManager = null;

  PageEntityBuilder(String targetUri, Configuration conf) {
    this.conf = ScentConfiguration.create();

    this.targetUri = targetUri;
    this.generateFetchList = conf.getBoolean("scent.net.generate.fetch.list", false);

    this.cachePage = conf.getBoolean("scent.page.cache", false);
    this.uploadWiki = conf.getBoolean("scent.wiki.upload", false);
    this.cacheWiki = conf.getBoolean("scent.wiki.cache", false);
    this.cacheDir = conf.get("scent.web.cache.file.dir", "/tmp/web");
    this.extractor = WebExtractor.create(conf);

    if (targetUri.startsWith("gora")) {
      targetMode = "gora";
    }
    else if (targetUri.startsWith("http")) {
      targetMode = "http";
    }
    else if (targetUri.startsWith("file")) {
      targetMode = "file";
    }
    else {
      targetMode = "fetch-list";
    }

    if (targetMode.equals("fetch-list")) {
      fetchListManager = new FetchListManager();
    }

    if (targetMode.equals("gora")) {
      try {
        goraStore = StorageUtils.createWebStore(conf, String.class, WebPage.class);

        System.out.println("Read records from table " + goraStore.getSchemaName());
      } catch (ClassNotFoundException | GoraException e) {
        LOG.error(e.toString());
      }
    }
  }

  public void process() throws Exception {
    // process the given uri
    if (targetMode.equals("http") || targetMode.equals("file")) {
      processDocument(readInternet(targetUri));
    }
    else if (targetMode.equals("gora")) {
      processGoraSource();

      return;
    }
    else {
      
    }

    // process uris from configuration
    for (String uri : fetchListManager.detailUrls()) {
      LOG.debug(uri);

      processDocument(readInternet(uri));

      if (generateFetchList) {
        fetchListManager.saveUrls();
      }
    }

    new WordsLearnerFactory(conf).getWordsLearner().save();
  }

  private void processGoraSource() throws IOException {
    long limit = 10;

    Query<String, WebPage> query = goraStore.newQuery();
    query.setLimit(limit);

    Result<String, WebPage> result = goraStore.execute(query);
    long count = 0;
    try {
      while (result.next() && ++count <= limit) {
        WebPage page = result.get();
        String contentType = page.getContentType().toString();
        if (contentType.contains("html")) {
          Document doc = Jsoup.parse(Bytes.toString(page.getContent()), page.getBaseUrl().toString());
          processDocument(doc);
        }

        if (page.getText() != null) {
          System.out.println(count + ".\t" + contentType
              + "\t" + page.getText().subSequence(0, 100));
        }
      }
    } catch (Exception e) {
      LOG.error(e.toString());
    }
    finally {
      if (result != null) {
        result.close();
      }
    }
  }

  private void processDocument(Document doc) throws IOException, DataExtractorNotFound {
    Validate.notNull(doc);

    long time = System.currentTimeMillis();

    PageEntity pageEntity = extractor.extract(new PageExtractor(doc, conf));
    cache(doc.baseUri(), pageEntity.toString(), "log");

    time = System.currentTimeMillis() - time;
    LOG.info("网页转换耗时 : {}s\n\n", time / 1000.0);

    Document html = generateHtml(pageEntity);

    cache(doc.baseUri(), html.toString(), "generated.html");

//    Page wiki = generateWiki(pageEntity, Page.ProductPage);
//    cache(doc.baseUri(), wiki.text(), "wiki");

    // if (uploadWiki) wiki.upload();
  }

  private Document readInternet(String uri) {
    // 配置选项：忽略已处理过的链接
    if (generateFetchList && fetchListManager.checkProcessed(uri)) {
      return null;
    }

    Document doc = extractor.getWebLoader().load(uri);
    if (doc == null) {
      LOG.info("bad uri {}", uri);
      return null;
    }

    if (generateFetchList) {
      fetchListManager.addProcessed(uri);
    }

    return doc;
  }

  private Page generateWiki(PageEntity pageEntity, String pageType) {
    Validate.notNull(pageEntity);

    return new ProductWikiBuilder(pageEntity, conf).build(pageType);
  }

  private Document generateHtml(PageEntity pageEntity) {
    Validate.notNull(pageEntity);

    EntityBuilder builder = new EntityBuilder(pageEntity, conf);
    builder.process();

    return builder.doc();
  }

  private void cache(String pageUri, String content, String suffix) {
    try {
      File file = new File(FileUtil.getFileForPage(pageUri, cacheDir, suffix));
      FileUtils.write(file, content, "utf-8");

      LOG.debug("saved in {}", file.getAbsoluteFile());
    } catch (IOException e) {
      LOG.error(e);
    }
  }

  private static Options createOptions() {
    Options options = new Options();

    OptionBuilder.withDescription("The url of the target page to extract.");
    OptionBuilder.hasOptionalArg();
    OptionBuilder.withArgName("page url");
    options.addOption(OptionBuilder.create("url"));

    OptionBuilder.withDescription("Read from hbase.");
    OptionBuilder.hasOptionalArg();
    OptionBuilder.withArgName("hbase");
    options.addOption(OptionBuilder.create("hbase"));

    return options;
  }

  public static void main(String[] args) throws Exception {
    Configuration conf = ScentConfiguration.create();

    CommandLineParser parser = new PosixParser();
    Options options = createOptions();
    CommandLine commandLine = parser.parse(options, args);

    if (commandLine.hasOption("help")) {
      HelpFormatter formatter = new HelpFormatter();
      formatter.printHelp("PageEntityBuilder", options, true);
      return;
    }

    String url = null;
    if (commandLine.hasOption("url")) {
      url = commandLine.getOptionValue("url");
    }

    PageEntityBuilder builder = new PageEntityBuilder(url, conf);
    builder.process();
  }
}
