package org.qiwur.scent.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.utils.ObjectCache;

import com.google.common.collect.Multiset;

public final class ColorFeature extends LinedFeature {

  static final Logger logger = LogManager.getLogger(ColorFeature.class);

  public static final String KnownColorsFile = "conf/known-colors.txt";

  private ColorFeature() {
    super(KnownColorsFile);
  }

  public static ColorFeature create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    final String cacheId = KnownColorsFile;

    if (objectCache.getObject(cacheId) != null) {
      return (ColorFeature) objectCache.getObject(cacheId);
    }
    else {
      ColorFeature feature = new ColorFeature();
      objectCache.setObject(cacheId, feature);
      return feature;
    }
  }

  public List<String> getColors(String text) {
    List<String> result = new ArrayList<String>();

    for (String color : knownColors()) {
      if (text.contains(color)) {
        result.add(color);
      }
    }

    return result;
  }

  public String getColor(String text) {
    for (String color : knownColors()) {
      if (text.contains(color)) {
        return color;
      }
    }

    return "";
  }

  public Multiset<String> knownColors() {
    return lines();
  }
}
