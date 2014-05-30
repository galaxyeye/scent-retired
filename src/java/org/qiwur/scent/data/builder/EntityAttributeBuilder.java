package org.qiwur.scent.data.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.EntityCategory;

import com.google.common.collect.Multimap;

public class EntityAttributeBuilder {

  final static Logger logger = LogManager.getLogger(EntityAttributeBuilder.class);

  private final EntityAttributeBuilderImpl builder = EntityAttributeBuilderImpl.create();

  public EntityAttributeBuilder() {
  }

  /*
   * Create a new EntityAttribute object with the given alias and value
   */
  public EntityAttribute build(String alias, String value) {
    return build(alias, value, null);
  }

  /*
   * Create a new EntityAttribute object with the given alias, value and
   * category tag
   */
  public EntityAttribute build(String alias, String value, String categoryName) {
    if (StringUtils.isEmpty(alias) || StringUtils.isEmpty(value))
      return null;

    // 1. 用别名和类别来找标准属性
    EntityAttribute attribute = builder.cloneByAlias(alias, categoryName);
    if (attribute == null) {
      // 2. 用别名来找
      // TODO : what about categoryName?
      attribute = builder.cloneByAliasOnlyOrNull(alias);
    }

    if (attribute != null) {
      attribute.value(value);

      return attribute;
    }

    // 3. 不在标准属性表中，创建一个属性
    EntityCategory category = null;
    if (!StringUtils.isEmpty(categoryName)) {
      category = new EntityCategory(categoryName);
    }

    EntityAttribute attr = new EntityAttribute(alias, value);    
    attr.categorize(category);

    return attr;
  }

  public List<EntityAttribute> build(Multimap<String, String> attributes, String categoryName) {
    List<EntityAttribute> pattrs = new ArrayList<EntityAttribute>();

    if (attributes == null)
      return pattrs;

    for (Entry<String, String> entry : attributes.entries()) {
      EntityAttribute attribute = build(entry.getKey(), entry.getValue(), categoryName);

      if (attribute != null) {
        pattrs.add(attribute);
      }
    }

    return pattrs;
  }

}
