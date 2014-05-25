package org.qiwur.scent.utils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.URL;

import org.apache.commons.lang.StringUtils;

public class NetUtil {

  public static int ProxyConnectionTimeout = 5 * 1000;

  public static boolean testNetwork(String ip, int port) {
    return testTcpNetwork(ip, port);
  }

  public static boolean testHttpNetwork(String ip, int port) {
    boolean reachable = false;

    try {
      URL url = new URL("http", ip, port, "/");
      HttpURLConnection con = (HttpURLConnection) url.openConnection();
      con.setConnectTimeout(ProxyConnectionTimeout);
      con.connect();
      // logger.info("available proxy server {} : {}", ip, port);
      reachable = true;
      con.disconnect();
    } catch (Exception x) {
      // logger.warn("can not connect to " + ip + ":" + port);
    }

    return reachable;
  }

  public static boolean testTcpNetwork(String ip, int port) {
    boolean reachable = false;
    Socket con = new Socket();

    try {
      con.connect(new InetSocketAddress(ip, port), ProxyConnectionTimeout);
      // logger.info("available proxy server : " + ip + ":" + port);
      reachable = true;
      con.close();
    } catch (Exception e) {
      // logger.warn("can not connect to " + ip + ":" + port);
    }

    return reachable;
  }

  // TODO ： Enhance performance
  public static String getDomain(String baseUri) {
    if (baseUri == null) {
      return null;
    }

    int pos = -1;
    String domain = null;

    if (StringUtils.startsWith(baseUri, "http")) {
      final int fromIndex = "https://".length();
      pos = baseUri.indexOf('/', fromIndex);

      if (pos != -1)
        baseUri = baseUri.substring(0, pos);
    }

    pos = baseUri.indexOf(".") + 1;

    if (pos != 0) {
      domain = baseUri.substring(pos);
    }

    // 如：http://dangdang.com
    if (domain != null && !domain.contains(".")) {
      domain = baseUri.replaceAll("(http://|https://)", "");
    }

    return domain;
  }
}
