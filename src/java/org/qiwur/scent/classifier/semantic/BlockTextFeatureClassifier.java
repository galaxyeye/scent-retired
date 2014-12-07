package org.qiwur.scent.classifier.semantic;

import java.util.Collection;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.data.feature.FeatureManager;
import org.qiwur.scent.data.feature.PhraseFeature;
import org.qiwur.scent.jsoup.block.DomSegment;

public class BlockTextFeatureClassifier extends RuleBasedBlockClassifier {

  private final PhraseFeature blockTextFeature;

  public BlockTextFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);

    double weight = conf.getFloat("scent.block.text.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String[] featureFiles = conf.getStrings("scent.block.text.feature.file");
    blockTextFeature = FeatureManager.get(conf, PhraseFeature.class, featureFiles);
  }

  /*
   * keyword based classifier, the classifier affects only the leaf segments in
   * the segment tree
   */
  @Override
  protected double getScore(DomSegment segment, String label) {
    Validate.notNull(segment);

    return blockTextFeature.getScore(segment, label, segment.patternTracker().keySet());
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockTextFeature.getLabels();
  }
}
