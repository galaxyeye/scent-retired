package org.qiwur.scent.net;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.Connection.Response;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
  private final String cacheDir;
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
    } catch (IOException | InterruptedException | NoProxyException e) {
      logger.info("can not fetch {}, {}", url, e);
    }

    return doc;
  }

  protected Document doLoad(String uri) throws IOException, InterruptedException, NoProxyException {
    // remote uri
    // cache mechanism
    File file = new File(FileUtil.getFileForPage(uri, cacheDir, "original.html"));
    if (checkLocalCacheAvailable(file, localFileCacheExpires)) {
      uri = "file://" + file.getAbsolutePath();
      logger.debug("load from local file cache : {}", uri);
    }

    // load from local cache
    if (uri.startsWith("file://")) {
      return Jsoup.parse(new File(uri.substring("file://".length())), "utf-8", uri);
    }

    return parse(fetch(uri));
  }

  protected Response fetch(final String uri) throws IOException, InterruptedException, NoProxyException {
    Response response = null;
    ProxyEntry proxy = null;
    boolean fetchSuccess = false;

    try {
      if (useProxyPool) {
        proxy = proxyPool.poll();
        if (proxy == null) {
          throw new NoProxyException("proxy pool exhausted");
        }

        proxyHost = proxy.host();
        proxyPort = proxy.port();

        String message = String.format("proxy : %s, available : %d, retired : %d, uri : %s", 
            proxy.ipPort(), proxyPool.size(), proxyPool.retiredSize(), uri);

        logger.debug(message);
      }
  
      if (useProxy) {
        // response = Jsoup.connect(uri, proxyHost, proxyPort).execute();
      }
      else {
        response = Jsoup.connect(uri).execute();
      }

      if (localFileCacheExpires > 0) {
        cachePage(uri, response.body(), cacheDir);
      }
  
      if (response != null) {
        fetchSuccess = true;

        if (response.body().length() < minPageLength) {
          logger.info("get just {} bytes from {}, ignore", response.body().length(), uri);
          response = null;
        } else {
          logger.debug("received {} bytes from {}", response.body().length(), uri);
        }
      }
    }
    finally {
      if (useProxyPool && proxy != null) {
        // put back the proxy resource, this is essential important!
        if (fetchSuccess) {
          logger.debug("put back proxy {}", proxy.ipPort());

          proxyPool.put(proxy);
        }
        else {
          logger.debug("retire proxy {}", proxy.ipPort());

          // the proxy may be usable later
          proxyPool.retire(proxy);
        }
      }
    }

    return response;
  }

  protected Document parse(Response res) {
    Document doc = null;

    try {
      if (res != null) doc = res.parse();
    } catch (IOException e) {
      logger.error(e);
    }

    return doc;
  }

  private void cachePage(String url, String content, String dir) {
    try {
      File file = FileUtil.createFileForPage(url, dir, "html");
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
