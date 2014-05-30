package org.qiwur.scent.entity;

import java.util.Collection;
import java.util.Formatter;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multimap;
import com.google.common.collect.Multiset;
import com.google.common.collect.TreeMultimap;
import com.google.common.collect.TreeMultiset;

// TODO : add multimap interface ?
public class PageEntity {

  // TODO : consider sorting by full name
  protected final Multiset<EntityAttribute> attributes = TreeMultiset.create();
  private final Set<String> values = new HashSet<String>();

  public PageEntity() {
  }

  public EntityAttribute put(String name, String value) {
    return put(name, value, "");
  }

  // put : do not put attribute with both name and value are the same
  public EntityAttribute put(String name, String value, String label) {
    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value))
      return null;

    return put(new EntityAttribute(name, value, label));
  }

  // put : do not put attribute with both name and value are the same
  public EntityAttribute put(String name, String value, Collection<String> labels) {
    if (StringUtils.isEmpty(name) || StringUtils.isEmpty(value))
      return null;

    return put(new EntityAttribute(name, value, labels));
  }

  public EntityAttribute put(EntityAttribute attribute) {
    if (attribute == null) {
      return null;
    }

    values.add(attribute.value());
    attributes.add(attribute);

    return attribute;
  }

  public boolean putAll(Collection<EntityAttribute> attributes) {
    boolean changed = false;

    for (EntityAttribute attribute : attributes) {
      EntityAttribute ret = put(attribute);
      changed = (ret != null);
    }

    return changed;
  }

  public Multiset<EntityAttribute> attributes() {
    return attributes;
  }

  public Multiset<EntityAttribute> get(String name) {
    Multiset<EntityAttribute> results = TreeMultiset.create();

    for (EntityAttribute attr : attributes) {
      if (attr.name().equals(name)) {
        results.add(attr);
      }
    }

    return results;
  }

  public Multiset<EntityAttribute> get(String name, String value) {
    Multiset<EntityAttribute> results = TreeMultiset.create();

    for (EntityAttribute attr : attributes) {
      if (attr.name().equals(name) && attr.value().equals(value)) {
        results.add(attr);
      }
    }

    return results;
  }

  public Multiset<EntityAttribute> get(String name, String value, String category) {
    Multiset<EntityAttribute> results = TreeMultiset.create();

    for (EntityAttribute attr : attributes) {
      if (attr.name().equals(name) && attr.value().equals(value) && attr.hasCategory(category)) {
        results.add(attr);
      }
    }

    return results;
  }

  public Multiset<EntityAttribute> getCategorized(String category) {
    Multiset<EntityAttribute> results = TreeMultiset.create();

    for (EntityAttribute attribute : attributes) {
      if (attribute.hasCategory(category)) {
        results.add(attribute);
      }
    }

    return results;
  }

  public Multiset<EntityAttribute> getUncategorized() {
    Multiset<EntityAttribute> results = TreeMultiset.create();

    for (EntityAttribute attribute : attributes) {
      if (attribute.uncategorized()) {
        results.add(attribute);
      }
    }

    return results;
  }

  public EntityAttribute first(String name) {
    Collection<EntityAttribute> attrs = get(name);

    if (!attrs.isEmpty())
      return get(name).iterator().next();

    return null;
  }

  public String firstValue(String name) {
    EntityAttribute attr = first(name);
    return attr == null ? null : attr.value();
  }

  public String firstText(String name) {
    String text = firstValue(name);
    return text == null ? null : text;
  }

  public int size() {
    return attributes.size();
  }

  public int count(String name) {
    return get(name).size();
  }

  /**
   * TODO : check bug
   * combine by category and make the collection unique
   * */
  public PageEntity getCombined() {
    PageEntity other = new PageEntity();

    for (EntityAttribute attr : attributes) {
      String name = attr.name();
      String value = attr.value();

      if (!other.contains(name, value)) {
        other.put(attr);
      }
      else {
        other.get(name, value).iterator().next().categorizeAll(attr.categories());
      }
    }

    return other;
  }

  public String join(String name) {
    return join(name, null);
  }

  public String join(String name, String sep) {
    StringBuilder sb = new StringBuilder();

    Collection<EntityAttribute> attrs = get(name);

    for (EntityAttribute attr : attrs) {
      sb.append(attr.value());

      if (sep != null)
        sb.append(sep);
    }

    return sb.toString();
  }

  public boolean contains(String name) {
    return !get(name).isEmpty();
  }

  public boolean contains(String name, String value) {
    return !get(name, value).isEmpty();
  }

  public boolean contains(String name, String value, String category) {
    return !get(name, value, category).isEmpty();
  }

  public boolean remove(EntityAttribute attribute) {
    return attributes.remove(attribute);
  }

  public boolean removeAll(String name) {
    return attributes.removeAll(get(name));
  }

  @Override
  public String toString() {
    StringBuilder reporter = new StringBuilder();
    @SuppressWarnings("resource")
    Formatter formatter = new Formatter(reporter, Locale.SIMPLIFIED_CHINESE);

    int counter = 0;

    @SuppressWarnings("unchecked")
    Multimap<String, EntityAttribute> categorySortedattributes = TreeMultimap.create(
        ComparatorUtils.reversedComparator(ComparatorUtils.NATURAL_COMPARATOR), ComparatorUtils.NATURAL_COMPARATOR);
    for (EntityAttribute attr : attributes) {
      if (!attr.categories().isEmpty()) ++counter;
      categorySortedattributes.put(attr.simpleCategoriesString(), attr);
    }

    formatter.format("\n\n total attributes : %d, categorized : %d \n\n", attributes.size(), counter);
    for (EntityAttribute attr : categorySortedattributes.values()) {
      String value = StringUtils.substring(attr.value(), 0, 120);
      value = StringUtil.stripNonChar(value, StringUtil.DefaultKeepChars);

      formatter.format("%-20s %-15s %-120s\n",
          StringUtils.substring(attr.simpleCategoriesString(), 0, 20),
          StringUtils.substring(attr.name(), 0, 12),
          value
      );
    }

    reporter.append("\n");
    return reporter.toString();
  }
}
