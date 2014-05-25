package org.qiwur.scent.feature;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.utils.ObjectCache;

public class PhraseFeatureFactory {

  private final String file;
  private final Configuration conf;

  public PhraseFeatureFactory(String file, Configuration conf) {
    this.file = file;
    this.conf = conf;
  }

  public PhraseFeature getFeature() {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = file;

    if (objectCache.getObject(cacheId) != null) {
      return (PhraseFeature) objectCache.getObject(cacheId);
    } else {
      PhraseFeature feature = new PhraseFeature(file);
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }
}
