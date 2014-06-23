package org.qiwur.scent.jsoup.block;

import java.util.HashSet;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Sets;

public class BlockPattern implements Comparable<BlockPattern> {

  public static BlockPattern N2 = BlockPattern.fromString("N2");
  public static BlockPattern II = BlockPattern.fromString("II");
  public static BlockPattern Table = BlockPattern.fromString("Table");
  public static BlockPattern Dl = BlockPattern.fromString("Dl");
  public static BlockPattern List = BlockPattern.fromString("List");

  public static BlockPattern Links = BlockPattern.fromString("Links");
  public static BlockPattern DensyLinks = BlockPattern.fromString("DensyLinks");
  public static BlockPattern Images = BlockPattern.fromString("Images");
  public static BlockPattern LinkImages = BlockPattern.fromString("LinkImages");
  public static BlockPattern DensyText = BlockPattern.fromString("DensyText");

  public static Set<BlockPattern> patterns = Sets.newHashSet();

  static {
    patterns.add(N2);
    patterns.add(II);
    patterns.add(Table);
    patterns.add(Dl);
    patterns.add(List);

    patterns.add(Links);
    patterns.add(DensyLinks);
    patterns.add(Images);
    patterns.add(LinkImages);
    patterns.add(DensyText);
  }

  private String text;

  private BlockPattern(String text) {
    this.text = text;
  }

  public String text() {
    return text;
  }

  public static BlockPattern fromString(String text) {
    if (text != null) {
      return new BlockPattern(text);
    }

    return null;
  }

  public static boolean is(Element ele, BlockPattern pattern) {
    if (pattern.equals(N2)) {
      return isN2(ele);
    }

    if (pattern.equals(II)) {
      return isII(ele);
    }

    if (pattern.equals(Table)) {
      return isTable(ele);
    }

    if (pattern.equals(List)) {
      return isList(ele);
    }

    if (pattern.equals(Dl)) {
      return isDl(ele);
    }

    if (pattern.equals(Links)) {
      return isLinks(ele);
    }

    if (pattern.equals(DensyLinks)) {
      return isDensyLinks(ele);
    }

    if (pattern.equals(Images)) {
      return isImages(ele);
    }

    if (pattern.equals(LinkImages)) {
      return isLinkImages(ele);
    }

    return false;
  }

  public static boolean isTable(Element ele) {
    if (!StringUtil.in(ele.tagName(), "table", "tbody")) {
      return false;
    }

    if (ele.tagName().equals("table")) {
      Element tbody = ele.getElementsByTag("tbody").first();
      if (tbody != null)
        ele = tbody;
    }

    double _child = ele.indic(Indicator.C);
    double _grant_child = ele.indic(Indicator.G);

    // tooo few children, or tooo many grand children
    if (_child < 3 || _grant_child / _child > 5) {
      return false;
    }

    return true;
  }

  public static boolean isDl(Element ele) {
    if (!StringUtil.in(ele.tagName(), "dl")) {
      return false;
    }

    double _child = ele.indic(Indicator.C);
    double _grant_child = ele.indic(Indicator.G);

    // tooo few children, or tooo many grand children
    if (_child < 3 || _grant_child / _child > 5) {
      return false;
    }

    return true;
  }

  public static boolean isII(Element ele) {
    double _img = ele.indic(Indicator.IMG);
    double _sep = ele.indic(Indicator.SEP);
    double _char = ele.indic(Indicator.CH);
    double _txt_blk = ele.indic(Indicator.TB);

    if (_sep >= 3 && _img <= 2 && _char / _txt_blk <= 10) {
      int count = StringUtils.countMatches(ele.text(), ">");

      if (_sep - count >= 3) {
        return true;
      }
    }

    return false;
  }

  public static boolean isN2(Element ele) {
    // calculate the likelihood of the children, if the children are very
    // likely, it's a list-like code block
    double _child = ele.indic(Indicator.C);
    double _grant_child = ele.indic(Indicator.G);
    double _txt_blk = ele.indic(Indicator.TB);

    // tooo few children
    if (_child < 3) {
      return false;
    }

    if (_grant_child / _child < 2 || _grant_child / _child >= 3) {
      return false;
    }

    if (_txt_blk / _child < 2 || _txt_blk / _child >= 3) {
      return false;
    }

    Set<String> childTags = new HashSet<String>();
    Set<Double> numGrandson = new HashSet<Double>();

    for (Element child : ele.children()) {
      childTags.add(child.tagName());
      numGrandson.add(child.indic(Indicator.C));
    }

    // 如果列表项的tag标签和直接子孩子数高度相似，那么可以忽略内部结构的相似性，这是对基于方差判断方法的补充
    if (childTags.size() <= 3 && numGrandson.size() <= 3) {
      return true;
    }

    return false;
  }

  public static boolean isList(Element ele) {
    // calculate the likelihood of the children, if the children are very
    // likely, it's a list-like code block
    double _child = ele.indic(Indicator.C);
    double _grant_child = ele.indic(Indicator.G);
    
    // tooo few children, or tooo many grand children
    if (_child < 3 || _grant_child / _child > 5) {
      return false;
    }

    Set<String> childTags = new HashSet<String>();
    Set<Double> numGrandson = new HashSet<Double>();

    for (Element child : ele.children()) {
      childTags.add(child.tagName());
      numGrandson.add(child.indic(Indicator.C));
    }

    // 如果列表项的tag标签和直接子孩子数高度相似，那么可以忽略内部结构的相似性，这是对基于方差判断方法的补充
    // 0.2 表示 : 每5个里边允许有一个干扰项
    if (childTags.size() / _child <= 0.2 && numGrandson.size() / _child <= 0.2) {
      return true;
    }

    return false;
  }

  public static boolean isLinks(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);

    if (_a > 0 && _img / _a < 0.15) {
      return true;
    }

    return false;
  }

  public static boolean isDensyLinks(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _char = ele.indic(Indicator.CH);
    double _txt_blk = ele.indic(Indicator.TB);

    if (_a >= 50 && _img / _a <= 0.15 && _char / _txt_blk <= 6) {
      return true;
    }

    return false;
  }

  public static boolean isImages(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);

    if (_img > 0 && _a / _img <= 0.2) {
      return true;
    }

    return false;
  }

  public static boolean isLinkImages(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);

    if (_img > 0 && _a > 0 && _a / _img >= 0.8 && _img / _a >= 0.8) {
      return true;
    }

    return false;
  }

  public static boolean isDesyText(Element ele) {
    double _char = ele.indic(Indicator.CH);
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _txt_blk = ele.indic(Indicator.TB);

    if (_char < 300 || _txt_blk < 30 || _char / _txt_blk < 10 || _a / _txt_blk > 0.2 || _img / _txt_blk > 0.1) {
      return false;
    }

    return true;
  }

  @Override
  public String toString() {
    return text;
  }

  @Override
  public boolean equals(Object other) {
    return (other instanceof BlockPattern) && text.equals(((BlockPattern) other).text);
  }

  @Override
  public int compareTo(BlockPattern other) {
    return text.compareTo(other.text);
  }
}
