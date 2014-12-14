package org.qiwur.scent.segment;

import java.util.Map.Entry;

import org.apache.commons.lang.Validate;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.jsoup.block.DomSegments;
import org.jsoup.block.DomSegmentsUtils;
import org.jsoup.helper.FuzzyProbability;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Indicator;
import org.jsoup.nodes.IndicatorIndex;
import org.jsoup.select.DOMUtil;
import org.jsoup.select.Elements;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Multimap;

public class DomSegmentsBuilder {

  static final Logger logger = LogManager.getLogger(DomSegmentsBuilder.class);

  public static final int MinTxtBlkNumber = 4;

  public static final int MinTableChildNumber = 4;

  public static final int MinListItemNumber = 4;

  public static final int MinImageNumberInPureImage = 3;

  public static final int MinLinkNumberInDenseLinkArea = 50;

  // TODO : we should calculate out a threshold value
  public static final double BlockLikelihoodThreshold = 0.8;

  private final Document doc;

  private final Configuration conf;

  public DomSegmentsBuilder(Document doc, Configuration conf) {
    this.doc = doc;
    this.conf = conf;
  }

  public DomSegments build() {
    // 建立文档片段集合
    DomSegments segments = doc.domSegments();

    // table blocks
    for (Element block : findTables()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "table");
    }

    // ul, ol, list like blocks
    for (Element block : findLists()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "list");
    }

    // dl blocks
    for (Element block : findDls()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "dl");
    }

    // image without links blocks
    for (Element block : findPureImageAreas()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "image");
    }

    // very dense links blocks
    for (Element block : findDenseLinks()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "dense link");
    }

    // variance based blocks, the blocks are list-like
    Multimap<Double, Element> blocks = doc.indicatorIndex(IndicatorIndex.Blocks);
    for (Element block : blocks.values()) {
      DomSegmentsUtils.addIfNotExist(segments, block, "variance");
    }

    // manual defined blocks, they are defined using CSS path in configuration file
    for (Element manualBlock : findManualDefinedBlocks()) {
      DomSegmentsUtils.addIfNotExist(segments, manualBlock, "manual");
    }

    DomSegment segMenu = findMenu();
    DomSegment segTitle = findTitle();

    Element block = findTitleContainer(segTitle.block());
    DomSegmentsUtils.addOrTag(segments, block, BlockLabel.TitleContainer, FuzzyProbability.MUST_BE);

    // calculate block pattern
    for (DomSegment segment : segments) {
      tagPattern(segment);
    }

    // body should not be a segment
    DomSegmentsUtils.removeByBlock(segments, doc.body());

    // link all segments as a tree like structure
    // TODO : make DomSegment be a tree just like jsoup.nodes.Document
    DomSegmentsUtils.buildTree(segments);

    DomSegmentsUtils.mergeSegments(segments, BlockLikelihoodThreshold);

    rebuildSegmentTree();

//    Element test = doc.select("tbody").first();
//    logger.debug(test.strippedText());

    // set global variables
    doc.attr("data-title", segTitle.text());
    conf.setInt("scent.page.title.sequence", segTitle.block().sequence());
    conf.setInt("scent.page.menu.sequence", segMenu.block().sequence());
    conf.setInt("scent.page.all.node.count", doc.indic(Indicator.D).intValue());

    // logger.debug("{} segments in {}", doc.domSegments().size(), doc.baseUri());

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

    adjustMenu(segMenu.block());

    // ensure the similarity be very likely
    segMenu.tag(BlockLabel.Menu, FuzzyProbability.VERY_LIKELY);
    segments.add(segMenu);

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

  private void adjustMenu(Element menu) {
    for (Element ele : menu.getAllElements()) {
      if (MenuLocator.badWords.contains(ele.ownText())) {
        ele.remove();
      }
    }
  }

  /**
   * Some areas such as dense link areas should be unabridged blocks,
   * so they should not has children segments
   * */
  private void rebuildSegmentTree() {
    DomSegments removal = new DomSegments();
    for (DomSegment segment : doc.domSegments()) {
      // keep segments who is already labeled, because it must be very important
      if (segment.labelTracker().empty() && segment.hasParent() && isNoDescendantSegment(segment.parent())) {
        if (logger.isDebugEnabled()) {
          // logger.debug("remove illegal segment : {}, parent : {}", segment.name(), segment.parent().name());
        }

        segment.parent().removeChild(segment);
        removal.add(segment);
      }
    }

    doc.domSegments().removeAll(removal);
  }

  /**
   * The segments that has no descendants
   * */
  private boolean isNoDescendantSegment(DomSegment segment) {
    Validate.notNull(segment);

    final BlockPattern[] noChildPatterns = {BlockPattern.DenseLinks, BlockPattern.Table, BlockPattern.Dl};

    for (BlockPattern pattern : noChildPatterns) {
      if (segment.veryLikely(pattern)) return true;
    }

    return false;
  }

  /**
   * 标注区块模式
   * Tag segment patterns
   */
  private void tagPattern(DomSegment segment) {
    Element ele = segment.block();

    for (BlockPattern pattern : BlockPattern.patterns) {
      if (BlockPattern.is(ele, pattern)) {
        segment.tag(pattern, FuzzyProbability.MUST_BE);
      }
    }
  }

  /**
   * Found out areas where are many images but few links
   * 寻找链接数远小于图片数的区域
   * */
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

  /**
   * Find out restricted tables, which are not layout tables, and contains valuable information
   * */
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

  /**
   * 
   * */
  private Elements findDls() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _child = entry.getKey();
      Element ele = entry.getValue();

      if (_child < MinListItemNumber) {
        break;
      }

      double _txt_blk = ele.indic(Indicator.TB);
      if (_txt_blk < MinTxtBlkNumber) {
        continue;
      }

      if (BlockPattern.isDl(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  /**
   * 
   * */
  private Elements findLists() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _child = entry.getKey();
      Element ele = entry.getValue();

      if (_child < MinListItemNumber) {
        break;
      }

      double _txt_blk = ele.indic(Indicator.TB);
      if (_txt_blk < MinTxtBlkNumber) {
        continue;
      }

      if (BlockPattern.listLikely(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private Elements findDenseLinks() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.A);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _a = entry.getKey();
      Element ele = entry.getValue();

      if (_a < MinLinkNumberInDenseLinkArea) {
        break;
      }

      if (ele.depth() <= doc.body().depth() + 2) {
        continue;
      }

      if (!StringUtil.in(ele.tagName(), "div", "ul")) {
        continue;
      }

      if (BlockPattern.isDenseLinks(ele)) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private Elements findManualDefinedBlocks() {
    Elements candidates = new Elements();

    for (String selector : conf.getStrings("scent.block.manual.defined", ArrayUtils.EMPTY_STRING_ARRAY)) {
      candidates.addAll(doc.select(selector));
    }

    for (Element ele : candidates) {
      ele.addClass("scent-manual-defined");
    }

    return candidates;
  }
}
