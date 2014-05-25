package org.qiwur.scent.data.extractor;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

public class WebExtractorFactory {

  private final Logger logger = WebExtractor.logger;

  private final Configuration conf;

  public WebExtractorFactory(Configuration conf) {
    this.conf = conf;
  }

  public WebExtractor getWebExtractor() {
    ObjectCache objectCache = ObjectCache.get(conf);
    String cacheId = "WebExtractor";

    if (objectCache.getObject(cacheId) != null) {
      return (WebExtractor) objectCache.getObject(cacheId);
    } else {
      WebExtractor extractor = new WebExtractor(conf);
      objectCache.setObject(cacheId, extractor);
      return extractor;
    }
  }
}
