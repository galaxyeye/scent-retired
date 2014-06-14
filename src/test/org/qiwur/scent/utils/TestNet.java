package org.qiwur.scent.utils;

import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TestNet {

	private static final Logger logger = LogManager.getLogger(TestNet.class);

	private static void testSystemProxy() throws URISyntaxException {
		try {
			Properties systemSettings = System.getProperties();

			systemSettings.put("proxySet", "true");
			systemSettings.put("http.proxyHost", "proxy.mycompany1.local");
			systemSettings.put("http.proxyPort", "80");

			URL u = new URL("http://www.google.com");
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
    String href = StringUtils.stripEnd("http://www.suning.com", "/");
    int count = StringUtils.countMatches(href, "/");

    String text = StringUtil.stripNonChar("首页");
    
    System.out.println(count);
    System.out.println(text.equals("首页"));
  }
}
