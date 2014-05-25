package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.utils.ObjectCache;

public class LinedFeatureFactory {

  private final String file;
  private final Configuration conf;

  public LinedFeatureFactory(String file, Configuration conf) {
    this.file = file;
    this.conf = conf;
  }

  public LinedFeature getFeature() {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = file;

    if (objectCache.getObject(cacheId) != null) {
      return (LinedFeature) objectCache.getObject(cacheId);
    } else {
      LinedFeature feature = new LinedFeature(file);
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }
}
