package org.qiwur.scent.jsoup.block;

import java.util.List;
import java.util.Set;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.Validate;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.parser.Tag;

import ruc.irm.similarity.FuzzyProbability;

import com.google.common.collect.Lists;

public class DomSegment implements Comparable<DomSegment> {

  public static final Logger logger = LogManager.getLogger(DomSegment.class);

  public static final FuzzyProbability DefaultPassmark = FuzzyProbability.MAYBE;

  // HTML区块的标题最小长度值
  public final static int MinTitleLength = 3;
  // HTML区块的标题最大长度值
  // 经验值，如一个很长的短语（14字符）："看过此商品后顾客买的其它商品"
  public final static int MaxTitleLength = 18;

  private Element root = null;
  private Element title = null;
  private Element block = null;

  private DomSegment parent = null;
  private List<DomSegment> children = Lists.newArrayList();

  BlockLabelTracker labelTracker = new BlockLabelTracker();
  BlockPatternTracker patternTracker = new BlockPatternTracker();

  public DomSegment(Element root, Element title, Element block) {
    Validate.notNull(block);

    if (title != null && title.text() == "") {
      title = null;
    }

    if (root == null) {
      root = block;
    }

    this.root = root;
    this.title = title;
    this.block = block;

    root.attr("data-segmented", "1");
    block.attr("data-segmented", "1");
  }

  public DomSegment(Element baseBlock) {
    Validate.notNull(baseBlock);

    block = baseBlock;

    findBlockRootAndHeader(block);

    if (title != null && title.text() == "") {
      title = null;
    }

    if (root == null) {
      title = null;
      root = block;
    }

    root.attr("data-segmented", "1");
    block.attr("data-segmented", "1");
  }

  public static DomSegment create(Element block) {
    return new DomSegment(null, null, block);
  }

  public static DomSegment create(Element block, BlockLabel label) {
    return create(block, label, FuzzyProbability.VERY_LIKELY);
  }

  public static DomSegment create(Element block, String label, FuzzyProbability p) {
    return create(block, BlockLabel.fromString(label), p);
  }

  public static DomSegment create(Element block, BlockLabel label, FuzzyProbability p) {
    DomSegment segment = new DomSegment(null, null, block);

    segment.tag(label, p);

    return segment;
  }

  public Element title() {
    return title;
  }

  public Element block() {
    Validate.notNull(block);

    return block;
  }

  public String titleText() {
    if (title == null)
      return "";

    return title.text();
  }

  public int baseSequence() {
    return root.sequence();
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

  public boolean hasParent() {
    return this.parent != null;
  }

  public DomSegment parent() {
    return this.parent;
  }

  public void parent(DomSegment parent) {
    this.parent = parent;
  }

  public List<DomSegment> children() {
    return this.children;
  }

  /**
  * append the specified segment to the children list, make it's parent be this segment
  * */
  public void appendChild(DomSegment child) {
    if (child != null) {
      this.children.add(child);
      child.parent(this);
    }
  }

  public boolean hasChild() {
    return !children.isEmpty();
  }

  /**
   * remove this segment from the tree, make it's parent be null,
   * append all it's child to it's parent if any
   * */
  public void remove() {
    for (DomSegment child : children) {
      child.parent(parent);
    }

    if (parent != null) {
      parent.removeChild(this);
      this.parent = null;
    }
  }

  /**
   * remove the specified segment from the children, make it's parent be null,
   * */
  public void removeChild(DomSegment child) {
    if (child != null) {
      this.children.remove(child);
      child.parent(null);
    }
  }

  /**
   * return a value between [0, 1]
   * */
  public double likelihood(DomSegment other) {
    final String[] indicators = {
        Indicator.CH, Indicator.TB,
        Indicator.A, Indicator.IMG,
        Indicator.D, Indicator.C, Indicator.G
    };

    return block.likelihood(other.block(), indicators);
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

  public boolean is(BlockPattern pattern, FuzzyProbability p) {
    return patternTracker.is(pattern, p);
  }

  public boolean maybe(BlockPattern pattern) {
    return patternTracker.maybe(pattern);
  }

  public boolean veryLikely(BlockPattern pattern) {
    return patternTracker.veryLikely(pattern);
  }

  public boolean mustBe(BlockPattern pattern) {
    return patternTracker.mustBe(pattern);
  }

  public boolean certainly(BlockPattern pattern) {
    return patternTracker.certainly(pattern);
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

  public String name() {
    return block().prettyName();
  }

  @Override
  public String toString() {
    return block().toString();
  }

  @Override
  public int hashCode() {
    return block.hashCode();
  }
  
  @Override
  public boolean equals(Object o) {
    if (!(o instanceof DomSegment)) return false;

    return compareTo((DomSegment)o) == 0;
  }

  @Override
  public int compareTo(DomSegment o) {
    return block.compareTo(o.block);
  }
}
