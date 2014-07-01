package org.qiwur.scent.entity;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.StringUtils;

import com.google.common.collect.Sets;

public class EntityAttribute implements Comparable<EntityAttribute> {
  private final String name;
  private String value = "";

  private Set<EntityCategory> categories = Sets.newHashSet();

  public EntityAttribute(String name, String value) {
    Validate.notEmpty(name);
    Validate.notNull(value);

    this.name = name;
    this.value = value;
  }

  public EntityAttribute(String name, String value, String category) {
    this(name, value);
    categorize(category);
  }

  public EntityAttribute(String name, String value, Collection<String> categories) {
    this(name, value);
    categorize(categories);
  }

  public EntityAttribute(EntityAttribute other) {
    Validate.notNull(other);

    this.name = other.name;
    this.value = other.value;
    this.categories.addAll(other.categories);
  }

  public String name() {
    return name;
  }

  // TODO : what if categories.size() > 1 in which case it's a category tree
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
    this.value = value;
  }

  public void categorize(EntityCategory category) {
    if (category == null) return;

    this.categories.add(category);
  }

  public void categorizeAll(Collection<EntityCategory> categories) {
    if (categories == null) return;

    for (EntityCategory category : categories) {
        categorize(category);
    }
  }

  public void categorize(String category) {
<<<<<<< HEAD
    if (StringUtils.isNotEmpty(category)) {
      this.categories.add(new EntityCategory(category));
    }
=======
    if (category != null) this.categories.add(new EntityCategory(category));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
  }

  public void categorize(Collection<String> categories) {
    if (categories == null) return;

    for (String category : categories) {
      categorize(category);
    }
  }

  public boolean hasCategory(String name) {
    for (EntityCategory category : categories) {
      if (category.name().equals(name)) return true;
    }

    return false;
  }

  public boolean uncategorized() {
    return categories.isEmpty();
  }

  public Set<EntityCategory> categories() {
    return categories;
  }

  @Override
  public EntityAttribute clone() {
    return new EntityAttribute(this);
  }

  public String simpleCategoriesString() {
    StringBuilder sb = new StringBuilder();

    int i = 0;
    for (EntityCategory category : categories) {
      if (i++ > 0) {
        sb.append(',');
      }
      sb.append(category.name());
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return fullName();
  }

  /**
   * Compares the specified EntityAttributes Two EntityAttribute are equal when
   * the following fileds are all equal : 1. name 2. value 3. categories
   * */
  @Override
  public boolean equals(Object attribute) {
    if (!(attribute instanceof EntityAttribute))
      return false;

    EntityAttribute a = (EntityAttribute) attribute;
    return name.equals(a.name) && value.equals(a.value) && categories.equals(a.categories);
  }

  // TODO : check if we need compare with the categories
  @Override
  public int compareTo(EntityAttribute other) {
    int r = name.compareTo(other.name);
    if (r == 0) {
      return value.compareTo(other.value);
    } else
      return r;
  }
}
