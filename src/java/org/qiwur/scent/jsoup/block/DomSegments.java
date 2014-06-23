package org.qiwur.scent.jsoup.block;

import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import ruc.irm.similarity.FuzzyProbability;

public final class DomSegments implements Set<DomSegment> {

  public static final FuzzyProbability DefaultProbability = FuzzyProbability.MAYBE;

  private Set<DomSegment> segments = new TreeSet<DomSegment>();

  public DomSegments() {
    
  }

  // 寻找相似度最大的区块
  public DomSegment get(BlockLabel label) {
    return get(label, DefaultProbability);
  }

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
