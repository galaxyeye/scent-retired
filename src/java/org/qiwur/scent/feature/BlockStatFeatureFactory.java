package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.utils.ObjectCache;

public class BlockStatFeatureFactory {

  private static final String defaultConfigFile = "conf/block-stat-feature-default.xml";

  private final String configFile;
  private final Configuration conf;

  public BlockStatFeatureFactory(String configFile, Configuration conf) {
    this.configFile = configFile;
    this.conf = conf;
  }

  public BlockStatFeature getFeature() {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = configFile;

    if (objectCache.getObject(cacheId) != null) {
      return (BlockStatFeature) objectCache.getObject(cacheId);
    } else {
      BlockStatFeature feature = new BlockStatFeature(defaultConfigFile, configFile);
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }
}
