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
import org.qiwur.scent.diagnosis.BlockLabelFormatter;
import org.qiwur.scent.diagnosis.BlockPatternFormatter;
<<<<<<< HEAD
import org.qiwur.scent.diagnosis.BlockVarianceFormatter;
=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.qiwur.scent.diagnosis.DomSegmentFormatter;
import org.qiwur.scent.diagnosis.IndicatorsFormatter;
import org.qiwur.scent.diagnosis.ScentDiagnoser;
import org.qiwur.scent.entity.PageEntity;
import org.qiwur.scent.feature.FeatureManager;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
<<<<<<< HEAD
=======
import org.qiwur.scent.learning.BlockFeatureRecorder;
import org.qiwur.scent.printer.BlockLabelPrinter;
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.qiwur.scent.utils.ObjectCache;

import ruc.irm.similarity.word.hownet2.concept.BaseConceptParser;

public class WebExtractor {

  public static final Logger logger = LogManager.getLogger(WebExtractor.class);

  private final Configuration conf;
  private final WebLoader loader;

  private WebExtractor(Configuration conf) {
    this.conf = conf;
    this.loader = new WebLoader(conf);

    logger.info("initialize NLP system");
    try {
      BaseConceptParser.load(new File("conf/commerce-concept.xml"));
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
    // just for debug mode
    FeatureManager.create(conf).reloadAll();
  }

  public PageEntity extract(PageExtractor extractorImpl) {
    Validate.notNull(extractorImpl);

    // reload features if necessary
    refreshFeatures();

    Document doc = extractorImpl.doc();

<<<<<<< HEAD
    // Calculate all indicators, this step is essential for the extraction
    doc.calculateIndicators();

    ScentDiagnoser diagnoser = new ScentDiagnoser(doc, conf);
    diagnoser.addFormatter(new IndicatorsFormatter(doc, conf));
    diagnoser.addFormatter(new BlockPatternFormatter(doc, conf));

    // calculate code blocks
    BlockVarianceCalculator calculator = new BlockVarianceCalculator(doc, conf);
    calculator.calculate();
    diagnoser.addFormatter(new BlockVarianceFormatter(calculator, conf));
=======
    ScentDiagnoser diagnoser = new ScentDiagnoser(doc, conf);

    // calculate code blocks
    new BlockVarianceCalculator(doc, conf).process();

    diagnoser.addFormatter(new IndicatorsFormatter(doc, conf));
    diagnoser.addFormatter(new DomSegmentFormatter(doc, conf));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

    // build code segments, calculate code patterns
    Set<DomSegment> segmentSet = new DomSegmentsBuilder(doc, conf).build();

    // classify each code segment, add proper tags
    DomSegment[] segments = segmentSet.toArray(new DomSegment[segmentSet.size()]);
    String[] labels = conf.getStrings("scent.classifier.block.labels");
    DomSegmentsClassifier classifier = new DomSegmentsClassifier(segments, labels, conf);
    classifier.classify();
    classifier.report(diagnoser);

<<<<<<< HEAD
    diagnoser.addFormatter(new DomSegmentFormatter(doc, conf));
=======
    diagnoser.addFormatter(new BlockPatternFormatter(doc, conf));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    diagnoser.addFormatter(new BlockLabelFormatter(doc, conf));

    // new BlockFeatureRecorder(doc, conf).process();

    // extract each code segment
    extractorImpl.process();

    diagnoser.diagnose();

    return extractorImpl.pageEntity();
  }
}
