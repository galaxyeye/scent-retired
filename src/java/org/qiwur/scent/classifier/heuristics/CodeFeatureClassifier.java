package org.qiwur.scent.classifier.heuristics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.classifier.RuleBasedBlockClassifier;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.feature.PhraseFeature;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.utils.StringUtil;

public class CodeFeatureClassifier extends RuleBasedBlockClassifier {

  private final PhraseFeature codeFeature;

	public CodeFeatureClassifier(DomSegment[] segments, String[] labels, Configuration conf) {
		super(segments, labels, conf);

    double weight = conf.getFloat("scent.code.feature.classifier.weight", 1.0f);
    this.weight(weight);

    String featureFile = conf.get("scent.block.code.feature.file");
    codeFeature = FeatureManager.get(conf, PhraseFeature.class, featureFile);    
	}

	@Override
	protected double getScore(DomSegment segment, String label) {
	  // TODO : implement the OR logic
    Map<String, Double> rules = codeFeature.getRules(label);
    if (rules == null) {
      return 0.0;
    }

    double score = 0.0;
    List<String> names = getAttributeNames(segment);
		for (Entry<String, Double> entry : rules.entrySet()) {
      if (names.contains(entry.getKey())) {
        score += entry.getValue();
      }
		}

		return score;
	}

  private List<String> getAttributeNames(DomSegment segment) {
    List<String> names = new ArrayList<String>();

    names.add(StringUtil.humanize(segment.root().attr("id")));
    names.add(StringUtil.humanize(segment.root().attr("class")));
    names.add(StringUtil.humanize(segment.body().attr("id")));
    names.add(StringUtil.humanize(segment.body().attr("class")));

    return names;
  }

  @Override
  public Collection<String> labelsInCharge() {
    return codeFeature.getLabels();
  }
}
