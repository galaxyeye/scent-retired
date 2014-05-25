package org.qiwur.scent.data.extractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.data.builder.WebsiteFactory;
import org.qiwur.scent.entity.EntityAttribute;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.EntityAttrValueFeature;
import org.qiwur.scent.feature.LinedFeature;
import org.qiwur.scent.feature.LinedFeatureFactory;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.block.DomSegments;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.learning.EntityAttributeLearner;
import org.qiwur.scent.utils.NetUtil;

public class PageExtractor implements DataExtractor {

  protected static final Logger logger = LogManager.getLogger(PageExtractor.class);

  protected final PageEntity pageEntity = new PageEntity();
  protected final Configuration conf;
  protected final Document doc;

  protected final LinedFeature badPageKeywords;
  protected final EntityAttributeLearner attrLearner;

  protected List<DomSegmentExtractor> extractors = new ArrayList<DomSegmentExtractor>();
  protected List<DomSegment> processedSegments = new ArrayList<DomSegment>();

  private static Map<String, String> configuratedExtractors = new HashMap<String, String>();

  static {
    // TODO : load from configuration
    configuratedExtractors.put(BlockLabel.Categories.text(), CategoriesExtractor.class.getName());
    configuratedExtractors.put(BlockLabel.Title.text(), TitleExtractor.class.getName());
    configuratedExtractors.put(BlockLabel.Gallery.text(), GalleryExtractor.class.getName());
    configuratedExtractors.put(BlockLabel.Links.text(), LinksExtractor.class.getName());
    configuratedExtractors.put(BlockLabel.Images.text(), ImagesExtractor.class.getName());
    configuratedExtractors.put(BlockLabel.SimilarEntity.text(), SimilarEntityExtractor.class.getName());
  }

  public PageExtractor(Document doc, Configuration conf) {
    Validate.notNull(doc);
    Validate.notNull(conf);

    this.doc = doc;
    this.conf = conf;
    this.attrLearner = new EntityAttributeLearner(conf);

    String file = conf.get("scent.bad.page.keywords.file");
    badPageKeywords = new LinedFeatureFactory(file, conf).getFeature();
  }

  @Override
  public void process() {
    Validate.notNull(doc);

    removeBadBlocks();

    installExtractors();

    extract();
    learn();
  }

  public Document doc() {
    return doc;
  }

  public PageEntity pageEntity() {
    return pageEntity;
  }

  protected void removeBadBlocks() {
    doc.domSegments().removeAll(getSegments(BlockLabel.BadBlock));
  }

  protected void installExtractors() {
    for (Entry<String, String> entry : configuratedExtractors.entrySet()) {
      for (DomSegment segment : getSegments(entry.getKey())) {
        addExtractor(getExtractor(segment, entry.getValue(), conf));
      }
    }
  }

  protected boolean addExtractor(DomSegmentExtractor extractor) {
    if (extractor == null) return false;

    return extractors.add(extractor);
  }

  protected PageEntity extract() {
    logger.debug("{} specified extractors", extractors.size());

    // specified extractors
    for (DomSegmentExtractor extractor : extractors) {
      extractor.process();
      processedSegments.add(extractor.segment());
    }

    int counter = 0;
    // common extractors
    for (DomSegment segment : doc.domSegments()) {
      if (!processedSegments.contains(segment)) {
        ++counter;
        new DomSegmentExtractor(segment, pageEntity, segment.primaryLabel()).process();
      }
    }
    logger.debug("{} common extractors", counter);

    // common attributes
    String domain = NetUtil.getDomain(doc.baseUri());
    pageEntity.put(getSoureLink(doc.baseUri()));
    pageEntity.put(getWebsiteDomain(domain));
    pageEntity.put(getWebsiteName(domain));
    pageEntity.put(getPageKeywords());
    pageEntity.put(getPageDescription());

    // unclassified attributes
    processUnclassifiedAttributes();

    return pageEntity;
  }

  protected void rebuild() {
//    rebuildColors();
//    rebuildKeywords();
  }

  protected void filter() {
    // 过滤敏感词
//    new EntityAttrValueFilter(pageEntity, conf).process();
  }

  protected void learn() {
    attrLearner.learn(pageEntity.getAll());
  }

  // 未分类属性集
  private void processUnclassifiedAttributes() {
//    String file = conf.get("scent.bad.attribute.name.file");
//    LinedFeature badAttrNames = new LinedFeatureFactory(file, conf).getFeature();
//
//    for (String name : pageEntity.strAttributes().keySet()) {
//      for (String value : pageEntity.strAttributes().get(name)) {
//        if (!badAttrNames.contains(name) && !pageEntity.contains(name, value)) {
//          pageEntity.put(new EntityAttribute(name, value));
//        }
//      }
//    }
  }

  private EntityAttribute getWebsiteDomain(String domain) {
    return new EntityAttribute("domain", domain, "Metadata");
  }

  private EntityAttribute getWebsiteName(String domain) {
    String siteName = WebsiteFactory.getInstance().getName(domain);
    return new EntityAttribute("website-name", siteName, "Metadata");
  }

  private EntityAttribute getSoureLink(String baseUri) {
    return new EntityAttribute("link", baseUri, "Metadata");
  }

  private EntityAttribute getPageKeywords() {
    Elements meta = doc.select("html head meta[name=keywords]");
    return new EntityAttribute("page-keyword", meta.attr("content"), "Metadata");
  }

  private EntityAttribute getPageDescription() {
    Elements meta = doc.select("html head meta[name=description]");
    return new EntityAttribute("page-description", meta.attr("content"), "Metadata");
  }

  private void rebuildColors() {
    final String AttributeName = "color";

    Set<String> colorStrings = new HashSet<String>();

    Collection<EntityAttribute> colorAttributes = pageEntity.get(AttributeName);
    for (EntityAttribute colorAttribute : colorAttributes) {
      colorStrings.addAll(Arrays.asList(StringUtils.split(colorAttribute.value())));
    }
    pageEntity.removeAll(AttributeName);

    // 颜色属性和可选颜色属性
    for (String text : colorStrings) {
      for (String color : EntityAttrValueFeature.knownColors()) {
        if (text.contains(color)) {
          pageEntity.put(new EntityAttribute(AttributeName, color));
        }
      }
    }
  }

  // 将网页关键词分解成多个独立的属性
  private void rebuildKeywords() {
    final String AttributeName = "page-keyword";

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

  protected DomSegments getSegments(String label) {
    return getSegments(BlockLabel.fromString(label));
  }

  protected DomSegments getSegments(BlockLabel label) {
    return doc.domSegments().getAll(label);
  }

  protected boolean hasSegment(BlockLabel label) {
    return doc.domSegments().hasSegment(label);
  }

  // TODO : use plugin mechanism
  protected DomSegmentExtractor getExtractor(DomSegment segment, String clazzName, Configuration conf) {
    DomSegmentExtractor extractor = null;

    try {
      Class<?> clazz = Class.forName(clazzName, false, this.getClass().getClassLoader());
      Constructor<?> constructor = clazz.getConstructor(new Class[] { DomSegment.class, PageEntity.class,
          Configuration.class });
      extractor = (DomSegmentExtractor) constructor.newInstance(segment, pageEntity, conf);
    } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | NoSuchMethodException
        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      logger.error(e);
    }

    return extractor;
  }
}
