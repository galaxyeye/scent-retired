package org.qiwur.scent.classifier;

import org.apache.hadoop.conf.Configuration;
import org.jsoup.block.DomSegment;

public abstract class RuleBasedBlockClassifier extends BlockClassifier {
  public RuleBasedBlockClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);
  }
}
