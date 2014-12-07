package org.qiwur.scent.data.builder;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import javax.xml.parsers.ParserConfigurationException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.data.entity.EntityAttribute;
import org.qiwur.scent.data.entity.EntityCategory;
import org.qiwur.scent.jsoup.Jsoup;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.parser.Parser;
import org.xml.sax.SAXException;

import com.google.common.collect.Multimap;
import com.google.common.collect.TreeMultimap;

final class EntityAttributeBuilderImpl {

  private final static Logger logger = LogManager.getLogger(EntityAttributeBuilderImpl.class);

  public static final String configFile = "conf/known-attributes.xml";

  // maintains all attributes
  private ArrayList<EntityAttribute> pattributes = new ArrayList<EntityAttribute>();
  // maintains an index to all attributes
  private Map<String, EntityAttribute> name2attribute = new HashMap<String, EntityAttribute>();
  // maintains an index to all attributes
  private Multimap<String, EntityAttribute> alias2attribute = TreeMultimap.create();

  private static EntityAttributeBuilderImpl instance = null;

  static EntityAttributeBuilderImpl create() {
    if (instance == null) {
      instance = new EntityAttributeBuilderImpl();

      try {
        instance.load();
        instance.buildIndex();
      } catch (IOException | ParserConfigurationException | SAXException e) {
        logger.error(e);
      }
    }

    return instance;
  }

  EntityAttributeBuilderImpl() {
  }

  String configFile() {
    return configFile;
  }

  EntityAttribute cloneByName(String name, String value) {
    EntityAttribute attribute = name2attribute.get(name);

    if (attribute != null) {
      attribute = attribute.clone();
      attribute.value(value);
    }

    return attribute;
  }

  EntityAttribute cloneByAliasOnlyOrNull(String aliase) {
    EntityAttribute attribute = cloneByName(aliase, "");

    if (attribute == null) {
      Collection<EntityAttribute> attributes = alias2attribute.get(aliase);

      if (attributes.size() == 1) {
        return attributes.iterator().next();
      }
    }

    return attribute;
  }

  EntityAttribute cloneByAlias(String aliase, String category) {
    EntityAttribute attribute = cloneByName(aliase, "");

    if (attribute == null && category != null) {
      Collection<EntityAttribute> attributes = alias2attribute.get(aliase);

      for (EntityAttribute attribute2 : attributes) {
        if (attribute2.hasCategory(category))
          return attribute2.clone();
      }
    }

    return attribute;
  }

  private void buildIndex() {
    for (EntityAttribute pattribute : pattributes) {
      name2attribute.put(pattribute.name(), pattribute);
      //
      // for (String aliase : pattribute.aliases()) {
      // alias2attribute.put(aliase, pattribute);
      // }
    }
  }

  private void load() throws IOException, ParserConfigurationException, SAXException {
    Document doc = Jsoup.parse(new FileInputStream(configFile), "utf-8", "", Parser.xmlParser());

    parse(doc);
    buildIndex();
  }

  private void parse(Document doc) {
    for (Element category : doc.select("known-attributes > category")) {
      parseCategory(category, null);
    }
  }

  private void parseCategory(Element category, EntityCategory parentCategory) {
    String name = category.attr("name");

    EntityCategory entityCategory = new EntityCategory(name, parentCategory);
    for (Element subCategory : category.select("category > category")) {
      parseCategory(subCategory, entityCategory);
    }

    for (Element attribute : category.select("category > attribute")) {
      EntityAttribute entityAttribute = parseAttribute(attribute, entityCategory);
      pattributes.add(entityAttribute);
    }
  }

  private EntityAttribute parseAttribute(Element attribute, EntityCategory parentCategory) {
    String name = attribute.attr("name");
    Pattern pattern = Pattern.compile(attribute.attr("patterm"));

    // EntityAttribute entityAttribute = new EntityAttribute(name, pattern,
    // parentCategory);
    EntityAttribute entityAttribute = new EntityAttribute(name, "");
    entityAttribute.categorize(parentCategory);
    Set<String> aliasSet = new HashSet<String>();
    for (Element aliases : attribute.select("attribute > aliases > alias")) {
      aliasSet.add(aliases.text());
    }
    // entityAttribute.aliases(aliasSet);

    return entityAttribute;
  }

  public static void main(String[] args) {
    EntityAttributeBuilderImpl builder = EntityAttributeBuilderImpl.create();
    logger.debug(builder.pattributes.toString());
  }
}
