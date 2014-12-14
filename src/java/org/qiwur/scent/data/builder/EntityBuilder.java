package org.qiwur.scent.data.builder;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Formatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.data.entity.EntityAttribute;
import org.qiwur.scent.data.entity.PageEntity;
import org.qiwur.scent.data.feature.FeatureManager;
import org.qiwur.scent.data.feature.LinedFeature;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Attribute;
import org.jsoup.nodes.Attributes;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Maps;
import com.google.common.collect.Multimap;
import com.google.common.collect.Multimaps;
import com.google.common.collect.TreeMultimap;

public class EntityBuilder implements Builder {

  static final Logger logger = LogManager.getLogger(EntityBuilder.class);

  // TODO : configurable
  public static final String[] PermittedAttributes = {
    "id", "class", "title", // permitted global attributes
    "href", // permitted link attributes
    "height", "width", "alt", "src", "" // permitted img attributes
  };

  protected final PageEntity originalPageEntity;
  protected PageEntity pageEntity;
  protected final Configuration conf;

  protected Document doc;

	public EntityBuilder(PageEntity originalPageEntity, Configuration conf) {
	  Validate.notNull(originalPageEntity);
    Validate.notNull(conf);

		this.originalPageEntity = originalPageEntity;
		this.pageEntity = originalPageEntity;
		this.conf = conf;

    try {
      doc = Jsoup.parse(new File("wwwroot/template/page.entity.template.html"), "utf-8", false);
    } catch (IOException e) {
      logger.error(e);
    }
	}

	@Override
	public void process() {
    buildHtml();
	}

  public Document doc() {
    return this.doc;
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

  protected void buildHtml() {
    Validate.notNull(doc);

    Element body = doc.body();
    Element div = body.appendElement("div");
    Element table = div.appendElement("table");
    table.attr("class", "table tablesorter");
    buildTableHead(table, "labels", "key", "value");

    // sort by category
    Multimap<String, EntityAttribute> attributes = TreeMultimap.create();
    for (EntityAttribute attr : pageEntity.attributes()) {
      attributes.put(attr.simpleCategoriesString(), attr);
    }

    // build the table
    for (EntityAttribute attr : attributes.values()) {
      buildTableRow(table, attr.simpleCategoriesString(), attr.name(), attr.value());
    }
  }

  protected Element buildPureImages(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    for (Element img : root.getElementsByTag("img")) {
      img.removeAttr("alt");
    }

    return root;
  }

  protected Element buildLinks(Element root, Collection<EntityAttribute> attributes) {
    for (EntityAttribute attr : attributes) {
      root.append(attr.value());
    }

    return root;
  }

  protected Element buildTableHead(Element table, String col, String col2, String col3) {
    Element tbody = table.appendElement("thead");

    Element tr = tbody.appendElement("tr");

    Element th = tr.appendElement("th");
    Element th2 = tr.appendElement("th");
    Element th3 = tr.appendElement("th");

    th.text(col);
    th2.text(col2);
    th3.append(col3);

    return tr;
  }

  protected Element buildTableRow(Element table, String col, String col2, String col3) {
    Element tr = table.appendElement("tr");

    Element th = tr.appendElement("th");
    Element td = tr.appendElement("td");
    Element td2 = tr.appendElement("td");

    th.attr("data-seperate-line", StringUtils.repeat("-", 100));

    th.text(col);
    td.text(col2);
    td2.append(col3);

    return tr;
  }

  protected Element buildTable(Element root, Collection<EntityAttribute> attributes) {
    Element table = root.appendElement("table");
    for (EntityAttribute attr : attributes) {
      if (attr.value().length() > 300) continue;

      Element tr = table.appendElement("tr");
      Element th = tr.appendElement("td");
      Element td = tr.appendElement("td");

      th.text(attr.name());
      td.text(attr.value());
    }

    return table;
  }

  protected void setAllImageSize(Element root) {
    for (Element img : root.select("img")) {
      int width = getImgWidth(img);

      // if it's greater than 200, just show it as it's original width
      if (width > 200) {
        img.attr("width", String.valueOf(width));
      }

      // probably banner ad
      if (width > 1000) {
        img.remove();
      }
    }
  }

  protected void adjustAttributes(Element root) {
    for (Element ele : root.getAllElements()) {
      Attributes validAttrs = new Attributes();

      if (ele.attributes() != null) {
        for (Attribute attr : ele.attributes()) {
          if (ArrayUtils.contains(PermittedAttributes, attr.getKey())) {
            validAttrs.put(attr);
          }
        }

        ele.clearAttrs();
        ele.attributes().addAll(validAttrs);
      }
      
      if (ele.tagName().equals("a")) {
        ele.attr("target", "_blank");
      }
    }
  }

  protected int getImgWidth(Element image) {
    try {
      return Integer.parseInt(image.attr("data-offset-width"));
    }
    catch(Exception e) {
    }

    return -1;
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
