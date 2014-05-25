package org.qiwur.scent.jsoup.block;

import java.util.Comparator;

// TODO : seems buggy?
public class SimilarityComparator implements Comparator<DomSegment> {

	BlockLabel type = null;

	public SimilarityComparator(BlockLabel type) {
		this.type = type;
	}

	@Override
	public int compare(DomSegment s, DomSegment s2) {
		Double sim = s.labelTracker().get(type);
		Double sim2 = s2.labelTracker().get(type);

		if (sim == sim2) {
		  return s.compareTo(s2);
		}

		return sim2.compareTo(sim);
	}
}
