package org.qiwur.scent.classifier.statistics;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.feature.BlockStatFeature;
import org.qiwur.scent.feature.BlockStatFeatureFactory;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Indicator;

public class CodeStructureFeatureClassifier extends RuleBasedBlockClassifier {

  private final BlockStatFeature blockStatFeature;

	public CodeStructureFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
		super(segments, labels, conf);

    double weight = conf.getFloat("scent.code.structure.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String file = conf.get("scent.block.stat.feature.file");
    blockStatFeature = new BlockStatFeatureFactory(file, conf).getFeature();

    blockStatFeature.setGlobalVar("$_menu_seq", conf.get("scent.page.menu.sequence"));
    blockStatFeature.setGlobalVar("$_title_seq", conf.get("scent.page.title.sequence"));
    blockStatFeature.setGlobalVar("$_last_seq", conf.get("scent.page.all.node.count"));

    logger.debug("global vars : " + blockStatFeature.dumpGlobalVars());
	}

  @Override
  protected double getScore(DomSegment segment, String label) {
    double score = 0.0;

    Collection<StatRule> rules = blockStatFeature.getRules(label);
    if (rules == null) return score;

    for (StatRule rule : rules) {
      score += rule.getScore(segment);
    }

//    if (segment.body().tagName().equals("ul") && label.equals("SimilarEntity")) {
//      if (segment.body().indic(Indicator.IMG) > 5) {
//        logger.debug(segment.body().indicators());
//        logger.debug(rules);
//        logger.debug("score : " + score);
//      }
//    }

    return score;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockStatFeature.getLabels();
  }
}
