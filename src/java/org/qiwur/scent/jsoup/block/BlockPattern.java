package org.qiwur.scent.jsoup.block;

import java.util.HashSet;
import java.util.Set;

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
  public static BlockPattern DenseLinks = BlockPattern.fromString("DenseLinks");
  public static BlockPattern Images = BlockPattern.fromString("Images");
  public static BlockPattern LinkImages = BlockPattern.fromString("LinkImages");
  public static BlockPattern DenseText = BlockPattern.fromString("DenseText");

  public static Set<BlockPattern> patterns = Sets.newHashSet();

  static {
    patterns.add(N2);
    patterns.add(II);
    patterns.add(Table);
    patterns.add(Dl);
    patterns.add(List);

    patterns.add(Links);
    patterns.add(DenseLinks);
    patterns.add(Images);
    patterns.add(LinkImages);
    patterns.add(DenseText);
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

    if (pattern.equals(DenseLinks)) {
      return isDenseLinks(ele);
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
    double _char_ave = ele.indic(Indicator.CHA);
    double _char_max = ele.indic(Indicator.CHM);
    double _txt_blk = ele.indic(Indicator.TB);

    // filter time string
    if (_sep >= 3) {
      // TODO : efficiency?
      int countTime = StringUtil.countTimeString(ele.text());
      _sep -= countTime * 2;
    }

    if (_sep < 3 || _img > 2 || _char_ave > 10 || _char_max > 50 || _txt_blk < 6) {
      return false;
    }

    return listLikely(ele);
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
    double _child = ele.indic(Indicator.C);
    double _grant_child = ele.indic(Indicator.G);

    // since most list like blocks are found out by variance rule,
    // we consider blocks complex enough here
    if (_child < 8 || _grant_child / _child > 5) {
      return false;
    }

    return !isLinks(ele) && !isLinkImages(ele) && listLikely(ele);
  }

  /**
   * Notice : List likely block is not only "List" pattern!
   * 
   * */
  public static boolean isLinks(Element ele) {
    if (isDenseLinks(ele)) {
      return false;
    }

    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);

    if (_a >= 3 && _img / _a <= 0.2) {
      return true;
    }

    return false;
  }

  public static boolean isDenseLinks(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _img_max_h = ele.indic(Indicator.IMH);
    double _img_max_w = ele.indic(Indicator.IMW);
    double _char_ave = ele.indic(Indicator.CHA);
    double _char_max = ele.indic(Indicator.CHM);
    double _txt_blk = ele.indic(Indicator.TB);

    if (_a >= 30 && _txt_blk >= 30 && _char_ave >= 2 && _char_ave <= 6 && _char_max <= 15 && 
        _img < 0.3 * _a && _img_max_h < 150 && _img_max_w < 150) {

      return true;
    }

    if (_a >= 100 && _txt_blk >= 100 && _char_ave >= 2 && _char_ave <= 6 && _char_max <= 15 && _img < 0.3 * _a) {
      return true;
    }

    return false;
  }

  public static boolean isImages(Element ele) {
    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _img_max_w = ele.indic(Indicator.IMW);
    double _img_max_h = ele.indic(Indicator.IMH);
    double _img_ave_w = ele.indic(Indicator.IAW);
    double _img_ave_h = ele.indic(Indicator.IAH);
    double _txt_blk = ele.indic(Indicator.TB);

    // we ignore images smaller than 40 x 40
    if ((_img_max_w > 0 && _img_max_w <= 40) || (_img_max_h > 0 && _img_max_h <= 40)) {
      return false;
    }

    // if there are too few images, but if they are big enough, they are welcome
    if (_img >= 1 && _img <= 3 && _a <= 1 && _img_ave_w >= 300 && _img_ave_h >= 300) {
      return true;
    }

    // or, if there are many images but few links, we mark it as "Images" block
    if (_img > 3 && _a / _img <= 0.2 && _txt_blk / _img <= 1.5) {
      return true;
    }

    return false;
  }

  /**
   * link-image example : 
   * 
   * <ul>
   *  <li>
   *    <a href="..."><img src="..." /><a>
   *    <div><a href="..."><span>something</span> <b>awesome</b></a></div>
   *    <div><a href="..."><span>something</span> <b>awesome</b></a></div>
   *    <div><span>damn</span> the <b>awesome</b> things</div>
   *  </li>
   *  <li>....</li>
   *  ...
   * </ul>
   * 
   * */
  public static boolean isLinkImages(Element ele) {
    if (!listLikely(ele)) {
      return false;
    }

    double _a = ele.indic(Indicator.A);
    double _img = ele.indic(Indicator.IMG);
    double _char = ele.indic(Indicator.CH);
    double _char_max = ele.indic(Indicator.CHM);
    double _txt_blk = ele.indic(Indicator.TB);

    // too few images or links
    if (_img < 3 || _a < 3) {
      return false;
    }

    // long text not permitted, tooo many text block not permitted
    if (_char / _a > 80 || _char / _img > 80 || _char_max > 80 || _txt_blk / Math.max(_a, _img) > 8) {
      return false;
    }

    double rate = _a / _img;
    if ((rate >= 0.8 && rate <= 1.2) || (rate >= 1.8 && rate <= 2.2) || (rate >= 2.8 && rate <= 3.2)) {
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

  /**
   * Notice : List likely block is not only "List" pattern!
   * 
   * */
  public static boolean listLikely(Element ele) {
    if (StringUtil.in(ele.tagName(), "ul", "ol")) {
      return true;
    }

    // calculate the likelihood of the children, if the children are very
    // likely, it's a list-like code block
    double _child = ele.indic(Indicator.C);

    // tooo few children, or tooo many grand children
    if (_child < 3) {
      return false;
    }

    Set<String> childTags = new HashSet<String>();
    Set<Double> numGrandson = new HashSet<Double>();

    for (Element child : ele.children()) {
      childTags.add(child.tagName());
      numGrandson.add(child.indic(Indicator.C));
    }

    // 如果列表项的tag标签和直接子孩子数高度相似，那么可以忽略内部结构的相似性，这是对基于方差判断方法的补充
    // 0.2 表示 : 每5个里边允许有一个干扰项，即20%干扰项
    if (childTags.size() / _child <= 0.2 && numGrandson.size() / _child <= 0.2) {
      return true;
    }

    return false;
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
  public int hashCode() {
    return text.hashCode();
  }

  @Override
  public int compareTo(BlockPattern other) {
    return text.compareTo(other.text);
  }
}
