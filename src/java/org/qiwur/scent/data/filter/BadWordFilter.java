package org.qiwur.scent.data.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.data.feature.FeatureManager;
import org.qiwur.scent.data.feature.LinedFeature;

public class BadWordFilter implements DataFilter {

  private final String featureFile;
  private final Configuration conf;
  private final LinedFeature feature;

  public BadWordFilter(String featureFile, Configuration conf) {
    this.featureFile = featureFile;
    this.conf = conf;

    feature = FeatureManager.get(conf, LinedFeature.class, featureFile);
  }

  public Configuration getConf() {
    return conf;
  }

  public Collection<String> filter(Collection<String> words) {
    Set<String> filteredWords = new HashSet<String>();
    return filter(filteredWords);
  }

  public Collection<String> filter(Collection<String> words, Collection<String> filteredWords) {
    for (String word : words) {
      for (String badWord : feature.lines()) {
        if (!word.contains(badWord)) {
          filteredWords.add(word);
        }
      }
    }

    return filteredWords;
  }

  @Override
  public String filter(String text) {
    for (String line : feature.lines()) {
      text = text.replace(line, "");
    }

    return text;
  }

  @Override
  public String filterToNull(String text) {
    String result = filter(text);
    return result.isEmpty() ? null : result;
  }
}
