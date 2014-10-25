package org.qiwur.scent.net;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.apache.commons.io.FileUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.nutch.util.FiledLines;
import org.apache.nutch.util.NetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * Proxy Resource Manager module
 * 
 * Manage proxy server pool, for every url in fetch queue, we choose a proxy server to fetch the target
 * from the proxy server pool
 * 
 * Proxy pool is used only for Proxy Mode, we will develop the other mode : Task Mode
 * In task mode, the nutch server will mainly do the schedule:
 * 
 * 1. satellites will ask the server for tasks
 * 2. once satellites finished the tasks, they will send the result back to the server
 * 3. the server will then save the result page into the backend storage
 * 
 * */
public class ProxyPool {

  protected static final Logger logger = LoggerFactory.getLogger(ProxyPool.class);

  public static final String ProxyListFile = "/tmp/nutch-proxy-servers.txt";
  // public static final String ProxyListFile = "conf/proxy-servers.txt";

  private final Configuration conf;

  private long fileLastModified = 0;

  private long lastReviewRetiredTime = 0;

  private long reviewRetiredPeriod = 60 * 1000;

  private long pollingWait = 10;

  private int pollingMaxRetry = 5;

  private FiledLines proxyServerList = null;

  // TODO : we will save connected connections, rather than just save the ip-port pairs
  private BlockingQueue<ProxyEntry> proxyEntries = new ProxyEntryBlockingQueue();
  private BlockingQueue<ProxyEntry> retiredProxyEntries = new ProxyEntryBlockingQueue();

  public ProxyPool() {
    this.conf = null;

    touchProxyConfigFile();
  }

  public ProxyPool(Configuration conf) {
    this.conf = conf;

    touchProxyConfigFile();
    update();
  }

  public int size() {
    return proxyEntries.size();
  }

  public boolean exhausted() {
    return size() == 0;
  }

  public int retiredSize() {
    return retiredProxyEntries.size();
  }

  // will block until timeout or an available proxy entry returns
  // thread safe
  public ProxyEntry poll() throws InterruptedException {
    ProxyEntry proxy = null;

    logger.debug("pool size : {}, {}", proxyEntries.size(), retiredProxyEntries.size());

    int retry = 0;
    while (proxy == null && retry < pollingMaxRetry) {
      if (proxyEntries.isEmpty()) {
        reviewRetired();
      }

      if (retry > 0) {
        logger.debug("polling for proxy, retry : {}", retry);
      }

      // Retrieves and removes the head of this queue, waiting if necessary until an element becomes available
      proxy = proxyEntries.poll(pollingWait, TimeUnit.SECONDS);
      if (proxy == null) {
        ++retry;
        continue;
      }

      if (proxy.expired()) {
        // proxy server is not available
        retire(proxy);
        proxy = null;
      }
    }

    return proxy;
  }

  // thread safe
  public boolean contains(ProxyEntry proxy) {
    return proxyEntries.contains(proxy);
  }

  // thread safe
  public void put(ProxyEntry proxy) throws InterruptedException {
    proxyEntries.put(proxy);
  }

  // thread safe
  public void retire(ProxyEntry proxy) throws InterruptedException {
    retiredProxyEntries.put(proxy);
  }

  // thread safe
  public void reviewRetired() throws InterruptedException {
    long time = System.currentTimeMillis();
    if (time - lastReviewRetiredTime < reviewRetiredPeriod) {
      // logger.debug("review retired proxy entries later, skip...");

      return;
    }

    lastReviewRetiredTime = time;

    int count = retiredProxyEntries.size();
    int reuseCount = 0;

    // no retired proxy
    if (count == 0) {
      return;
    }

    logger.debug("retired proxy : {}, available proxy : {}", count, size());

    // poll : Retrieves and removes the head of this queue, or returns null if this queue is empty.
    ProxyEntry proxy = retiredProxyEntries.poll();
    while (proxy != null) {
      if (testNetwork(proxy)) {
        ++reuseCount;
        put(proxy);
      }
      else if (!proxy.dead()) {
        retire(proxy);
      }
      else {
        logger.info("proxy {} is dead", proxy);
        // just throw away
      }

      if (count-- > 0) {
        proxy = retiredProxyEntries.poll();
      }
      else {
        proxy = null;
      }
    }

    if (reuseCount > 0) {
      logger.debug("reuse {} retired proxy, available proxy : {}", reuseCount, size());
    }
  }

  public void tryUpdateFromFile() {
    // logger.trace("update proxy servers from file");

    // touch the file to force update from the file
    final long ForceTouchPeriod = 60 * 60 * 1000; // an hour
    final long UpdateFromFilePeriod = 60 * 1000; // one minute

    File file = new File(ProxyListFile);
    if (!file.exists() || System.currentTimeMillis() - fileLastModified > ForceTouchPeriod) {
      touchProxyConfigFile();
    }

    long modified = file.lastModified();
    double elapsed = modified - fileLastModified;
    fileLastModified = modified;

    if (elapsed > UpdateFromFilePeriod) {
      logger.debug("update from file, last modified : {}, elapsed : {}s", fileLastModified, elapsed / 1000.0);

      update();
    }
  }

  public static List<String> getConfiguredProxyList() {
    List<String> result = new ArrayList<String>();

    try {
      result.addAll(new FiledLines(ProxyListFile).getLines(ProxyListFile));
    } catch (IOException e) {
      logger.error(e.toString());
    }

    return result;
  }

  public static String touchProxyConfigFile() {
    File file = new File(ProxyListFile);
    if (!file.exists()) {
      try {
        file.createNewFile();
      } catch (IOException e) {
        logger.error(e.toString());
      }
    }

    try {
      FileUtils.touch(file);
    } catch (IOException e) {
      logger.error(e.toString());
    }

    return new Date(file.lastModified()).toString();
  }

  public static void testAndSave(List<String> proxyList) throws IOException {
    if (proxyList.isEmpty()) {
      return;
    }

    touchProxyConfigFile();

    List<Collection<String>> proxylists = new ArrayList<Collection<String>>();
    proxylists.add(proxyList);
    proxylists.add(new FiledLines(ProxyListFile).getLines(ProxyListFile));

    Set<ProxyEntry> mergedProxyEntries = new TreeSet<ProxyEntry>();
    for (Collection<String> list : proxylists) {
      for (String line : list) {
        ProxyEntry proxyEntry = ProxyEntry.parse(line);

        if (proxyEntry != null) {
          // do not add if a entry with the same ip-port exists
          mergedProxyEntries.add(proxyEntry);
        }
      }
    }

    Set<String> testedProxyList = new TreeSet<String>();
    for (ProxyEntry proxyEntry : mergedProxyEntries) {
      if (testNetwork(proxyEntry)) {
        proxyEntry.refresh(true);
      }
      else if (proxyEntry.lastAvailableTime() > 0 && proxyEntry.dead()) {
        // when the entry is first added, last available time is zero
        proxyEntry = null;
      }
      else {
        proxyEntry.refresh(false);
      }

      if (proxyEntry != null) {
        testedProxyList.add(proxyEntry.toString());
      }
    }

    // logger.trace("reported and tested proxy servers : {}", mergedProxyEntries);

    // truncate
    new FileOutputStream(ProxyListFile, false).close();
    FiledLines filedLines = new FiledLines(ProxyListFile);
    filedLines.addAll(ProxyListFile, testedProxyList);
    filedLines.saveAll();
  }

  public static boolean testNetwork(ProxyEntry proxy) {
    if (proxy == null) {
      return false;
    }

    return NetUtil.testNetwork(proxy.host(), proxy.port());
  }

  @Override
  public String toString() {
    String result = "proxy servers : [";
    for (ProxyEntry p : proxyEntries) {
      result += p.toString() + ", ";
    }
    result += "]";
    return result;
  }

  private void load() throws IOException {
    proxyServerList = new FiledLines(ProxyListFile);
  }

  private void parse() {
    try {
      List<String> proxyList = new ArrayList<String>();

      for (String line : proxyServerList.getLines(ProxyListFile)) {
        ProxyEntry proxy = ProxyEntry.parse(line);

        if (proxy != null && !proxyEntries.contains(proxy) && !retiredProxyEntries.contains(proxy)) {
          if (testNetwork(proxy)) {
            put(proxy);
            proxyList.add(proxy.ipPort());
          }
        }
      }

      if (!proxyList.isEmpty()) {
        logger.debug("use {} new proxy : {}", proxyList.size(), proxyList);
      }

      // logger.trace("parse and add tested proxy servers : {}", testedProxyServers);
    } catch (InterruptedException e) {
      logger.error("putting proxy entry interrupted, {}", e);
    }
  }

  private void update() {
    try {
      load();
      parse();
    } catch (IOException e) {
      logger.error(e.toString());
    }
  }

  private class ProxyEntryBlockingQueue extends LinkedBlockingQueue<ProxyEntry> {

    // thread safe
    @Override
    public synchronized void put(ProxyEntry proxy) throws InterruptedException {
      if (contains(proxy)) {
        logger.warn("{} is already in pool", proxy);
        return;
      }

      proxy.refresh(true);
      proxyEntries.put(proxy);
    }

    // thread safe
    public synchronized void retire(ProxyEntry proxy) throws InterruptedException {
      if (contains(proxy)) {
        logger.warn("{} is already retired", proxy);
        return;
      }

      proxy.refresh(false);
      retiredProxyEntries.put(proxy);
    }
  };
}
