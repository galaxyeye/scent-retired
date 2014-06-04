package org.qiwur.scent.jsoup.block;

import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.parser.Tag;

import ruc.irm.similarity.FuzzyProbability;

public class DomSegment implements Comparable<DomSegment> {

  private static final Logger logger = LogManager.getLogger(DomSegment.class);

  public static final FuzzyProbability DefaultPassmark = FuzzyProbability.MAYBE;

  // HTML区块的标题最小长度值
  public final static int MinTitleLength = 3;
  // HTML区块的标题最大长度值
  // 经验值，如一个很长的短语（14字符）："看过此商品后顾客买的其它商品"
  public final static int MaxTitleLength = 18;

  Element root = null;
  Element title = null;
  Element body = null;

  BlockLabelTracker labelTracker = new BlockLabelTracker();
  BlockPatternTracker patternTracker = new BlockPatternTracker();

  public DomSegment(Element root, Element title, Element body) {
    if (body == null) {
      logger.warn("Can not construct a null DomSegment");
      return;
    }

    if (title != null && title.text() == "") {
      title = null;
    }

    if (root == null) {
      root = body;
    }

    this.root = root;
    this.title = title;
    this.body = body;
  }

  public DomSegment(Element baseBlock) {
    if (baseBlock == null) {
      logger.warn("Can not construct a null DomSegment");
      return;
    }

    body = baseBlock;

    findBlockRootAndHeader(body);

    if (title != null && title.text() == "") {
      title = null;
    }

    if (root == null) {
      title = null;
      root = body;
    }
  }

  public Element root() {
    return root;
  }

  public Element title() {
    return title;
  }

  public Element body() {
    return body;
  }

  public String titleText() {
    if (title == null)
      return "";

    return title.text();
  }

  public boolean hasTitle() {
    return titleText() != "";
  }

  public String outerHtml() {
    if (root == null)
      return "";

    return root.outerHtml();
  }

  public String html() {
    if (root == null)
      return "";

    return root.html();
  }

  public String text() {
    if (root == null)
      return "";

    return root.text();
  }

  /**
   * return a value between [0, 1]
   * */
  public double likelihood(DomSegment other, double tolerance) {
    return body.likelihood(other.body(), tolerance, Indicator.SEQ);
  }

  public void tag(BlockLabel label) {
    tag(label, FuzzyProbability.VERY_LIKELY.floor());
  }

  public void tag(BlockLabel label, FuzzyProbability p) {
    labelTracker().set(label, p.floor());
  }

  public void tag(BlockLabel label, Double sim) {
    labelTracker().set(label, sim);
  }

  public void unTag(BlockLabel label) {
    labelTracker().remove(label);
  }

  public void tag(BlockPattern pattern) {
    tag(pattern, FuzzyProbability.VERY_LIKELY.floor());
  }

  public void tag(BlockPattern label, FuzzyProbability p) {
    patternTracker().set(label, p.floor());
  }

  public void tag(BlockPattern pattern, Double sim) {
    patternTracker().set(pattern, sim);
  }

  public void unTag(BlockPattern pattern) {
    patternTracker().remove(pattern);
  }

  // 打出及格分，使用默认及格线
  public double markPass(BlockLabel label) {
    return markPass(label, DefaultPassmark);
  }

  // 打出及格分，使用自定义及格线
  public double markPass(BlockLabel label, FuzzyProbability passmark) {
    // 如果已经及格，则直接返回当前分数
    if (is(label, passmark)) {
      return labelTracker().get(label);
    }

    // 如果没有及格，则打出及格分
    return labelTracker().inc(label, passmark.floor());
  }

  // 1分制
  public double grade(BlockLabel label, double score) {
    return labelTracker().inc(label, score);
  }

  // 10分制
  public double grade10(BlockLabel label, double score) {
    return grade(label, score / 10.0);
  }

  public BlockLabelTracker labelTracker() {
    return labelTracker;
  }

  public BlockPatternTracker patternTracker() {
    return patternTracker;
  }

  public BlockLabel primaryLabel() {
    return labelTracker.primaryKey();
  }

  public Set<String> labels() {
    return labelTracker.getLabels();
  }

  public boolean is(BlockLabel label, FuzzyProbability p) {
    return labelTracker.is(label, p);
  }

  public boolean maybe(BlockLabel label) {
    return labelTracker.maybe(label);
  }

  public boolean veryLikely(BlockLabel label) {
    return labelTracker.veryLikely(label);
  }

  public boolean mustBe(BlockLabel label) {
    return labelTracker.mustBe(label);
  }

  public boolean certainly(BlockLabel label) {
    return labelTracker.certainly(label);
  }

  public boolean is(BlockPattern label, FuzzyProbability p) {
    return patternTracker.is(label, p);
  }

  public boolean maybe(BlockPattern label) {
    return patternTracker.maybe(label);
  }

  public boolean veryLikely(BlockPattern label) {
    return patternTracker.veryLikely(label);
  }

  public boolean mustBe(BlockPattern label) {
    return patternTracker.mustBe(label);
  }

  public boolean certainly(BlockPattern label) {
    return patternTracker.certainly(label);
  }

  private void findBlockRootAndHeader(Element baseBlock) {
    if (baseBlock == null)
      return;

    // 向上寻找的层次
    int upwardDepth = 3;
    Element parent = baseBlock.parent();

    while (parent != null && title == null && upwardDepth-- > 0) {
      // 找到的根节点不应该包含太多其他标签
      double numDescend = baseBlock.indic(Indicator.D);
      double numDescend2 = parent.indic(Indicator.D);
      double delta = numDescend2 - numDescend;
      double growth = delta / numDescend;

      // 经验值：
      if (delta > 5 || growth > 0.5) {
        break;
      }

      // 找到的根节点不应该包含太多其他字符块
      double numTxtBlk = baseBlock.indic(Indicator.TB);
      double numTxtBlk2 = parent.indic(Indicator.TB);
      delta = numTxtBlk2 - numTxtBlk;

      // 经验值：字符块数相差3个以内
      // 标题中可能会包含em, strong, span等标签
      if (delta >= 1 && delta <= 3) {
        title = findTitleFromChildren(parent, baseBlock);

        if (title != null) {
          root = parent;
        }
      }

      parent = parent.parent();
    }
  }

  private Element findTitleFromChildren(Element root, Element except) {
    // 优先寻找h标签
    for (Element child : root.children()) {
      if (child != except) {
        if (ArrayUtils.contains(Tag.headerTags, child.tagName())) {
          if (child.hasText()) {
            return child;
          }
        }
      }
    }

    // 如果没有h标签，则寻找其他含文本的标签
    for (Element child : root.children()) {
      if (child != except && child.hasText()) {
        return child;
      }
    }

    // TODO : getLongestTitle

    return null;
  }

  @Override
  public String toString() {
    return root().toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DomSegment)) return false;

    return compareTo((DomSegment)o) == 0;
  }

  @Override
  public int compareTo(DomSegment o) {
    return root.compareTo(o.root);
  }

}