package org.qiwur.scent.jsoup.select;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.collections.ComparatorUtils;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;

import com.google.common.collect.TreeMultimap;

public class ElementTreeUtil {

  // 判断两个元素是否相邻
  // 相邻的定义是具有某代以内的相同祖先
  // 返回代数
  static public Element getRecentAncestor(Element e, Element e2, int generation) {
    if (e == null || e2 == null)
      return null;

    // 如果两个元素的深度相差generation以上，不可能有generation代以内的共同祖先
    if (Math.abs(e.depth() - e2.depth()) >= generation) {
      return null;
    }

    Element ancestor = e.parent();
    int d = generation;
    while (ancestor != null && d-- > 0) {
      ancestor = ancestor.parent();

      if (isAncestor(e2, ancestor, generation)) {
        return ancestor;
      }
    }

    return null;
  }

  // 是否generation代以内的先祖
  static public boolean isAncestor(Element child, Element ancestor, int generation) {
    if (ancestor == null || child == null)
      return false;

    while (child != null && generation-- > 0) {
      child = child.parent();

      if (ancestor == child) {
        return true;
      }
    }

    return false;
  }

  static public Element getAncestor(Element e, int generation) {
    while (e != null && generation-- > 0) {
      e = e.parent();
    }

    return e;
  }

  // 寻找root下绝对深度为depth，且标签名在desiredTags中的所有节点
  public static List<Element> findNDepthElements(Element root, int depth, String... desiredTags) {
    NDepthElementFounder header = new NDepthElementFounder(depth, desiredTags);

    new ElementTraversor(header).traverse(root);

    return header.getElements();
  }

  // 寻找root下第一个兄弟数不少于numChildren，且标签名在desiredTags中的节点
  public static Element findFirstElementWithNChildren(Element root, int numChildren, String... desiredTags) {
    Element e = findFirstElementWithNSibling(root, numChildren, desiredTags);

    if (e == null)
      return null;

    return e.parent();
  }

  // 寻找第一个有文本的子节点
  public static Element findFirstChildHasText(Element root, String pattern, boolean ownText) {
    return findNthChildHasText(root, 0, pattern, ownText);
  }

  // 寻找第二个有文本的子节点
  public static Element findSecondChildHasText(Element root, String pattern, boolean ownText) {
    return findNthChildHasText(root, 1, pattern, ownText);
  }

  // public static Element getFirstElementHasText(Element root, int n, String
  // text) {
  // return null;
  // }

  /*
   * 在子树中寻找第N个有文本的直接子节点 n从0开始
   * 
   * @param root 子数的根
   * 
   * @param text 不为空且不为null，寻找包含text的子节点
   * 
   * @param ownText 直接文本还是子树的所有文本
   */
  public static Element findNthChildHasText(Element root, int n, String pattern, boolean ownText) {
    if (root == null)
      return null;

    for (Element e : root.children()) {
      if (pattern == null) {
        if (ownText) {
          if (e.ownText().length() > 0) {
            if (n == 0)
              return e;
            --n;
          }
        } else if (e.hasText()) {
          if (n == 0)
            return e;
          --n;
        }
      } else {
        String s = ownText ? e.ownText() : e.text();
        if (s != null && s.length() > 0 && s.matches(pattern)) {
          if (n == 0)
            return e;
          --n;
        }
      }
    }

    return null;
  }

  public static Element findFirstElementWithNSibling(Element root, int numSibling, String... desiredTags) {
    FirstElementWithNSiblingFounder header = new FirstElementWithNSiblingFounder(numSibling, desiredTags);

    new ElementTraversor(header).traverse(root);

    return header.getFirstSibling();
  }

  // 找到直接孩子中包含最多某种标签的结点，不考虑空标签（空标签通常用于布局）
  public static TreeMultimap<Double, Element> findMostChildrenElement(Element root, int minChildren,
      String... desiredTags) {
    MostChildrenElementFounder founder = new MostChildrenElementFounder(minChildren, desiredTags);

    new ElementTraversor(founder).traverse(root);

    return founder.getElementMap();
  }

  /*
   * Find the container complex enough
   * 
   * @param addTB required additional number of text blocks
   * 
   * @param addC1 required additional number of direct child elements
   * 
   * @param addCAll required additional number of all child elements
   */
  public static Element getContainerSatisfyAny(Element content, int addTB, int addC1, int addCAll) {
    if (content == null || content.parent() == null)
      return null;

    Element parent = content.parent();

    while (parent != null) {
      double numTextBlocks = parent.indic(Indicator.TB) - content.indic(Indicator.TB);
      double childNumber = parent.indic(Indicator.C) - content.indic(Indicator.C);
      double descendantNumber = parent.indic(Indicator.D) - content.indic(Indicator.D);

      if (numTextBlocks >= addTB || childNumber >= addC1 || descendantNumber >= addCAll) {
        break;
      }

      parent = parent.parent();
    }

    return parent;
  }

  /*
   * Find the container complex enough
   * 
   * @param addTB required additional number of text blocks
   * 
   * @param addC1 required additional number of direct child elements
   * 
   * @param addCAll required additional number of all child elements
   */
  public static Element getContainerSatisfyAll(Element content, int addTB, int addC1, int addCAll) {
    if (content == null || content.parent() == null)
      return null;

    Element parent = content.parent();

    while (parent != null) {
      double numTextBlocks = parent.indic(Indicator.TB) - content.indic(Indicator.TB);
      double childNumber = parent.indic(Indicator.C) - content.indic(Indicator.C);
      double descendantNumber = parent.indic(Indicator.D) - content.indic(Indicator.D);

      if (numTextBlocks >= addTB && childNumber >= addC1 && descendantNumber >= addCAll) {
        break;
      }

      parent = parent.parent();
    }

    return parent;
  }

  private static class NDepthElementFounder extends InterruptiveElementVisitor {

    int depth = 0;
    List<Element> elements = new ArrayList<Element>();
    List<String> desiredTags = null;

    NDepthElementFounder(int depth, String... desiredTags) {
      this.depth = depth;
      this.desiredTags = Arrays.asList(desiredTags);
    }

    public List<Element> getElements() {
      return elements;
    }

    @Override
    public void head(Element node, int depth) {
      if (depth == this.depth && node instanceof Element) {
        Element e = (Element) node;
        if (desiredTags == null || desiredTags.contains(e.tagName())) {
          elements.add((Element) node);
        }
      }
    }
  }

  // 找到直接孩子中包含最多某种标签的结点，不考虑空标签
  private static class MostChildrenElementFounder extends InterruptiveElementVisitor {
    int minChildren = 0;
    List<String> desiredTags = null;

    @SuppressWarnings("unchecked")
    TreeMultimap<Double, Element> map = TreeMultimap.create(
        ComparatorUtils.reversedComparator(ComparatorUtils.NATURAL_COMPARATOR), ComparatorUtils.NATURAL_COMPARATOR);

    public MostChildrenElementFounder(int minChildren, String... desiredTags) {
      this.minChildren = minChildren;

      if (desiredTags.length > 0) {
        this.desiredTags = Arrays.asList(desiredTags);
      }
    }

    public TreeMultimap<Double, Element> getElementMap() {
      return map;
    }

    @Override
    public void head(Element e, int depth) {
      if (desiredTags != null && !desiredTags.contains(e.tagName())) {
        return;
      }

      double numChildren = e.indic(Indicator.C);
      if (numChildren >= minChildren) {
        map.put(numChildren, e);
      }
    }
  }

  private static class FirstElementWithNSiblingFounder extends InterruptiveElementVisitor {
    int numSibling = 0;
    Element firstSibling = null;
    List<String> desiredTags = null;

    FirstElementWithNSiblingFounder(int numSibling, String... desiredTags) {
      this.numSibling = numSibling;

      if (desiredTags.length > 0) {
        this.desiredTags = Arrays.asList(desiredTags);
      }
    }

    public Element getFirstSibling() {
      return firstSibling;
    }

    @Override
    public void head(Element e, int depth) {
      if (desiredTags == null) {
        if (e.siblingElements().size() >= numSibling) {
          firstSibling = e;
          stopped = true;
        }

        return;
      }

      int requiredElementCount = 0;
      for (Element sibling : e.siblingElements()) {
        if (desiredTags.contains(sibling.tagName())) {
          ++requiredElementCount;

          if (requiredElementCount >= numSibling) {
            firstSibling = e.siblingElements().first();
            stopped = true;
          }
        }
      }
    }

  }
}
