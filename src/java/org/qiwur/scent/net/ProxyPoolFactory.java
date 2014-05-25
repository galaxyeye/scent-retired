package org.qiwur.scent.net;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

public class ProxyPoolFactory {

  public static final Logger LOG = LogManager.getLogger(ProxyPoolFactory.class);

  private final Configuration conf;

  public ProxyPoolFactory(Configuration conf) {
    this.conf = conf;
  }

  public ProxyPool getProxyPool() {
    ObjectCache objectCache = ObjectCache.get(conf);
    String cacheId = "NetworkProxyConnectionPool";

    if (objectCache.getObject(cacheId) != null) {
      return (ProxyPool) objectCache.getObject(cacheId);
    } else {
      ProxyPool pool = new ProxyPool(conf);
      objectCache.setObject(cacheId, pool);
      return pool;
    }
  }
}
