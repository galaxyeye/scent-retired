package org.qiwur.scent.data.extractor;

import java.io.File;
import java.io.IOException;
import java.util.Set;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.classifier.DomSegmentsBuilder;
import org.qiwur.scent.classifier.DomSegmentsClassifier;
import org.qiwur.scent.classifier.statistics.BlockVarianceCalculator;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.printer.BlockLabelPrinter;
import org.qiwur.scent.printer.DomStatisticsPrinter;
import org.qiwur.scent.utils.ObjectCache;

import ruc.irm.similarity.word.hownet2.concept.BaseConceptParser;

public class WebExtractor {

  public static final Logger logger = LogManager.getLogger(WebExtractor.class);

  private final Configuration conf;
  private final WebLoader loader;

  private WebExtractor(Configuration conf) {
    this.conf = conf;
    this.loader = new WebLoader(conf);

    // logger.info(ConfigurationUtils.toString(conf));

    logger.info("初始化语义计算系统");
    File conceptFile = new File("conf/commerce-concept.xml");
    try {
      BaseConceptParser.load(conceptFile);
    } catch (IOException e) {
      logger.error(e);
    }
  }

  static public WebExtractor create(Configuration conf) {
    ObjectCache objectCache = ObjectCache.get(conf);
    String cacheId = WebExtractor.class.getName();

    if (objectCache.getObject(cacheId) != null) {
      return (WebExtractor) objectCache.getObject(cacheId);
    } else {
      WebExtractor extractor = new WebExtractor(conf);
      objectCache.setObject(cacheId, extractor);
      return extractor;
    }
  }

  public WebLoader getWebLoader() {
    return loader;
  }

  public void refreshFeatures() {

  }

  public PageEntity extract(PageExtractor extractorImpl) {
    Validate.notNull(extractorImpl);
    Document doc = extractorImpl.doc();

    new BlockVarianceCalculator(doc, conf).process();

    Set<DomSegment> segmentSet = new DomSegmentsBuilder(doc, conf).build();

    new DomStatisticsPrinter(doc).process();

    DomSegment[] segments = segmentSet.toArray(new DomSegment[segmentSet.size()]);
    String[] labels = conf.getStrings("scent.segment.labels");
    new DomSegmentsClassifier(segments, labels, conf).classify();

    // logger.info("打印已标注的标签");
    new BlockLabelPrinter(doc, conf).process();

//    new BlockFeatureRecorder(doc, conf).process();

    // logger.info("实体抽取");
    extractorImpl.process();

    return extractorImpl.pageEntity();
  }

}
