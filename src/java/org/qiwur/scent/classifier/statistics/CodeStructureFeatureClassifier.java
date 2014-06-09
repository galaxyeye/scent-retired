package org.qiwur.scent.classifier.statistics;

import java.util.Collection;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.feature.BlockStatFeature;
import org.qiwur.scent.jsoup.block.DomSegment;

public class CodeStructureFeatureClassifier extends RuleBasedBlockClassifier {

  private final BlockStatFeature blockStatFeature;

	public CodeStructureFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
		super(segments, labels, conf);

    double weight = conf.getFloat("scent.code.structure.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String file = conf.get("scent.block.stat.feature.file");
    blockStatFeature = BlockStatFeature.create(file, conf);

    blockStatFeature.setGlobalVar("$_menu_seq", conf.get("scent.page.menu.sequence"));
    blockStatFeature.setGlobalVar("$_title_seq", conf.get("scent.page.title.sequence"));
    blockStatFeature.setGlobalVar("$_last_seq", conf.get("scent.page.all.node.count"));

    logger.debug("global vars : " + blockStatFeature.dumpGlobalVars());
	}

  @Override
  protected double getScore(DomSegment segment, String label) {
    if (segment.body() == null) return 0.0;

    return blockStatFeature.getScore(segment.body(), label);
  }

  @Override
  public Collection<String> labelsInCharge() {
    return blockStatFeature.getLabels();
  }
}
