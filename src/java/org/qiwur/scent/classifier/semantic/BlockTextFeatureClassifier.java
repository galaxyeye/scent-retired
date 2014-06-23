package org.qiwur.scent.classifier.semantic;

import java.util.Collection;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
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
   * keyword based classifier, the classifier affects only the leaf segments in the segment tree
   */
  @Override
  protected double getScore(DomSegment segment, String label) {
    if (!segment.hasChild()) return 0.0;

    Map<String, Double> rules = blockTextFeature.getRules(label);
    if (rules == null) return 0.0;

    double score = 0.0;
    String text = segment.body().text();
    for (Entry<String, Double> rule : rules.entrySet()) {
      int count = StringUtils.countMatches(text, rule.getKey());
      score += count * rule.getValue();
    }

    return score;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockTextFeature.getLabels();
  }
}
