package org.qiwur.scent.jsoup.block;

import java.util.HashSet;
import java.util.Set;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.FuzzyTracker;

public class BlockLabelTracker extends FuzzyTracker<BlockLabel> {

	public BlockLabelTracker() {
		super();
	}

	public Set<String> getLabels() {
	  Set<String> labels = new HashSet<String>();

	  for (BlockLabel label : keySet()) {
	    labels.add(label.text());
	  }

	  return labels;
	}

  public String getLabelsAsString(FuzzyProbability p) {
    String labels = "";

    for (BlockLabel label : keySet()) {
      if (is(label, p)) {
        labels += label;
        labels += " ";
      }
    }

    return labels;
  }

	public String getLabelsAsString() {
	  return getLabelsAsString(FuzzyProbability.UNSURE);
	}
}
