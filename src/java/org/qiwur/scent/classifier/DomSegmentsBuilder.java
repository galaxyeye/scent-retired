package org.qiwur.scent.classifier;

import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.block.locator.MenuLocator;
import org.qiwur.scent.block.locator.TitleLocator;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.block.DomSegments;
import org.qiwur.scent.jsoup.block.DomSegmentsUtils;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.IndicatorIndex;
import org.qiwur.scent.jsoup.select.DOMUtil;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

import com.google.common.collect.Multimap;

public class DomSegmentsBuilder {

  static final Logger logger = LogManager.getLogger(DomSegmentsBuilder.class);

  public static final int MinTxtBlkNumber = 4;

  public static final int MinTableChildNumber = 4;

  public static final int MinListItemNumber = 4;

  public static final int MinImageNumberInPureImage = 3;

  public static final int MinLinkNumberInDensyLinkArea = 50;

  // TODO : we should calculate out a threshold value
  public static final double BlockLikihoodThreshold = 0.8;

  private final Document doc;

  private final Configuration conf;

  public DomSegmentsBuilder(Document doc, Configuration conf) {
    this.doc = doc;
    this.conf = conf;
  }

  public DomSegments build() {
    // 建立文档片段集合
    Multimap<Double, Element> blocks = doc.indicatorIndex(IndicatorIndex.Blocks);
    DomSegments segments = doc.domSegments();

    // variance based blocks, the blocks are list-like
    for (Element block : blocks.values()) {
      block.attr("data-blocking-rule", "variance");
      DomSegmentsUtils.addIfNotExist(segments, block);
    }

    // table blocks
    for (Element block : findTables()) {
      block.attr("data-blocking-rule", "table");
      DomSegmentsUtils.addIfNotExist(segments, block);
    }

    // image without links blocks
    for (Element block : findPureImageAreas()) {
      block.attr("data-blocking-rule", "pure-image");
      DomSegmentsUtils.addIfNotExist(segments, block);
    }

    // very dense links blocks
    for (Element block : findDensyLinks()) {
      block.attr("data-blocking-rule", "densy-links");
      DomSegmentsUtils.addIfNotExist(segments, block);
    }

    DomSegment segMenu = findMenu();
    DomSegment segTitle = findTitle();

    Element block = findTitleContainer(segTitle.body());
    DomSegmentsUtils.addOrTag(segments, block, BlockLabel.TitleContainer, FuzzyProbability.MUST_BE);

    // TODO : do not modify conf?
    doc.attr("data-title", segTitle.text());
    doc.attr("data-title-seq", String.valueOf(segTitle.body().sequence()));
    conf.setInt("scent.page.title.sequence", segTitle.body().sequence());

    doc.attr("data-menu-seq", String.valueOf(segMenu.body().sequence()));
    conf.setInt("scent.page.menu.sequence", segMenu.body().sequence());

    doc.attr("data-node-count", String.valueOf(doc.indic(Indicator.D)));
    conf.setInt("scent.page.all.node.count", doc.indic(Indicator.D).intValue());

    // calculate block pattern
    for (DomSegment segment : segments) {
      tagPattern(segment);
    }

    // TODO : make DomSegment be a tree just like jsoup.nodes.Document
    segments.buildTree();

    segments.mergeSegments(BlockLikihoodThreshold);

    logger.debug("there are {} segments", doc.domSegments().size());

    return segments;
  }

  private DomSegment findMenu() {
    DomSegments segments = doc.domSegments();

    // locate page menu
    DomSegment segMenu = new MenuLocator(doc).locate();
    if (segMenu == null) {
      logger.warn("no menu found, create one");
      segMenu = MenuLocator.createMenu(doc, conf);
    }

    // ensure the similarity be very likely at least
    segMenu.tag(BlockLabel.Menu, FuzzyProbability.VERY_LIKELY);
    segments.add(segMenu);

    removeSegmentsBefore(segMenu.root());

    return segMenu;
  }

  private DomSegment findTitle() {
    DomSegments segments = doc.domSegments();

    // locate content title if any
    DomSegment segTitle = new TitleLocator(doc, conf).locate();
    if (segTitle == null) {
      logger.warn("no tilte found, create one");
      segTitle = TitleLocator.createTitle(doc, conf);
    }

    // ensure the similarity be very likely at least
    segTitle.tag(BlockLabel.Title, FuzzyProbability.VERY_LIKELY);
    segments.add(segTitle);

    return segTitle;
  }

  private Element findTitleContainer(Element eleTitle) {
    Validate.notNull(eleTitle);

    return DOMUtil.getContainerSatisfyAny(eleTitle, new Indicator(Indicator.TB, 20), new Indicator(Indicator.D, 50));
  }

  private void removeSegmentsBefore(Element ele) {
    // remove segments before menu
    DomSegments removal = new DomSegments();
    for (DomSegment segment : doc.domSegments()) {
      if (segment.root().sequence() < ele.sequence()) {
        removal.add(segment);
      }
    }
    doc.domSegments().removeAll(removal);
  }

  /*
   * 标注区块模式
   */
  private void tagPattern(DomSegment segment) {
    Element ele = segment.body();

    for (BlockPattern pattern : BlockPattern.patterns) {
      if (BlockPattern.is(ele, pattern)) {
        segment.tag(pattern, FuzzyProbability.MUST_BE);
      }
    }
  }

  // 寻找链接数远小于图片数的区域
  private Elements findPureImageAreas() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.IMG);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _img = entry.getKey();
      Element ele = entry.getValue();

      if (_img < MinImageNumberInPureImage) {
        break;
      }

      if (BlockPattern.isImages(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private Elements findTables() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _child = entry.getKey();
      Element ele = entry.getValue();

      if (_child < MinTableChildNumber) {
        break;
      }

      if (BlockPattern.isTable(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private Elements findDensyLinks() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.A);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _a = entry.getKey();
      Element ele = entry.getValue();

      if (_a < MinLinkNumberInDensyLinkArea) {
        break;
      }

      if (ele.depth() <= doc.body().depth() + 2) {
        continue;
      }

      if (!StringUtil.in(ele.tagName(), "div", "ul")) {
        continue;
      }

      if (BlockPattern.isDensyLinks(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }
}
