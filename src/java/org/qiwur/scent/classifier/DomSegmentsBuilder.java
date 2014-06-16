package org.qiwur.scent.classifier;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.block.locator.MenuLocator;
import org.qiwur.scent.block.locator.TitleLocator;
import org.qiwur.scent.feature.EntityNameFeature;
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

  public static final int MinTableChildNumber = 5;

  public static final int MinListItemNumber = 4;

  public static final int MinImageNumberInPureImage = 4;

  public static final int MinLinkNumberInDensyLinkArea = 50;

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

    // variance based blocks
    for (Element block : blocks.values()) {
      if (!block.isSegmented()) {
        segments.add(DomSegment.create(block));
      }
    }

    // table blocks
    for (Element block : findTables()) {
      if (!block.isSegmented()) {
        segments.add(DomSegment.create(block));
      }
    }

    // ul, ol blocks
    for (Element block : findLists()) {
      if (!block.isSegmented()) {
        segments.add(DomSegment.create(block));
      }
    }

    // image without links blocks
    for (Element block : findPureImageAreas()) {
      DomSegmentsUtils.addOrTag(segments, block, BlockLabel.PureImages, FuzzyProbability.MUST_BE);
    }

    // very dense links blocks
    for (Element block : findDensyLinks()) {
      DomSegmentsUtils.addOrTag(segments, block, BlockLabel.DensyLinks, FuzzyProbability.MUST_BE);
    }

    DomSegment segMenu = findMenu();
    DomSegment segTitle = findTitle();

    Element block = findTitleContainer(segTitle.body());
    DomSegmentsUtils.addOrTag(segments, block, BlockLabel.TitleContainer, FuzzyProbability.VERY_LIKELY);

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

    // link images
    for (DomSegment segment : segments) {
      if (segment.veryLikely(BlockPattern.LI)) {
        segment.tag(BlockLabel.LinkImages, FuzzyProbability.VERY_LIKELY);
      }
    }

    // pure links
    for (DomSegment segment : segments) {
      if (segment.veryLikely(BlockPattern.L)) {
        segment.tag(BlockLabel.Links, FuzzyProbability.VERY_LIKELY);
      }
    }

    // similar entities
    for (DomSegment segment : segments) {
      if (segment.veryLikely(BlockPattern.LI)) {
        if (hasSimiliarText(segment, segTitle.text()) || hasSimiliarUrl(segment, doc.baseUri())) {
          segment.tag(BlockLabel.SimilarEntity, FuzzyProbability.VERY_LIKELY);
        }
      }
    }

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

    // 找到第一个包含titleSegment的足够复杂的元素
    final int _txt_blk = 6;
    final int _child = 8;
    final int _descend = 15;

    return DOMUtil.getContainerSatisfyAny(eleTitle, _txt_blk, _child, _descend);
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

    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _child = ele.indic(Indicator.C);
    double _sep = ele.indic(Indicator.SEP);

    // 在抽取的时候，还会有更多的限制条件，来判断分隔符两端的文本是否为属性
    if (_sep >= 3) {
      int count = StringUtils.countMatches(ele.text(), ">");

      if (_sep - count >= 3) {
        segment.tag(BlockPattern.I_I, FuzzyProbability.MUST_BE);
      }
    }

    final List<String> tableTags = Arrays.asList("table", "tbody", "tr");
    if (tableTags.contains(ele.tagName())) {
      segment.tag(BlockPattern.Table, FuzzyProbability.CERTAINLY);
    }

    double sim = segment.labelTracker().get(BlockLabel.DensyLinks);
    if (FuzzyProbability.maybe(sim)) {
      segment.tag(BlockPattern.SIGMA_L, sim);
    }

    // 只考虑三个及以上的列表项
    if (_child <= 3) {
      return;
    }

    double diff = _a - _child;
    double diff2 = 0;
    double diff3 = 0;
    double diff4 = 0;

    // 只考虑三个及以上的链接
    if (_a >= 3) {
      if (_img > 0) {
        // logger.debug("\n\n" + segment + "\n\n");

        // 一个图像一个链接的情况
        diff = Math.abs(_a - _img);
        // 一个图像两个链接的情况
        diff2 = Math.abs(_a - 2 * _img);
        // 一个图像三个链接的情况
        diff3 = Math.abs(_a - 3 * _img);
        // 一个图像四个链接的情况
        diff4 = Math.abs(_a - 4 * _img);

        // 即使相差也不要相差太大
        // TODO : 这个算法不完善
        if (diff <= 2 || diff2 <= 2 || diff3 <= 2 || diff4 <= 2) {
          segment.tag(BlockPattern.LI, FuzzyProbability.MUST_BE);
        }
      }

      boolean mustBeL = false;

      // 情形1，链接数比直接子孩子数多或者相等，也就是每个列表项存在至少一个链接
      // 情形2，链接数比直接子孩子数少，也就是每个列表项存在一个链接，但是有几个布局项（少于3个）
      // 数字0.21指：每10个列表项允许有2个布局标签(20%)
      // TODO : 这个算法不完善
      if (diff >= 0) {
        mustBeL = true;
      } else if (diff > -3 || (-diff / (double) _child) < 0.21) {
        mustBeL = true;
      }

      if (mustBeL) {
        segment.tag(BlockPattern.L, FuzzyProbability.MUST_BE);
      }
    }
  }

  // 寻找链接数远小于图片数的区域
  private Elements findPureImageAreas() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.IMG);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double n = entry.getKey();
      Element ele = entry.getValue();

      if (n < MinImageNumberInPureImage) {
        break;
      }

      // 链接数远小于图片数
      if (ele.indic(Indicator.A) < n / 2) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private Elements findTables() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double n = entry.getKey();
      Element ele = entry.getValue();

      if (n < MinTableChildNumber) {
        break;
      }

      if (ele.tagName().equals("table")) {
        candidates.add(ele);
      }

      if (ele.tagName().equals("tbody")) {
        candidates.add(ele.parent());
      }
    }

    return candidates;
  }

  private Elements findDensyLinks() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.A);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double n = entry.getKey();
      Element ele = entry.getValue();

      if (n < MinLinkNumberInDensyLinkArea) {
        break;
      }

      if (!StringUtil.in(ele.tagName(), "div", "ul")) {
        continue;
      }

      double seq = ele.indic(Indicator.SEQ);
      double a = ele.indic(Indicator.A);
      double img = ele.indic(Indicator.IMG);
      double c = ele.indic(Indicator.C);
      double tb = ele.indic(Indicator.TB);
      double sep = ele.indic(Indicator.SEP);
      double aah = ele.indic(Indicator.AAH);
      double docD = doc.body().indic(Indicator.D);

      if (seq > (docD / 3) || img > a / 5 || c / tb > 8 || sep > 10 || aah > 30) {
        continue;
      }

      candidates.add(ele);
    }

    return candidates;    
  }

  private Elements findLists() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double _child = entry.getKey();
      Element ele = entry.getValue();

      if (_child < MinListItemNumber) {
        break;
      }

      if (StringUtil.in(ele.tagName(), "ul", "ol")) {
        candidates.add(ele);
        continue;
      }

      // calculate the likelihood of the children, if the children are very likely, it's a list-like code block
      double _g = ele.indic(Indicator.G);

      // tooo many grand children
      if (_g / _child > 5) {
        continue;
      }

      Set<String> childTags = new HashSet<String>();
      Set<Double> numGrandson = new HashSet<Double>();

      for (Element child : ele.children()) {
        childTags.add(child.tagName());
        numGrandson.add(child.indic(Indicator.C));
      }

      // 如果列表项的tag标签和直接子孩子数高度相似，那么可以忽略内部结构的相似性，这是对基于方差判断方法的补充
      if (childTags.size() <= 3 && numGrandson.size() <= 3) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private boolean hasSimiliarText(DomSegment segment, String title) {
    int counter = 0;
    int counter2 = 0;
    for (Element ele : segment.body().children()) {
      double sim = 0.0;

      if (!EntityNameFeature.validate(title) || !EntityNameFeature.validate(ele.text())) {
        continue;
      }

      // 由于有些情况下，前面有前导字符如列表数字，故需要截取中间部分比较相似度
      String name = StringUtils.substring(title, 3, 11);
      String name2 = StringUtils.substring(ele.text(), 3, 11);

      if (name.equals(name2)) {
        sim = 1.0;
      }
      else {
        sim = EntityNameFeature.getStandardEditSimilarity(name, name2);
      }

      if (sim > 0.5) {
        logger.debug("ed sim : {}, {}, {}", sim, title, ele.text());
      }

      if (!FuzzyProbability.maybe(sim) && ++counter > 1) {
        return false;
      }

      if (FuzzyProbability.maybe(sim) && ++counter2 > 1) {
        return true;
      }
    }

    return false;
  }

  private boolean hasSimiliarUrl(DomSegment segment, String baseUri) {
    int counter = 0;
    int counter2 = 0;

    for (Element ele : segment.body().getElementsByTag("a")) {
      baseUri = StringUtils.substringBeforeLast(baseUri, "/");
      String href = ele.absUrl("href");
      href = StringUtils.substringBeforeLast(href, "/");

      if (!baseUri.equals(href) && ++counter > 1) {
        return false;
      }

      if (baseUri.equals(href) && ++counter2 > 1) {
        return true;
      }
    }

    return false;
  }
}
