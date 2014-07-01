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
<<<<<<< HEAD
  private final String cacheDir;
=======
  private String cacheDir = "/tmp/web/original";
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
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
    cacheDir = conf.get("scent.web.cache.file.dir", "/tmp/web");
    localFileCacheExpires = conf.getLong("scent.web.cache.file.expires", 3 * 60 * 1000);
  }

  public Document load(final String url) {
    Document doc = null;

    try {
      doc = doLoad(url);
    } catch (IOException | InterruptedException e) {
      logger.info("can not fetch {}, {}", url, e);
    }

    return doc;
  }

  protected Document doLoad(final String url) throws IOException, InterruptedException {
    String uri = url;

    // cache mechanism
<<<<<<< HEAD
    File file = new File(FileUtil.getFileForPage(uri, cacheDir, "html"));
=======
    File file = new File(FileUtil.getFileForPage(uri, cacheDir));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    if (checkLocalCacheAvailable(file, localFileCacheExpires)) {
      uri = "file://" + file.getAbsolutePath();
      logger.debug("load from local file cache : {}", uri);
    }

    if (uri.startsWith("file://")) {
      return Jsoup.parse(file, "utf-8", url);
    }
    else {
      return parse(fetch(uri));
    }
  }

  protected Response fetch(final String uri) throws IOException, InterruptedException {
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
      cachePage(uri, response.body(), cacheDir);
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
<<<<<<< HEAD
      File file = FileUtil.createFileForPage(url, dir, "html");
=======
      File file = FileUtil.createFileForPage(url, dir);
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
      FileUtils.writeStringToFile(file, content);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  private boolean checkLocalCacheAvailable(File file, long expires) {
    if (expires > 0 && file.exists()) {
      long modified = file.lastModified();

//      logger.debug("check expired : {}, {}, {}, {}", 
//          System.currentTimeMillis(), modified, System.currentTimeMillis() - modified, expires);

      if (System.currentTimeMillis() - modified < 1000 * expires) {
        return true;
      }
      else {
        file.delete();
      }
    }

    return false;
  }
}
