package org.qiwur.scent.utils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Ignore;
import org.junit.Test;
import org.qiwur.scent.jsoup.block.BlockPattern;

public class TestNet {

	private static final Logger logger = LogManager.getLogger(TestNet.class);

  @Test
  @Ignore("GORA-326 Removal of _g_dirty field from _ALL_FIELDS array and Field Enum in Persistent classes")
  public void testSinglethreaded() throws Exception {
  }

  @Test
  public void testSystemProxy() throws URISyntaxException {
		try {
			Properties systemSettings = System.getProperties();

			systemSettings.put("proxySet", "true");
			systemSettings.put("http.proxyHost", "proxy.mycompany1.local");
			systemSettings.put("http.proxyPort", "80");

			URL u = new URL("http://www.baidu.com");
			HttpURLConnection con = (HttpURLConnection) u.openConnection();

			System.out.println(con.getResponseCode() + " : " + con.getResponseMessage());
			System.out.println(con.getResponseCode() == HttpURLConnection.HTTP_OK);
		} catch (Exception e) {
			e.printStackTrace();
			System.out.println(false);
		}

		System.setProperty("java.net.useSystemProxies", "true");

		Proxy proxy = (Proxy) ProxySelector.getDefault().select(new URI("http://www.yahoo.com/")).iterator().next();
		System.out.println("proxy hostname : " + proxy.type());

		InetSocketAddress addr = (InetSocketAddress) proxy.address();
		if (addr == null) {
			System.out.println("No Proxy");
		} else {
			System.out.println("proxy hostname : " + addr.getHostName());
			System.out.println("proxy port : " + addr.getPort());
		}
	}

	public static void main(String s[]) throws Exception {
    logger.debug("all block patterns : {}", BlockPattern.patterns);
  }
}
