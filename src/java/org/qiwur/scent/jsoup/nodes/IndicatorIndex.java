package org.qiwur.scent.jsoup.nodes;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;

import com.google.common.collect.TreeMultimap;

public class IndicatorIndex {

  public static final String Blocks = "Blocks";

  private Map<String, TreeMultimap<Double, Element>> indexes = new HashMap<String, TreeMultimap<Double, Element>>();

  public IndicatorIndex() {
    for (String name : Indicator.names) {
      @SuppressWarnings("unchecked")
      TreeMultimap<Double, Element> map = TreeMultimap.create(
          ComparatorUtils.reversedComparator(ComparatorUtils.NATURAL_COMPARATOR),
          ComparatorUtils.NATURAL_COMPARATOR);

      indexes.put(name, map);
    }
  }

  public TreeMultimap<Double, Element> get(String name) {
    return indexes.get(name);
  }

  public Set<Element> get(String name, Double key) {
    TreeMultimap<Double, Element> map = indexes.get(name);
    if (map != null)
      return map.get(key);

    return null;
  }

  public TreeMultimap<Double, Element> put(String name, TreeMultimap<Double, Element> index) {
    return indexes.put(name, index);
  }

  public boolean put(String name, Double key, Element ele) {
    TreeMultimap<Double, Element> map = indexes.get(name);
    if (map != null) {
      return map.put(key, ele);
    }

    return false;
  }

  @Override
  public String toString() {
    return indexes.keySet().toString();
  }
}
