package org.qiwur.scent.jsoup.block;

import org.qiwur.scent.jsoup.nodes.Element;

import ruc.irm.similarity.FuzzyProbability;

public class DomSegmentsUtils {

  public static DomSegments addOrTag(DomSegments segments, Element block, BlockLabel label) {
    return addOrTag(segments, block, label, FuzzyProbability.VERY_LIKELY);
  }

  public static DomSegments addOrTag(DomSegments segments, Element block, String label, FuzzyProbability p) {
    return addOrTag(segments, block, BlockLabel.fromString(label), p);
  }

  public static DomSegments addOrTag(DomSegments segments, Element block, BlockLabel label, FuzzyProbability p) {
    if (block == null) return new DomSegments();

    DomSegments result = find(segments, block);

    if (result.isEmpty()) {
      DomSegment segment = DomSegment.create(block);
      segments.add(segment);
      result.add(segment);
    }

    for (DomSegment segment : result) {
      segment.tag(label, p);
    }

    return result;
  }

  public static DomSegments addOrTag(DomSegments segments, Element block, BlockPattern pattern, FuzzyProbability p) {
    if (block == null) return new DomSegments();

    DomSegments result = find(segments, block);

    if (result.isEmpty()) {
      DomSegment segment = DomSegment.create(block);
      segments.add(segment);
      result.add(segment);
    }

    for (DomSegment segment : result) {
      segment.tag(pattern, p);
    }

    return result;    
  }

  public static DomSegments find(DomSegments segments, Element block) {
    DomSegments result = new DomSegments();

    for (DomSegment segment : segments) {
      if (segment.body().equals(block) || segment.root().equals(block)) {
        result.add(segment);
      }
    }

    return result;
  }

  public static boolean hasSegment(DomSegments segments, Element block) {
    return !find(segments, block).isEmpty();
  }

}
