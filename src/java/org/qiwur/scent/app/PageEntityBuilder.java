package org.qiwur.scent.app;

import java.io.File;
import java.io.IOException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.OptionBuilder;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.PosixParser;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.data.builder.EntityBuilder;
import org.qiwur.scent.data.builder.ProductWikiBuilder;
import org.qiwur.scent.data.extractor.DataExtractorNotFound;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.learning.WordsLearnerFactory;
import org.qiwur.scent.utils.FetchListManager;
import org.qiwur.scent.utils.FileUtil;
import org.qiwur.scent.wiki.Page;

public class PageEntityBuilder {

  private static final Logger logger = LogManager.getLogger(PageEntityBuilder.class);

  private final Configuration conf;
  private final String targetUri;
  private final boolean generateFetchList;
  private final WebExtractor extractor;
  private final boolean cachePage;
  private final boolean cacheWiki;
  private final boolean uploadWiki;
  private final String cacheDir;

  private FetchListManager fetchListManager = new FetchListManager();

  PageEntityBuilder(String targetUri, Configuration conf) {
    this.conf = ScentConfiguration.create();

    this.targetUri = targetUri;
    this.generateFetchList = conf.getBoolean("scent.net.generate.fetch.list", false);

    this.cachePage = conf.getBoolean("scent.page.cache", false);
    this.uploadWiki = conf.getBoolean("scent.wiki.upload", false);
    this.cacheWiki = conf.getBoolean("scent.wiki.cache", false);
    this.cacheDir = conf.get("scent.web.cache.file.dir", "/tmp/web");

    this.extractor = WebExtractor.create(conf);
  }

  public void process() {
    if (targetUri != null) {
      try {
        logger.debug(targetUri);

        processPage(targetUri);
      } catch (IOException | DataExtractorNotFound e) {
        logger.error(e);
      }

      return;
    }

    for (String uri : fetchListManager.detailUrls()) {
      try {
        logger.debug(uri);

        processPage(uri);
      } catch (IOException | DataExtractorNotFound e) {
        logger.error(e);
      }
    }

    if (generateFetchList) {
      fetchListManager.saveUrls();
    }

    new WordsLearnerFactory(conf).getWordsLearner().save();
  }

  // 支持远程URL和本地文件
  public void processPage(String uri) throws IOException, DataExtractorNotFound {
    // 配置选项：忽略已处理过的链接
    if (generateFetchList && fetchListManager.checkProcessed(uri)) {
      return;
    }

    Document doc = extractor.getWebLoader().load(uri);
    if (doc == null) {
      logger.info("bad uri {}", uri);
      return;
    }

    if (generateFetchList) {
      fetchListManager.addProcessed(uri);
    }

    long time = System.currentTimeMillis();

    PageEntity pageEntity = extractor.extract(new PageExtractor(doc, conf));
    cache(doc.baseUri(), pageEntity.toString(), "log");

    time = System.currentTimeMillis() - time;
    logger.info("网页转换耗时 : {}s\n\n", time / 1000.0);

    Document html = generateHtml(pageEntity);

    cache(doc.baseUri(), html.toString(), "generated.html");

//    Page wiki = generateWiki(pageEntity, Page.ProductPage);
//    cache(doc.baseUri(), wiki.text(), "wiki");

    // if (uploadWiki) wiki.upload();
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
      FileUtils.write(file, content);

      logger.debug("saved in {}", file.getAbsoluteFile());
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private static Options createOptions() {
    Options options = new Options();

    OptionBuilder.withDescription("The url of the target page to extract.");
    OptionBuilder.hasOptionalArg();
    OptionBuilder.withArgName("page url");
    options.addOption(OptionBuilder.create("url"));

    return options;
  }

  public static void main(String[] args) throws IOException, DataExtractorNotFound, ParseException {
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
