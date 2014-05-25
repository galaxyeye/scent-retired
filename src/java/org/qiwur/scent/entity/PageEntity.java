package org.qiwur.scent.entity;

import java.util.Collection;
import java.util.Formatter;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class PageEntity {

  protected final LinkedListMultimap<String, EntityAttribute> attributes = LinkedListMultimap.create();

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

  public EntityAttribute put(EntityAttribute attribute) {
    if (attribute == null) {
      return null;
    }

    boolean ok = attributes.put(attribute.name(), attribute);

    return ok ? attribute : null;
  }

  public boolean putAll(Collection<EntityAttribute> attributes) {
    boolean changed = false;

    for (EntityAttribute attribute : attributes) {
      EntityAttribute ret = put(attribute);
      changed = (ret != null);
    }

    return changed;
  }

  public List<EntityAttribute> get(String name) {
    return attributes.get(name);
  }

  public List<EntityAttribute> getAll() {
    return attributes.values();
  }

  public List<EntityAttribute> getByCategory(String category) {
    LinkedList<EntityAttribute> results = new LinkedList<EntityAttribute>();

    for (EntityAttribute attribute : attributes.values()) {
      if (attribute.hasCategory(category)) {
        results.add(attribute);
      }
    }

    return results;
  }

  public List<EntityAttribute> getUncategorized() {
    List<EntityAttribute> results = new LinkedList<EntityAttribute>();

    for (EntityAttribute attribute : attributes.values()) {
      if (attribute.uncategorized()) {
        results.add(attribute);
      }
    }

    return results;
  }

  public int size() {
    return attributes.size();
  }

  public int count(String name) {
    return attributes.get(name).size();
  }

  public String text(String name) {
    Collection<EntityAttribute> attrs = get(name);

    if (!attrs.isEmpty())
      return get(name).iterator().next().value();

    return "";
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
    return attributes.containsKey(name);
  }

  public boolean contains(String name, String value) {
    Collection<EntityAttribute> attrs = get(name);

    for (EntityAttribute attr : attrs) {
      if (attr.value().equals(value))
        return true;
    }

    return false;
  }

  public boolean contains(String name, String value, String label) {
    Collection<EntityAttribute> attrs = get(name);

    for (EntityAttribute attr : attrs) {
      if (attr.value().equals(value) && attr.categories().contains(label))
        return true;
    }

    return false;
  }

  public EntityAttribute first(String name) {
    Collection<EntityAttribute> attrs = get(name);

    if (!attrs.isEmpty())
      return get(name).iterator().next();

    return null;
  }

  public boolean remove(EntityAttribute attribute) {
    return attributes.remove(attribute.name(), attribute);
  }

  public Collection<EntityAttribute> removeAll(String name) {
    return attributes.removeAll(name);
  }

  public Multimap<String, EntityAttribute> attributes() {
    return attributes;
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
    for (EntityAttribute attr : attributes.values()) {
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
