package org.qiwur.scent.data.filter;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.feature.LinedFeature;
import org.qiwur.scent.feature.LinedFeatureFactory;

public class BadWordFilter implements DataFilter {

  private final String file;
  private final Configuration conf;
  private final LinedFeature feature;

  public BadWordFilter(String file, Configuration conf) {
    this.file = file;
    this.conf = conf;

    feature = new LinedFeatureFactory(file, conf).get();
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
      if (text.contains(line)) {
        return "";
      }
    }

    return text;
  }
}
