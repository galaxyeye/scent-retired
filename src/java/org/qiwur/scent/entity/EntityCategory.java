package org.qiwur.scent.entity;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class EntityCategory implements Comparable<EntityCategory> {

  private String name;
  private Multimap<String, EntityCategory> categories = TreeMultimap.create();
  private Set<String> aliases = new HashSet<String>();

  public EntityCategory(String name) {
    Validate.notEmpty(name);
    this.name = name;
  }

  public EntityCategory(String name, String... categories) {
    Validate.notEmpty(name);

    this.name = name;
    if (categories != null) {
      for (String category : categories) {
        if (StringUtils.isEmpty(category)) {
          this.categories.put(category, new EntityCategory(category));
        }
      }
    }
  }

  public EntityCategory(String name, EntityCategory... categories) {
    Validate.notEmpty(name);

    this.name = name;
    if (categories != null) {
      for (EntityCategory category : categories) {
        if (category != null) {
          this.categories.put(category.name(), category);
        }
      }
    }
  }

  public EntityCategory(EntityCategory other) {
    Validate.notNull(other);

    this.name = other.name;
    this.categories = TreeMultimap.create(other.categories);

    if (other.aliases != null) {
      this.aliases = new HashSet<String>(other.aliases);
    }
  }

  public String name() {
    return name;
  }

  public String fullName() {
    if (categories.size() > 0) {
      StringBuilder fullName = new StringBuilder();

      fullName.append(categories().iterator().next().fullName());
      fullName.append('/');
      fullName.append(name);

      return fullName.toString();
    }

    return name;
  }

  public Collection<EntityCategory> categories() {
    return categories.values();
  }

  public Set<String> aliases() {
    return aliases;
  }

  public void aliases(Set<String> aliases) {
    this.aliases = aliases;
  }

  public String categoryString() {
    if (categories.isEmpty()) return "";
    return categories.toString();
  }

  public String aliasesString() {
    if (aliases.isEmpty()) return "";
    return aliases.toString();
  }

  @Override
  public String toString() {
    String s = "name : " + name + "\t";

    if (aliases != null) {
      s += "aliases : " + aliases.toString();
    }

    if (categories != null) {
      s += "categories : " + categories.toString();
    }

    return s;
  }

  @Override
  public EntityCategory clone() {
    return new EntityCategory(this);
  }

  @Override
  public boolean equals(Object category) {
    if (!(category instanceof EntityCategory)) {
      return false;
    }

    EntityCategory c = (EntityCategory) category;

    boolean equal = false;
    equal |= name.equals(c.name);
    equal |= categories.equals(c.categories);

    return equal;
  }

  @Override
  public int compareTo(EntityCategory other) {
    return name.compareTo(other.name);
  }
}
