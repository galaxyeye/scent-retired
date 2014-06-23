package org.qiwur.scent.jsoup.block;

import org.qiwur.scent.jsoup.nodes.Element;

import ruc.irm.similarity.FuzzyProbability;

public class DomSegmentsUtils {

  public static void addIfNotExist(DomSegments segments, Element block) {
    if (!hasSegment(segments, block)) {
      segments.add(DomSegment.create(block));
    }
  }

  public static DomSegments addOrTag(DomSegments segments, Element block, BlockLabel label) {
    return addOrTag(segments, block, label, FuzzyProbability.VERY_LIKELY);
  }

  public static DomSegments addOrTag(DomSegments segments, Element block, BlockLabel label, FuzzyProbability p) {
    if (block == null) return new DomSegments();

    DomSegments result = findByBlock(segments, block);

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

    DomSegments result = findByBlock(segments, block);

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

  public static DomSegments findByBlock(DomSegments segments, Element block) {
    DomSegments result = new DomSegments();

    for (DomSegment segment : segments) {
      if (segment.body() == block || segment.root() == block) {
        result.add(segment);
      }
    }

    return result;
  }

  public static boolean hasSegment(DomSegments segments, Element block) {
    for (DomSegment segment : segments) {
      if (segment.body() == block || segment.root() == block) {
        return true;
      }
    }

    return false;
  }

}
