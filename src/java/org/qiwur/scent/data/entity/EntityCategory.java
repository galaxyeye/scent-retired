package org.qiwur.scent.data.entity;

import java.util.Collection;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;

import com.google.common.collect.Sets;

public class EntityCategory implements Comparable<EntityCategory> {

  private final String name;
  private Set<EntityCategory> categories = Sets.newHashSet();

  public EntityCategory(String name) {
    Validate.notEmpty(name);
    this.name = name;
  }

  public EntityCategory(String name, String... categories) {
    Validate.notEmpty(name);

    this.name = name;
    categorize(categories);
  }

  public EntityCategory(String name, EntityCategory... categories) {
    Validate.notEmpty(name);

    this.name = name;
    categorize(categories);
  }

  public EntityCategory(EntityCategory other) {
    Validate.notNull(other);

    this.name = other.name;
    this.categories.addAll(other.categories);
  }

  public String name() {
    return name;
  }

  // TODO : what if there is a ring in category graph?
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

  public void categorize(Collection<EntityCategory> categories) {
    if (categories == null) return;
    this.categories.addAll(categories);
  }

  public void categorize(EntityCategory... categories) {
    if (categories == null) return;

    for (EntityCategory category : categories) {
      this.categories.add(category);
    }
  }

  public void categorize(String... categories) {
    if (categories == null) return;

    for (String category : categories) {
      if (!StringUtils.isEmpty(category)) {
        this.categories.add(new EntityCategory(category));
      }
    }
  }

  public Set<EntityCategory> categories() {
    return categories;
  }

  public String simpleCategoriesString() {
    return simpleCategoriesString(",");
  }

  public String simpleCategoriesString(final String seperator) {
    StringBuilder sb = new StringBuilder();

    int i = 0;
    for (EntityCategory category : categories) {
      if (i++ > 0) {
        sb.append(seperator);
      }
      sb.append(category.name());
    }

    return sb.toString();
  }

  @Override
  public String toString() {
    return fullName();
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
    return name.equals(c.name) && categories.equals(c.categories);
  }

  @Override
  public int compareTo(EntityCategory other) {
    return name.compareTo(other.name);
  }
}
