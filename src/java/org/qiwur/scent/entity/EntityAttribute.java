package org.qiwur.scent.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;

// PAttribute 被设计为name和<alias, category>是unique id
public class EntityAttribute implements Comparable<EntityAttribute> {
  private String name;
  private String value = "";

  private Multimap<String, EntityCategory> categories = LinkedListMultimap.create();
  private Set<String> aliases = new HashSet<String>();
  private Pattern valuePattern = null;

  public EntityAttribute(String name, Pattern valuePattern, EntityCategory... categories) {
    Validate.notEmpty(name);

    this.name = name;
    this.valuePattern = valuePattern;

    if (categories != null) {
      for (EntityCategory category : categories) {
        if (category != null) {
          this.categories.put(category.name(), category);
        }
      }
    }
  }

  public EntityAttribute(String name, String value) {
    Validate.notEmpty(name);
    Validate.notNull(value);

    this.name = name;
    this.value = value;    
  }

  public EntityAttribute(String name, String value, String... categories) {
    Validate.notEmpty(name);
    Validate.notNull(value);

    this.name = name;
    this.value = value;

    if (categories != null) {
      for (String category : categories) {
        if (!StringUtils.isEmpty(category)) {
          this.categories.put(category, new EntityCategory(category));
        }
      }
    }
  }

  public EntityAttribute(String name, String value, EntityCategory... categories) {
    this(name, (Pattern) null, categories);
    this.value = value;
  }

  public EntityAttribute(EntityAttribute other) {
    Validate.notNull(other);

    this.name = other.name;
    this.value = other.value;
    this.categories = LinkedListMultimap.create(other.categories);

    if (other.aliases != null) {
      this.aliases = new HashSet<String>(other.aliases);
    }

    if (other.valuePattern != null) {
      this.valuePattern = other.valuePattern;
    }
  }

  public String name() {
    return name;
  }

  public void name(String name) {
    this.name = name;
  }

  public String fullName() {
    if (categories.size() > 0) {
      StringBuilder fullName = new StringBuilder();

      fullName.append(categories().iterator().next().fullName());
      fullName.append("/");
      fullName.append(name);

      return fullName.toString();
    }

    return name;
  }

  public String value() {
    return value;
  }

  public void value(String value) {
    this.value = getValue(value);
  }

  public Pattern valuePattern() {
    return valuePattern;
  }

  public boolean hasCategory(String name) {
    return categories.containsKey(name);
  }

  public boolean uncategorized() {
    return categories.isEmpty();
  }

  public Set<String> aliases() {
    return aliases;
  }

  public void aliases(Set<String> aliases) {
    this.aliases = aliases;
  }

  public Collection<EntityCategory> categories() {
    return categories.values();
  }

  @Override
  public EntityAttribute clone() {
    return new EntityAttribute(this);
  }

  public String simpleCategoriesString() {
    if (categories.isEmpty()) return "";
    return categories.keySet().toString();
  }

  public String simpleAliasesString() {
    if (aliases.isEmpty()) return "";
    return aliases.toString();
  }

  @Override
  public String toString() {
    String s = name + " : " + value + "\t";

    if (!aliases.isEmpty()) s += "| aliases : " + aliases.toString() + "\t";
    if (!categories.isEmpty()) s += "| categories : " + categories.keySet().toString() + "\t";

    return s;
  }

  private String getValue(String value) {
    if (valuePattern == null) {
      return value;
    }

    Matcher matcher = valuePattern.matcher(value);

    if (matcher.find()) {
      value = matcher.group();
    }

    return value;
  }

  @Override
  public int compareTo(EntityAttribute other) {
    int r = name.compareTo(other.name);
    if (r == 0) {
      return value.compareTo(other.value);
    }
    else return r;
  }
}
