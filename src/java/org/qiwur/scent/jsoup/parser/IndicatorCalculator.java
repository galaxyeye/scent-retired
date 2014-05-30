package org.qiwur.scent.jsoup.parser;

import org.apache.commons.lang.StringUtils;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.Node;
import org.qiwur.scent.jsoup.nodes.TextNode;
import org.qiwur.scent.jsoup.select.InterruptiveNodeVisitor;

public class IndicatorCalculator extends InterruptiveNodeVisitor {

  // hit when the node is first seen
  public void head(Node node, int depth) {
    node.depth(depth);

    if (!(node instanceof Element) && !(node instanceof TextNode)) {
      return;
    }

    node.indicators().clear();
    calcSelfIndicator(node);
  }

  // hit when all of the node's children (if any) have been visited
  public void tail(Node node, int depth) {
    if (node instanceof TextNode) {
      node.parent().accumIndics(
          new Indicator(Indicator.OCH, node.indic(Indicator.OCH)),
          new Indicator(Indicator.CH, node.indic(Indicator.CH)),
          new Indicator(Indicator.OTB, 1.0),
          new Indicator(Indicator.TB, 1.0),
          new Indicator(Indicator.SEP, node.indic(Indicator.SEP))
      );

      return;
    }

    if (node instanceof Element) {
      Element e = (Element) node;

      // 计算父节点的统计信息
      Element pe = e.parent();
      if (pe == null) {
        return;
      }

      pe.accumIndics(
          e.getIndicator(Indicator.CH),
          e.getIndicator(Indicator.TB),
          e.getIndicator(Indicator.SEP),
          e.getIndicator(Indicator.A),
          e.getIndicator(Indicator.IMG),
          new Indicator(Indicator.C, 1.0),
          new Indicator(Indicator.D, 1.0)
      );

      // grant children number
      Element ppe = pe.parent();
      if (ppe != null) {
        ppe.accumIndics(
            new Indicator(Indicator.G, 1.0),
            new Indicator(Indicator.D, 1.0 + node.indic(Indicator.D))
        );
      }
    } // if
  }

  // 单个节点统计项
  private void calcSelfIndicator(Node node) {
    node.accumIndics(
        new Indicator(Indicator.SEQ, node.sequence()),
        new Indicator(Indicator.DEP, node.depth()),
        new Indicator(Indicator.SIB, node.siblingSize())
    );

    if (node instanceof TextNode) {
      double textLen = 0.0;
      double numSeparator = 0.0;

      String text = ((TextNode) node).text().replaceAll("\\s", "");
      textLen = text.length();

      for (String sep : Indicator.separators) {
        numSeparator += StringUtils.countMatches(text, sep);
      }

      if (textLen > 0) {
        node.accumIndics(
            new Indicator(Indicator.CH, textLen),
            new Indicator(Indicator.OCH, textLen),
            new Indicator(Indicator.OTB, 1.0),
            new Indicator(Indicator.TB, 1.0),
            new Indicator(Indicator.SEP, numSeparator)
        );
      }
    }

    if (node instanceof Element) {
      double numLink = 0.0;
      double numImage = 0.0;

      if (node.nodeName().equals("a")) ++numLink;
      if (node.nodeName().equals("img")) ++numImage;

      node.accumIndics(
          new Indicator(Indicator.A, numLink),
          new Indicator(Indicator.IMG, numImage)
      );
    }
  }
}
