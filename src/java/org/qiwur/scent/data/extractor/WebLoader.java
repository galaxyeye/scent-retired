package org.qiwur.scent.data.extractor;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.Connection.Response;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.net.ProxyEntry;
import org.qiwur.scent.net.ProxyPool;
import org.qiwur.scent.net.ProxyPoolFactory;
import org.qiwur.scent.utils.FileUtil;

public class WebLoader {

  public static final Logger logger = LogManager.getLogger(WebLoader.class);

  private final Configuration conf;

  private String proxyHost = "";
  private boolean useProxy = false;
  private boolean useProxyPool = false;
  private int proxyPort = 19080;
  private ProxyPool proxyPool = null;
  private int minPageLength = 2000;
  private final long localFileCacheExpires;

  public WebLoader(Configuration conf) {
    this.conf = conf;

    useProxyPool = conf.getBoolean("scent.net.proxy.use.proxy.pool", false);
    if (useProxyPool) {
      proxyPool = new ProxyPoolFactory(conf).getProxyPool();
      useProxy = true;
    }
    else {
      proxyHost = conf.get("scent.net.proxy.host", null);
      if (!proxyHost.isEmpty()) useProxy = true;
      proxyPort = conf.getInt("scent.net.proxy.port", 19080);
    }

    minPageLength = conf.getInt("scent.page.length.min", 2000);
    localFileCacheExpires = conf.getLong("scent.local.file.cache.expires", 3 * 60 * 1000);
  }

  public Document load(String uri) {
    Document doc = null;

    try {
      doc = doLoad(uri);
    } catch (IOException | InterruptedException e) {
      logger.info("can not fetch {}, {}", uri, e);
    }

    return doc;
  }

  protected Document doLoad(String uri) throws IOException, InterruptedException {
    // cache mechanism
    File file = new File(FileUtil.getFileNameFromUri(uri));
    if (checkLocalCacheAvailable(file, localFileCacheExpires)) {
      uri = "file://" + file.getAbsolutePath();
    }

    if (uri.startsWith("file://")) {
      return Jsoup.parse(file, "utf-8");
    }
    else {
      return parse(fetch(uri));
    }
  }

  protected Response fetch(String uri) throws IOException, InterruptedException {
    Response response = null;

    if (useProxyPool) {
      ProxyEntry proxy = proxyPool.poll();
      proxyHost = proxy.host();
      proxyPort = proxy.port();
    }

    if (useProxy) {
      response = Jsoup.connect(uri, proxyHost, proxyPort).execute();
    }
    else {
      response = Jsoup.connect(uri).execute();
    }

    if (localFileCacheExpires > 0) {
      cachePage(uri, response.body(), "original");
    }

    if (response != null) {
      if (response.body().length() < minPageLength) {
        logger.info("get just {} bytes from {}, ignore", response.body().length(), uri);
        response = null;
      } else {
        logger.debug("received {} bytes from {}", response.body().length(), uri);
      }
    }

    return response;
  }

  protected Document parse(Response res) {
    Document doc = null;

    try {
      if (res != null)
        doc = res.parse();
    } catch (IOException e) {
      logger.error(e);
    }

    return doc;
  }

  private void cachePage(String url, String content, String dir) {
    try {
      File file = FileUtil.createTempFileForPage(url, "web/" + dir);
      FileUtils.writeStringToFile(file, content);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private boolean checkLocalCacheAvailable(File file, long expires) {
    if (expires > 0 && file.exists()) {
      long modified = file.lastModified();
      if (System.currentTimeMillis() - modified < expires) {
        return true;
      }
      else {
        file.delete();
      }
    }

    return false;
  }
}
