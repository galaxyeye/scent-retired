package org.qiwur.scent.classifier.semantic;

import java.util.Collection;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.feature.PhraseFeature;
import org.qiwur.scent.jsoup.block.DomSegment;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.sentence.morphology.MorphoSimilarity;

public class BlockTitleFeatureClassifier extends RuleBasedBlockClassifier {

  private final PhraseFeature blockTitleFeature;

  public BlockTitleFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
    super(segments, labels, conf);

    double weight = conf.getFloat("scent.block.title.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String featureFile = conf.get("scent.block.title.feature.file");
    blockTitleFeature = FeatureManager.get(conf, PhraseFeature.class, featureFile);
  }

  @Override
  protected double getScore(DomSegment segment, String label) {
    double score = 0.0;

    if (segment.hasTitle()) {
      String text = preprocessTitleText(segment.titleText());

      if (text != null) {
        // we use 10 marked system, so map [0, 1] to [0, 10]
        score = 10 * getMaxSimilarity(text, blockTitleFeature.getPhraseRules(label));
      }
    }

    return score;
  }

  /*
   * 计算一个字符串和一组字符串的相似度，取超过mustBe的相似度值或者最大相似度值
   */
  private double getMaxSimilarity(String text, Map<String, Double> rules) {
    double lastSim = 0.0;

    if (rules == null) return 0.0;

    for (String phrase : rules.keySet()) {
      double sim = MorphoSimilarity.getInstance().getSimilarity(text, phrase);
      // 在相似度计算中，分值为扩大系数 : sim = sim * (1 + score)
      if (FuzzyProbability.maybe(sim)) {
        sim += sim * rules.get(phrase);
      }

      if (sim > 1.0) {
        sim = 1.0;
      }

      if (sim > lastSim) {
        lastSim = sim;
      }

      // 已经充分相似了，下面的比较没有意义
      if (FuzzyProbability.mustBe(sim)) {
        break;
      }
    }

    return lastSim;
  }

  private String preprocessTitleText(String text) {
    if (text == null || text.length() < DomSegment.MinTitleLength || text.length() > DomSegment.MaxTitleLength) {
      return null;
    }

    text = text.replace("'", "");
    text = text.replace("\"", "");
    text = text.trim().toLowerCase();
    // text = PAT_REMOVE_COMMENT_POTION.matcher(text).replaceAll("").trim();

    if (text.length() < DomSegment.MinTitleLength)
      return null;

    return text;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockTitleFeature.getLabels();
  }
}
