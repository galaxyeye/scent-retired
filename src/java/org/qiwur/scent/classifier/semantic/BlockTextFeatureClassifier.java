package org.qiwur.scent.classifier.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.feature.PhraseFeature;
import org.qiwur.scent.jsoup.block.DomSegment;

public class BlockTextFeatureClassifier extends RuleBasedBlockClassifier {

  private final PhraseFeature blockTextFeature;

  public BlockTextFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);

    double weight = conf.getFloat("scent.block.text.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String featureFile = conf.get("scent.block.text.feature.file");
    blockTextFeature = FeatureManager.get(conf, PhraseFeature.class, featureFile);
  }

  /*
   * 关键词匹配算法，特征关键词数量越多，质量越高，效果越好
   */
  @Override
  protected double getScore(DomSegment segment, String label) {
    double score = 0.0;

    Map<String, Double> rules = blockTextFeature.getRules(label);
    if (rules == null) return score;

    for (Entry<String, Double> rule : rules.entrySet()) {
      boolean found = segment.body().containsStrippedOwnText(rule.getKey());
      if (found) {
        score += rule.getValue();
      }
    }

    return score;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockTextFeature.getLabels();
  }
}
