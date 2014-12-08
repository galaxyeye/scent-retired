package org.qiwur.scent.jsoup.block;

import java.util.List;

import jodd.util.StringUtil;

import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.select.DOMUtil;

import com.google.common.collect.Lists;

import ruc.irm.similarity.FuzzyProbability;

public class DomSegmentsUtils {

  public static void addIfNotExist(DomSegments segments, Element block, String reason) {
    if (!hasSegment(segments, block)) {
      if (StringUtil.isNotEmpty(reason)) {
        block.attr("data-blocking-reason", reason);
      }
      segments.add(DomSegment.create(block));
    }
  }

  public static void addIfNotExist(DomSegments segments, Element block) {
    addIfNotExist(segments, block, null);
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

    return result;
  }

  public static DomSegments findByBlock(DomSegments segments, Element block) {
    DomSegments result = new DomSegments();

    for (DomSegment segment : segments) {
      if (segment.block() == block) {
        result.add(segment);
      }
    }

    return result;
  }

  public static void removeByBlock(DomSegments segments, Element block) {
    segments.removeAll(DomSegmentsUtils.findByBlock(segments, block));
  }

  public static boolean hasSegment(DomSegments segments, Element block) {
    for (DomSegment segment : segments) {
      if (segment.block() == block) {
        return true;
      }
    }

    return false;
  }

  public static void destroyTree(DomSegments segments) {
    for (DomSegment segment : segments) {
      segment.remove();
    }
  }

  public static void buildTree(DomSegments segments) {
    for (DomSegment ancestor : segments) {
      for (DomSegment child : segments) {
        if (DOMUtil.isAncestor(child, ancestor)) {
          if (child.parent() == null) {
            ancestor.appendChild(child);
          }
          else if (child.parent().block().depth() < ancestor.block().depth()) {
            // the new "parent" is closer than the older one, so replace the older one
            child.parent().removeChild(child);
            ancestor.appendChild(child);
          }
        }
      }
    }
  }

  public static List<DomSegment> mergeTree(DomSegments segments, double likelihood) {
    List<DomSegment> removal = Lists.newArrayList();

    for (DomSegment parent : segments) {
      for (DomSegment child : parent.children()) {
        double like = child.likelihood(parent);

        if (DomSegment.logger.isDebugEnabled() && like > 0.6) {
//          DomSegment.logger.debug("likelihood : {}, {}, {}", String.format("%.2f", like), parent.block().prettyName(), 
//              child.block().prettyName());
        }

        // it's the parent rather then the child should be removed
        // because the child usually appears to be much more well formatted
        if (like >= likelihood) {
          removal.add(parent);
        }
      }
    }

    for (DomSegment segment : removal) {
      segment.remove();
    }

    return removal;
  }

  public static List<DomSegment> mergeSegments(DomSegments segments, double likelihood) {
    List<DomSegment> removal = mergeTree(segments, likelihood);
    segments.removeAll(removal);

    if (DomSegment.logger.isDebugEnabled()) {
      List<String> names = Lists.newArrayList();
      for (DomSegment segment : removal) {
        names.add(segment.block().prettyName());
      }

      // DomSegment.logger.debug("remove redundant segments : {}", names);
    }

    return removal;
  }
}
