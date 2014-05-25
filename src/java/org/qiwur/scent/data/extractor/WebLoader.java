package org.qiwur.scent.data.extractor;

import java.io.IOException;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.Connection.Response;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.net.ProxyEntry;
import org.qiwur.scent.net.ProxyPool;
import org.qiwur.scent.net.ProxyPoolFactory;

public class WebLoader {

  public static final Logger logger = LogManager.getLogger(WebLoader.class);

  private final Configuration conf;

  private String proxyHost = "";
  private boolean useProxy = false;
  private boolean useProxyPool = false;
  private int proxyPort = 19080;
  private ProxyPool proxyPool = null;
  private int minPageLength = 2000;

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
  }

  public Document load(String url) {
    return parse(fetch(url));
  }

  public Response fetch(String url) {
    Response response = null;

    try {
      if (useProxyPool) {
        ProxyEntry proxy = proxyPool.poll();
        proxyHost = proxy.host();
        proxyPort = proxy.port();
      }

      if (useProxy) {
        response = Jsoup.connect(url, proxyHost, proxyPort).execute();
      }
      else {
        response = Jsoup.connect(url).execute();
      }
    } catch (IOException e) {
      logger.info("can not fetch {}, {}", url, e);
    } catch (InterruptedException e) {
      logger.info("can not fetch {}, {}", url, e);
    }

    if (response != null) {
      if (response.body().length() < minPageLength) {
        logger.info("get just {} bytes from {}, ignore", response.body().length(), url);
        response = null;
      } else {
        logger.debug("received {} bytes from {}", response.body().length(), url);
      }
    }

    return response;
  }

  public Document parse(Response res) {
    Document doc = null;

    try {
      if (res != null)
        doc = res.parse();
    } catch (IOException e) {
      logger.error(e);
    }

    return doc;
  }

}
