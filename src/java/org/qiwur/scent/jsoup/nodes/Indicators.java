package org.qiwur.scent.jsoup.nodes;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.qiwur.scent.utils.Validate;

public class Indicators implements Iterable<Indicator>, Cloneable {

  private LinkedHashMap<String, Indicator> indicators = null;

  // linked hash map to preserve insertion order.
  // null be default as so many elements have no indicators -- saves a good
  // chunk of memory

  /**
   * Get an indicator value by key.
   * 
   * @param key
   *          the indicator key
   * @return the indicator value if set; or empty string if not set.
   * @see #hasKey(String)
   */
  public double get(String key) {
    Validate.notEmpty(key);

    if (indicators == null) {
      return 0.0;
    }

    Indicator indicator = indicators.get(key.toLowerCase());
    return indicator != null ? indicator.getValue() : 0.0;
  }

  public Indicator getIndicator(String key) {
    Validate.notEmpty(key);

    if (indicators == null) {
      return null;
    }

    return indicators.get(key.toLowerCase());
  }

  /**
   * Set a new indicator, or replace an existing one by key.
   * 
   * @param key
   *          indicator key
   * @param value
   *          indicator value
   */
  public void put(String key, double value) {
    Indicator indicator = new Indicator(key, value);
    put(indicator);
  }

  /**
   * Set a new indicator, or replace an existing one by key.
   * 
   * @param indicator
   *          indicator
   */
  public void put(Indicator indicator) {
    Validate.notNull(indicator);
    if (indicators == null)
      indicators = new LinkedHashMap<String, Indicator>(Indicator.names.length);

    indicators.put(indicator.getKey(), indicator);
  }

  /**
   * Remove an indicator by key.
   * 
   * @param key
   *          indicator key to remove
   */
  public void remove(String key) {
    Validate.notEmpty(key);
    if (indicators == null)
      return;
    indicators.remove(key.toLowerCase());
  }

  public void clear() {
    indicators.clear();
  }

  /**
   * Tests if these indicators contain an indicator with this key.
   * 
   * @param key
   *          key to check for
   * @return true if key exists, false otherwise
   */
  public boolean hasKey(String key) {
    return indicators != null && indicators.containsKey(key.toLowerCase());
  }

  /**
   * Get the number of indicators in this set.
   * 
   * @return size
   */
  public int size() {
    if (indicators == null)
      return 0;
    return indicators.size();
  }

  /**
   * Add all the indicators from the incoming set to this set.
   * 
   * @param incoming
   *          indicators to add to these indicators.
   */
  public void addAll(Indicators incoming) {
    if (incoming.size() == 0)
      return;
    if (indicators == null)
      indicators = new LinkedHashMap<String, Indicator>(incoming.size());
    indicators.putAll(incoming.indicators);
  }

  public Iterator<Indicator> iterator() {
    return asList().iterator();
  }

  /**
   * Get the indicators as a List, for iteration. Do not modify the keys of the
   * indicators via this view, as changes to keys will not be recognised in the
   * containing set.
   * 
   * @return an view of the indicators as a List.
   */
  public List<Indicator> asList() {
    if (indicators == null)
      return Collections.emptyList();

    List<Indicator> list = new ArrayList<Indicator>(indicators.size());
    for (Map.Entry<String, Indicator> entry : indicators.entrySet()) {
      list.add(entry.getValue());
    }
    return Collections.unmodifiableList(list);
  }

  @Override
  public String toString() {
    if (indicators == null) return "null";
    return indicators.toString();
  }

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (!(o instanceof Indicators))
      return false;

    Indicators that = (Indicators) o;

    if (indicators != null ? !indicators.equals(that.indicators) : that.indicators != null)
      return false;

    return true;
  }

  @Override
  public int hashCode() {
    return indicators != null ? indicators.hashCode() : 0;
  }

  @Override
  public Indicators clone() {
    if (indicators == null)
      return new Indicators();

    Indicators clone;
    try {
      clone = (Indicators) super.clone();
    } catch (CloneNotSupportedException e) {
      throw new RuntimeException(e);
    }
    clone.indicators = new LinkedHashMap<String, Indicator>(indicators.size());
    for (Indicator indicator : this)
      clone.indicators.put(indicator.getKey(), indicator.clone());
    return clone;
  }
}
