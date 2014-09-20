package org.qiwur.scent.data.builder;

import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.feature.LinedFeature;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

public class EntityBuilder implements Builder {

  static final Logger logger = LogManager.getLogger(EntityBuilder.class);

  public static final Map<String, String> ImageAttributeTransformer = new HashMap<String, String>();

  static {
    ImageAttributeTransformer.put("data-offset-height", "height");
    ImageAttributeTransformer.put("data-offset-width", "width");
  }

  // TODO : configurable
  public static final String[] PermittedAttributes = {
    "id", "class", "title", // permitted global attributes
    "href", // permitted link attributes
    "height", "width", "alt", "src", "" // permitted img attributes
  };

  protected final PageEntity originalPageEntity;
  protected PageEntity pageEntity;
  protected final Configuration conf;

	public EntityBuilder(PageEntity originalPageEntity, Configuration conf) {
	  Validate.notNull(originalPageEntity);
    Validate.notNull(conf);

		this.originalPageEntity = originalPageEntity;
		this.conf = conf;
	}

	@Override
	public void process() {
	}

	public PageEntity pageEntity() {
		return pageEntity;
	}

	private PageEntity normalize(PageEntity pageEntity) {
	  return originalPageEntity;
	}

	protected String buildEntityName(String text) {
	  return text;
	}

  protected void buildColors() {
    final String AttributeName = "color";

    Set<String> colorStrings = new HashSet<String>();

    Collection<EntityAttribute> colorAttributes = pageEntity.get(AttributeName);
    for (EntityAttribute colorAttribute : colorAttributes) {
      colorStrings.addAll(Arrays.asList(StringUtils.split(colorAttribute.value())));
    }
    pageEntity.removeAll(AttributeName);

    // 颜色属性和可选颜色属性
    for (String text : colorStrings) {
//      for (String color : EntityAttrValueFeature.knownColors()) {
//        if (text.contains(color)) {
//          pageEntity.put(new EntityAttribute(AttributeName, color));
//        }
//      }
    }
  }

  // 将网页关键词分解成多个独立的属性
  protected void rebuildKeywords() {
    final String AttributeName = "page-keywords";

    String file = conf.get("scent.bad.page.keywords.file");
    LinedFeature badPageKeywords = FeatureManager.get(conf, LinedFeature.class, file);

    Set<String> keywordStrings = new HashSet<String>();
    Collection<EntityAttribute> keywordAttributes = pageEntity.get(AttributeName);

    for (EntityAttribute keywordAttribute : keywordAttributes) {
      String value = keywordAttribute.value();

      for (String word : badPageKeywords.lines()) {
        value = value.replaceAll(word, "");
      }

      String[] keywords = StringUtils.split(value, ",，、|");

      for (String keyword : keywords) {
        keywordStrings.add(StringUtils.trimToEmpty(keyword));
      }
    }

    // rebuild
    pageEntity.removeAll(AttributeName);
    for (String keyword : keywordStrings) {
      pageEntity.put(new EntityAttribute(AttributeName, keyword));
    }
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

    for (EntityAttribute attr : pageEntity.attributes()) {
      if (!attr.categories().isEmpty()) ++counter;
      categorySortedattributes.put(attr.simpleCategoriesString(), attr);
    }

    formatter.format("\n\n total attributes : %d, categorized : %d \n\n", pageEntity.attributes().size(), counter);

    // build header
    formatter.format("%-20s %-15s %-120s\n", "categories", "name", "value");

    for (EntityAttribute attr : categorySortedattributes.values()) {
      String value = StringUtils.substring(attr.value(), 0, 12000000);
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
