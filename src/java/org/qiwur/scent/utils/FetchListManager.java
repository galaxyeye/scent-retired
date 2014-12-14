package org.qiwur.scent.utils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import com.google.common.collect.Multiset;

/**
 * Example program to list links from a URL.
 */
public class FetchListManager {

  private static final Logger logger = LogManager.getLogger(FetchListManager.class);

  public static final String SeedUrlFile = "conf/seeds.txt";
  public static final String IndexUrlFile = "output/urls/index.txt";
  public static final String DetailUrlFile = "output/urls/detail.txt";
  public static final String ProcessedUrlFile = "output/urls/processed.txt";

  FiledLines lines = null;

  public FetchListManager() {
    init();
  }

  public static void generateFromSeeds() {
    logger.info("generate urls from seeds");

    FetchListManager extractor = new FetchListManager();

    for (String seed : extractor.lines.getLines(SeedUrlFile)) {
      try {
        extractor.fetchAndExtractLinks(seed);
      } catch (IOException e) {
        logger.error(e);
      }
    }

    extractor.saveUrls();
  }

  private void init() {
    lines = new FiledLines(SeedUrlFile, IndexUrlFile, DetailUrlFile, ProcessedUrlFile);
  }

  public Multiset<String> seeds() {
    return lines.getLines(SeedUrlFile);
  }

  public Multiset<String> detailUrls() {
    return lines.getLines(DetailUrlFile);
  }

  public void extractLinks(Document doc) {
    if (doc == null)
      return;

    Elements linkElements = doc.select("a[href]");

    for (Element linkElement : linkElements) {
      String u = linkElement.attr("href");
      if (u.contains("#")) {
        u = u.substring(0, u.indexOf('#'));
      }

      if (isDetailPageLink(u)) {
        lines.add(DetailUrlFile, u);
      } else if (isIndexPageLink(u)) {
        lines.add(IndexUrlFile, u);
      }
    }
  }

  public boolean checkProcessed(String url) {
    return lines.contains(ProcessedUrlFile, url);
  }

  public void addProcessed(String url) {
    lines.remove(IndexUrlFile, url);
    lines.remove(DetailUrlFile, url);
    lines.add(ProcessedUrlFile, url);
  }

  public void saveUrls() {
    try {
      // 写之前先清空原来的文件
      new FileOutputStream(new File(IndexUrlFile), true);
      new FileOutputStream(new File(DetailUrlFile), true);

      lines.saveAll();
    } catch (IOException e) {
      logger.error(e);
    }
  }

  public void fetchAndExtractLinks(String seed) throws IOException {
    logger.info("fetch and extract link : {}", seed);

    Document doc = getDocument(seed);

    extractLinks(doc);
  }

  private boolean isIndexPageLink(String link) {
    if (StringUtils.isEmpty(link))
      return false;

    boolean yes = link.contains("searchex.") | link.contains("list.") | link.contains("jiadian.")
        | link.contains("page=");

    return yes;
  }

  private boolean isDetailPageLink(String link) {
    if (StringUtils.isEmpty(link))
      return false;

    boolean yes = link.contains("item.") | link.contains("detail.") | link.contains("product.")
        | link.matches("\\d{5,10}[\\./]");

    return yes;
  }

  private Document getDocument(String seed) throws IOException {
    Response res = Jsoup.connect(seed).execute();
    if (res != null && res.bodyAsBytes().length < 1000) {
      logger.info("Invalid response, just got %d characters", res.bodyAsBytes().length);
      return null;
    }
    Document doc = res.parse();

    return doc;
  }

  public static void main(String[] args) throws IOException {
    FetchListManager.generateFromSeeds();
  }
}
