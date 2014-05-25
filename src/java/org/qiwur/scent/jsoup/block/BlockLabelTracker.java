package org.qiwur.scent.jsoup.block;

import ruc.irm.similarity.FuzzyProbability;
import ruc.irm.similarity.FuzzyTracker;

public class BlockLabelTracker extends FuzzyTracker<BlockLabel> {

	public BlockLabelTracker() {
		super();
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
