package org.qiwur.scent.data.feature;

import java.util.ArrayList;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.common.collect.Multiset;

public final class ColorFeature extends LinedFeature {

  static final Logger logger = LogManager.getLogger(ColorFeature.class);

  public static final String KnownColorsFile = "conf/feature/known-colors.txt";

  public ColorFeature(Configuration conf, String[] featureFile) {
    super(conf, featureFile);
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
