package org.qiwur.scent.jsoup.block;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.select.DOMUtil;

import ruc.irm.similarity.FuzzyProbability;

import com.google.common.collect.Lists;

public final class DomSegments implements Set<DomSegment> {

  private static final Logger logger = LogManager.getLogger(DomSegments.class);

  public static final FuzzyProbability DefaultProbability = FuzzyProbability.MAYBE;

  private Set<DomSegment> segments = new TreeSet<DomSegment>();

  public DomSegments() {
    
  }

  public void destroyTree() {
    for (DomSegment segment : segments) {
      segment.remove();
    }
  }

  public void buildTree() {
    for (DomSegment ancestor : segments) {
      for (DomSegment child : segments) {
        if (DOMUtil.isAncestor(child, ancestor)) {
          if (child.parent() == null) {
            ancestor.appendChild(child);
          }
          else if (child.parent().body().depth() < ancestor.body().depth()) {
            // the new "parent" is closer than the older one, so replace the older one
            child.parent().removeChild(child);
            ancestor.appendChild(child);
          }
        }
      }
    }
  }

  public List<DomSegment> mergeTree(double likelihood) {
    List<DomSegment> removal = Lists.newArrayList();

    for (DomSegment parent : segments) {
      for (DomSegment child : parent.children()) {
        double like = child.likelihood(parent);

        if (logger.isDebugEnabled() && like > 0.6) {
          logger.debug("likelihood : {}, {}, {}", String.format("%.2f", like), parent.root().prettyName(), 
              child.root().prettyName());
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

  public List<DomSegment> mergeSegments(double likelihood) {
    List<DomSegment> removal = mergeTree(likelihood);
    segments.removeAll(removal);

    if (logger.isDebugEnabled()) {
      List<String> names = Lists.newArrayList();
      for (DomSegment segment : removal) {
        names.add(segment.root().prettyName());
      }
      logger.debug("remove redundant segments : {}", names);
    }

    return removal;
  }

<<<<<<< HEAD
=======
  // 寻找相似度最大的区块
  public DomSegment get(BlockLabel label) {
    return get(label, DefaultProbability);
  }

>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  // 寻找大于p的相似度最大的区块
  public DomSegment get(BlockLabel label, FuzzyProbability p) {
    DomSegment result = null;
    double lastSim = 0.0;

    for (DomSegment segment : segments) {
      if (segment.is(label, p)) {
        double sim = segment.labelTracker().get(label);

        if (sim > lastSim) {
          lastSim = sim;
          result = segment;
        }
      }
    }

    return result;
  }

  // 寻找相似度最大的区块
  public DomSegments get(Collection<BlockLabel> labels) {
    return get(labels, DefaultProbability);
  }

  public DomSegments get(Collection<BlockLabel> labels, FuzzyProbability p) {
    DomSegments segments = new DomSegments();

    for (BlockLabel label : labels) {
      DomSegment segment = get(label, p);

      if (segment != null) {
        segments.add(segment);
      }
    }

    return segments;
  }

  public DomSegments getAll(BlockLabel label) {
    return getAll(label, DefaultProbability);
  }

  public DomSegments getAll(BlockPattern pattern, FuzzyProbability p) {
    DomSegments result = new DomSegments();

    for (DomSegment segment : segments) {
      if (segment.is(pattern, p)) {
        result.add(segment);
      }
    }

    return result;
  }

  public DomSegments getAll(BlockPattern pattern) {
    return getAll(pattern, DefaultProbability);
  }

  public DomSegments getAll(BlockLabel label, FuzzyProbability p) {
    DomSegments result = new DomSegments();

    for (DomSegment segment : segments) {
      if (segment.is(label, p)) {
        result.add(segment);
      }
    }

    return result;
  }

  public SortedSet<DomSegment> getAll(BlockLabel label, Comparator<DomSegment> comp) {
    return getAll(label, DefaultProbability, comp);
  }

  public SortedSet<DomSegment> getAll(BlockLabel label, FuzzyProbability p, Comparator<DomSegment> comp) {
    SortedSet<DomSegment> result = new TreeSet<DomSegment>(comp);

    for (DomSegment segment : segments) {
      if (segment.is(label, p)) {
        result.add(segment);
      }
    }

    return result;
  }

  public boolean hasSegment(BlockLabel label) {
    return get(label) != null;
  }

  public boolean hasSegment(BlockLabel label, FuzzyProbability p) {
    return get(label, p) != null;
  }

  @Override
  public boolean add(DomSegment segment) {
    return segments.add(segment);
  }

  @Override
  public boolean addAll(Collection<? extends DomSegment> segments) {
    return this.segments.addAll(segments);
  }

  @Override
  public void clear() {
    segments.clear();
  }

  @Override
  public boolean contains(Object arg0) {
    return segments.contains(arg0);
  }

  @Override
  public boolean containsAll(Collection<?> arg0) {
    return segments.containsAll(arg0);
  }

  @Override
  public boolean isEmpty() {
    return segments.isEmpty();
  }

  @Override
  public Iterator<DomSegment> iterator() {
    return segments.iterator();
  }

  @Override
  public boolean remove(Object arg0) {
    return segments.remove(arg0);
  }

  @Override
  public boolean removeAll(Collection<?> arg0) {
    boolean changed = false;

    for (Object o : arg0) {
      changed = remove(o);
    }

    return changed;
  }

  @Override
  public boolean retainAll(Collection<?> arg0) {
    return segments.retainAll(arg0);
  }

  @Override
  public int size() {
    return segments.size();
  }

  @Override
  public Object[] toArray() {
    return segments.toArray();
  }

  @Override
  public <T> T[] toArray(T[] arg0) {
    return segments.toArray(arg0);
  }
}
