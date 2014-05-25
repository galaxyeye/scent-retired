package org.qiwur.scent.classifier;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.block.locator.MenuLocator;
import org.qiwur.scent.block.locator.TitleLocator;
import org.qiwur.scent.feature.ProductNameFeature;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.block.DomSegments;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.IndicatorIndex;
import org.qiwur.scent.jsoup.select.ElementTreeUtil;
import org.qiwur.scent.jsoup.select.Elements;
import org.qiwur.scent.utils.StringUtil;

import ruc.irm.similarity.FuzzyProbability;

import com.google.common.collect.Multimap;

public class DomSegmentsBuilder {

  static final Logger logger = LogManager.getLogger(DomSegmentsBuilder.class);

  public static final int MinTableChildNumber = 5;

  public static final int MinListItemNumber = 4;

  public static final int MinChildNumberInListLikeArea = 5;

  public static final int MinImageNumberInPureImage = 4;

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

    // variance based block recognition
    for (Element block : blocks.values()) {
      segments.add(new DomSegment(block));
    }

    // tables
    for (Element block : findTables()) {
      segments.add(new DomSegment(null, null, block));
    }

    // uls
    for (Element block : findLists()) {
      segments.add(new DomSegment(null, null, block));
    }

    // image without links
    for (Element block : findPureImageAreas()) {
      DomSegment segment = new DomSegment(null, null, block);
      segment.tag(BlockLabel.Images, FuzzyProbability.MUST_BE);
      segments.add(segment);
    }

    // list like
    for (Element block : findListLikeAreas()) {
      segments.add(new DomSegment(null, null, block));
    }

    for (DomSegment segment : segments) {
      // 计算文档片段的模式
      tagBlockPatterns(segment);
    }

    rebuildLabelsConfigValue();

    // locate page menu
    DomSegment menu = new MenuLocator(doc).locate();
    if (menu != null) {
      segments.add(menu);
      conf.setInt("scent.page.menu.sequence", menu.body().sequence());

      removeSegmentsBefore(menu.root());
    }

    conf.setInt("scent.page.all.node.count", doc.indic(Indicator.D).intValue());

    // TODO : since the DOM has been rebuilt, the node sequence may change

    // locate content title if any
    DomSegment title = new TitleLocator(doc).locate();
    if (title == null) {
      title = TitleLocator.createTitle(doc);
    }
    segments.add(title);
    conf.set("scent.page.title.text", title.text());
    conf.setInt("scent.page.title.sequence", title.body().sequence());

    // TODO : configurable for non-common locators
    buildProductShowSegment();

    for (DomSegment segment : segments) {
      if (segment.veryLikely(BlockPattern.LI)) {
        segment.tag(BlockLabel.Images);
      }

      if (segment.veryLikely(BlockPattern.L)) {
        segment.tag(BlockLabel.Links);

        // tag similar entities
        if (hasSimiliarText(segment, title.text()) || hasSimiliarUrl(segment, doc.baseUri())) {
          segment.tag(BlockLabel.SimilarEntity, FuzzyProbability.VERY_LIKELY);
        }
      }
    }

    logger.debug("there are {} segments", doc.domSegments().size());

    return segments;
  }

  // TODO : use configured rules
  private void buildProductShowSegment() {
    DomSegments segments = doc.domSegments();
    DomSegment titleSegment = segments.get(BlockLabel.Title, FuzzyProbability.VERY_LIKELY);

    if (titleSegment != null) {
      // 找到第一个包含titleSegment的足够复杂的元素
      final int _txt_blk = 6;
      final int _child = 8;
      final int _descend = 15;
      Element container = ElementTreeUtil.getContainerSatisfyAny(titleSegment.body(), _txt_blk, _child, _descend);

      if (container != null) {
        DomSegment segment = new DomSegment(null, null, container);
        BlockLabel label = BlockLabel.fromString("ProductShow");
        segment.tag(label, FuzzyProbability.VERY_LIKELY);
        segments.add(segment);
      } else {
        logger.warn("can not fin product show");
      }
    }
  }

  private void rebuildLabelsConfigValue() {
    Collection<String> labels = conf.getStringCollection("scent.segment.labels");
    for (BlockLabel label : BlockLabel.labels) {
      if (!labels.contains(label.text())) {
        labels.add(label.text());
      }
    }
    conf.set("scent.segment.labels", StringUtils.join(labels, ","));
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
  private void tagBlockPatterns(DomSegment segment) {
    Element element = segment.body();

    if (element == null) {
      return;
    }

    // 在抽取的时候，还会有更多的限制条件，来判断分隔符两端的文本是否为属性
    if (element.indic(Indicator.SEP) > 3) {
      segment.tag(BlockPattern.I_I, FuzzyProbability.MUST_BE);
    }

    final List<String> tableTags = Arrays.asList("table", "tbody", "tr");

    if (tableTags.contains(element.tagName())) {
      segment.tag(BlockPattern.Table, FuzzyProbability.CERTAINLY);
    }

    double numLinks = element.indic(Indicator.A);
    double numImages = element.indic(Indicator.IMG);
    double numChildren = element.indic(Indicator.C);

    // 只考虑三个及以上的列表项
    if (numChildren <= 3) {
      return;
    }

    double diff = numLinks - numChildren;
    double diff2 = 0;
    double diff3 = 0;
    double diff4 = 0;

    // 只考虑三个及以上的链接
    if (numLinks >= 3) {
      if (numImages > 0) {
        // logger.debug("\n\n" + segment + "\n\n");

        // 一个图像一个链接的情况
        diff = Math.abs(numLinks - numImages);
        // 一个图像两个链接的情况
        diff2 = Math.abs(numLinks - 2 * numImages);
        // 一个图像三个链接的情况
        diff3 = Math.abs(numLinks - 3 * numImages);
        // 一个图像四个链接的情况
        diff4 = Math.abs(numLinks - 4 * numImages);

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
      } else if (diff > -3 || (-diff / (double) numChildren) < 0.21) {
        mustBeL = true;
      }

      if (mustBeL) {
        segment.tag(BlockPattern.L, FuzzyProbability.MUST_BE);
      }
    } // numLinks >= 3
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

  private Elements findLists() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double n = entry.getKey();
      Element ele = entry.getValue();

      if (n < MinListItemNumber) {
        break;
      }

      if (ele.tagName().equals("ul")) {
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

  private Elements findListLikeAreas() {
    Elements candidates = new Elements();
    Multimap<Double, Element> indicatorIndex = doc.indicatorIndex(Indicator.C);

    for (Entry<Double, Element> entry : indicatorIndex.entries()) {
      double n = entry.getKey();
      Element ele = entry.getValue();

      if (n < MinChildNumberInListLikeArea) {
        break;
      }

      if (!StringUtil.in(ele.tagName(), "div", "ul")) {
        continue;
      }

      Set<String> childTags = new HashSet<String>();
      Set<Double> numGrandson = new HashSet<Double>();
      for (Element child : ele.children()) {
        childTags.add(child.prettyName());
        numGrandson.add(child.indic(Indicator.C));
      }

      // 如果列表项的tag标签和直接子孩子数高度相似，那么可以忽略内部结构的相似性，这是对基于方差判断方法的补充
      if (childTags.size() < 3 && numGrandson.size() < 3) {
        candidates.add(ele);
      }
    }

    return candidates;
  }

  private boolean hasSimiliarText(DomSegment segment, String title) {
    int counter = 0;
    int counter2 = 0;
    for (Element ele : segment.body().children()) {
      double sim = ProductNameFeature.getTitleSimilarity(title, ele.text());

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
