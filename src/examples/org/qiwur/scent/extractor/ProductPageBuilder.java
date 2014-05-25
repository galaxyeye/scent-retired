package org.qiwur.scent.extractor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.configuration.ScentConfiguration;
import org.qiwur.scent.data.builder.ProductHTMLBuilder;
import org.qiwur.scent.data.builder.ProductWikiBuilder;
import org.qiwur.scent.data.extractor.PageExtractor;
import org.qiwur.scent.data.extractor.ProductExtractor;
import org.qiwur.scent.data.extractor.WebExtractor;
import org.qiwur.scent.data.extractor.WebExtractorFactory;
import org.qiwur.scent.data.extractor.WebLoader;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.Connection.Response;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.learning.WordsLearnerFactory;
import org.qiwur.scent.utils.FetchListManager;
import org.qiwur.scent.utils.FileUtil;
import org.qiwur.scent.wiki.Page;

public class ProductPageBuilder {

  private static final Logger logger = LogManager.getLogger(ProductPageBuilder.class);

  private final Configuration conf;
  private final boolean GenerateFetchList;
  private final WebExtractor extractor;
  private final boolean savePage;
  private final boolean saveWiki;
  private final boolean uploadWiki;

  private FetchListManager fetchListManager = new FetchListManager();

  ProductPageBuilder(Configuration conf) {
    this.conf = ScentConfiguration.create();

    GenerateFetchList = conf.getBoolean("scent.net.generate.fetch.list", false);

    savePage = conf.getBoolean("scent.page.save", false);
    uploadWiki = conf.getBoolean("scent.wiki.upload", false);
    saveWiki = conf.getBoolean("scent.wiki.save", false);

    extractor = new WebExtractorFactory(conf).getWebExtractor();
  }

  public void process() {
    try {
      for (String uri : fetchListManager.detailUrls()) {
        try {
          processPage(uri);
        } catch (IOException e) {
          logger.error(e);
        }
      }
    }
    finally {
      if (GenerateFetchList) {
        fetchListManager.saveUrls();
      }

      new WordsLearnerFactory(conf).getWordsLearner().save();      
    }
  }

  // 支持远程URL和本地文件
  public void processPage(String uri) throws IOException {
    Document doc = null;

    // 配置选项：忽略已处理过的链接
    if (GenerateFetchList && fetchListManager.checkProcessed(uri)) {
      return;
    }

    if (uri.startsWith("http")) {
      WebLoader loader = extractor.getWebLoader();

      Response response = loader.fetch(uri);
      if (response == null)
        return;

      if (savePage)
        savePage(uri, response.body());

      doc = loader.parse(response);

      if (doc != null && GenerateFetchList) {
        fetchListManager.extractLinks(doc);
      }
    } else if (uri.startsWith("file://")) {
      doc = Jsoup.parse(new File(uri.substring("file://".length())), "utf-8");
    }

    if (GenerateFetchList) {
      fetchListManager.addProcessed(uri);
    }

    if (doc != null) {
      long time = System.currentTimeMillis();

      PageExtractor extractorImpl = new ProductExtractor(doc, conf);
      PageEntity pageEntity = extractor.extract(extractorImpl);
      logger.info(pageEntity);

      time = System.currentTimeMillis() - time;
      logger.info("网页转换耗时 : {}s\n\n", time / 1000.0);

      Document page = generateHtml(pageEntity);
      savePage(page.baseUri(), page.toString());

      // Page page = generateWiki(product, Page.ProductPage);
      //
      // if (saveWiki) saveWiki(page);
      // if (uploadWiki) uploadWiki(page);
    }
  }

  public void savePage(String url, String content) {
    try {
      File file = FileUtil.createTempFileForPage(url, "web");
      FileUtils.writeStringToFile(file, content);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public void saveWiki(Page page) {
    PrintWriter writer = null;

    try {
      writer = new PrintWriter(getOutputFile(page.title()), "UTF-8");
    } catch (FileNotFoundException | UnsupportedEncodingException e) {
      logger.error(e);
    }

    if (writer != null) {
      writer.write(page.text());
      writer.close();
    }
  }

  public void uploadWiki(Page page) {
    page.upload();
  }

  private String getOutputFile(String title) {
    return "output/wiki/" + DigestUtils.md5Hex(title);
  }

  private Page generateWiki(PageEntity pageEntity, String pageType) {
    Validate.notNull(pageEntity);

    return new ProductWikiBuilder(pageEntity).build(pageType);
  }

  private Document generateHtml(PageEntity pageEntity) {
    Validate.notNull(pageEntity);

    ProductHTMLBuilder builder = null;
    try {
      builder = new ProductHTMLBuilder(pageEntity);
      builder.process();
      return builder.doc();
    } catch (IOException e) {
      logger.error(e);
    }

    return null;
  }

  public static void main(String[] args) throws IOException {
    Configuration conf = ScentConfiguration.create();
    ProductPageBuilder builder = new ProductPageBuilder(conf);
    builder.process();
  }
}
