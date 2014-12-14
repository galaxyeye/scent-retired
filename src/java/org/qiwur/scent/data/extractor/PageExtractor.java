package org.qiwur.scent.data.extractor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.nutch.storage.WebPage.Field;
import org.qiwur.scent.data.builder.WebsiteFactory;
import org.qiwur.scent.data.entity.EntityAttribute;
import org.qiwur.scent.data.entity.PageEntity;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.jsoup.block.DomSegments;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.qiwur.scent.learning.EntityAttributeLearner;
import org.qiwur.scent.utils.NetUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PageExtractor implements DataExtractor {

  protected static final Logger logger = LogManager.getLogger(PageExtractor.class);

  private final PageEntity pageEntity = new PageEntity();
  private final Document doc;
  private Configuration conf;

  // TODO : chained extractors
  private Map<DomSegment, DomSegmentExtractor> extractors = Maps.newHashMap();

  public PageExtractor(Document doc, Configuration conf) {
    this.doc = doc;
    this.conf = conf;
  }

  @Override
  public void process() {
    Validate.notNull(doc);
    Validate.notNull(conf);

    cleanDocument();
    // cleanSegments();

    clearPreviousExtractors();
    installExtractors();

    if (logger.isDebugEnabled()) {
      for (DomSegmentExtractor extractor : extractors.values()) {
        logger.debug("installed extractors : {}", extractor);
      }
    }

    extract();
    learn();
  }

  public Document doc() {
    return doc;
  }

  public PageEntity getResult() {
    return pageEntity;
  }

  @SuppressWarnings("unchecked")
  protected void installConfiguredExtractors() {
    String[] blockLabels = conf.getStrings("scent.classifier.block.labels");
    for (String label : blockLabels) {
      BlockLabel blockLabel = BlockLabel.fromString(label);
      String extractorKey = "scent.block.extractors." + label;
      String extractorName = conf.get(extractorKey);

      if (StringUtils.isNotEmpty(extractorName)) {
        Class<? extends DomSegmentExtractor> extractor = null;
        try {
          // TODO : chained extractors, external extractor implementation
          extractor = (Class<? extends DomSegmentExtractor>) Class.forName(extractorName);
        } catch (ClassNotFoundException e) {
          logger.warn("Bad configured block extractor {}", extractorKey);
        }

        if (extractor != null) {
          installExtractor(blockLabel, extractor);
        }
      }
    }
  }

  // TODO : optimize install process, especially class initialization process
  protected void installExtractor(BlockLabel label, Class<? extends DomSegmentExtractor> clazz) {
    for (DomSegment segment : doc.domSegments()) {
      if (segment.veryLikely(label)) {
        addExtractor(getExtractor(segment, clazz, conf));
      }
      else if (segment.maybe(label) && !extractors.containsKey(segment)) {
        addExtractor(getExtractor(segment, clazz, conf));
      }
    }
  }

  protected void installExtractor(BlockPattern pattern, Class<? extends DomSegmentExtractor> clazz) {
    for (DomSegment segment : doc.domSegments()) {
      if (segment.veryLikely(pattern)) {
        addExtractor(getExtractor(segment, clazz, conf));
      }
    }
  }

  protected void addExtractor(DomSegmentExtractor extractor) {
    Validate.notNull(extractor);

    extractors.put(extractor.segment(), extractor);
  }

  protected void learn() {
    EntityAttributeLearner attrLearner = new EntityAttributeLearner(conf);
    attrLearner.learn(pageEntity.attributes());
  }

  protected DomSegments getSegments(String label) {
    return getSegments(BlockLabel.fromString(label));
  }

  protected DomSegments getSegments(String... labels) {
    Validate.notNull(labels);

    List<BlockLabel> blockLabels = Lists.newArrayList();
    for (String label : labels) {
      blockLabels.add(BlockLabel.fromString(label));
    }
    return doc.domSegments().get(blockLabels);
  }

  protected DomSegments getSegments(BlockPattern label) {
    return doc.domSegments().getAll(label);
  }

  protected DomSegments getSegments(BlockLabel label) {
    return doc.domSegments().getAll(label);
  }

  protected boolean hasSegment(BlockLabel label) {
    return doc.domSegments().hasSegment(label);
  }

  protected DomSegmentExtractor getExtractor(DomSegment segment, Class<? extends DomSegmentExtractor> clazz, Configuration conf) {
    DomSegmentExtractor extractor = null;

    try {
      // Class<?> clazz = Class.forName(clazzName, false, this.getClass().getClassLoader());
      Constructor<?> constructor = clazz.getConstructor(DomSegment.class, PageEntity.class, Configuration.class);
      extractor = (DomSegmentExtractor) constructor.newInstance(segment, pageEntity, conf);
    } catch (InstantiationException | IllegalAccessException | NoSuchMethodException
        | SecurityException | IllegalArgumentException | InvocationTargetException e) {
      logger.error(e);
    }

    return extractor;
  }

	@Override
	public Collection<Field> getFields() {
		return null;
	}

	@Override
	public Configuration getConf() {
		return conf;
	}

	@Override
	public void setConf(Configuration conf) {
		this.conf = conf;
	}

  private void cleanDocument() {
    String[] labels = conf.getStrings("scent.extractor.bad.blocks");

    if (labels != null) {
      DomSegments segments = getSegments(labels);
      for (DomSegment segment : segments) {
        doc.evict(segment.block());
      }
    }
  }

  private void cleanSegments() {
    String[] labels = conf.getStrings("scent.extractor.bad.blocks");

    if (labels != null) {
      DomSegments segments = getSegments(labels);
      for (DomSegment segment : segments) {
        segment.remove();
      }
      doc.domSegments().removeAll(segments);
    }
  }

  private void clearPreviousExtractors() {
    extractors.clear();
  }

  private void installExtractors() {
    // 1. install configured extractors
    installConfiguredExtractors();

    // 2. if no extractors, use common extractor
    for (DomSegment segment : doc.domSegments()) {
      if (!extractors.containsKey(segment)) {
        BlockLabel label = segment.primaryLabel();
        String displayLabel = label != null ? label.text() : null;
        addExtractor(new DomSegmentExtractor(segment, pageEntity, displayLabel));
      }
    }
  }

  private PageEntity extract() {
    // specified extractors
    for (DomSegmentExtractor extractor : extractors.values()) {
      extractor.process();
    }

    // Metadata
    String domain = NetUtil.getDomain(doc.baseUri());
    if (StringUtils.isNotEmpty(domain)) {
      pageEntity.put(getSoureLink(doc.baseUri()));
      pageEntity.put(getWebsiteDomain(domain));
      pageEntity.put(getWebsiteName(domain));
    }

    pageEntity.put(getPageKeywords());
    pageEntity.put(getPageDescription());

    return pageEntity;
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
    return new EntityAttribute("page-keywords", meta.attr("content"), "Metadata");
  }

  private EntityAttribute getPageDescription() {
    Elements meta = doc.select("html head meta[name=description]");
    return new EntityAttribute("page-description", meta.attr("content"), "Metadata");
  }	
}
