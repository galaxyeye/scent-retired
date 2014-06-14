package org.qiwur.scent.jsoup.parser;

import org.apache.commons.lang.StringUtils;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.Node;
import org.qiwur.scent.jsoup.nodes.TextNode;
import org.qiwur.scent.jsoup.select.InterruptiveNodeVisitor;
import org.qiwur.scent.utils.StringUtil;

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

      // since the the sum has been calculated, we can get the average
      e.indic(Indicator.IAW, division(e, Indicator.ITW, Indicator.IMG));
      e.indic(Indicator.IAH, division(e, Indicator.ITH, Indicator.IMG));
      e.indic(Indicator.AAW, division(e, Indicator.ATW, Indicator.A));
      e.indic(Indicator.AAH, division(e, Indicator.ATH, Indicator.A));

      // 计算父节点的统计信息
      Element pe = e.parent();
      if (pe == null) {
        return;
      }

      pe.accumIndics(
          // code structure feature
          e.getIndicator(Indicator.CH),
          e.getIndicator(Indicator.TB),
          e.getIndicator(Indicator.SEP),
          e.getIndicator(Indicator.A),
          e.getIndicator(Indicator.IMG),
          new Indicator(Indicator.C, 1.0),
          new Indicator(Indicator.D, 1.0 + e.indic(Indicator.D)),

          // vision feature
          // calculate the sum and calculate the average later, when we leave that node
          e.getIndicator(Indicator.ATW),
          e.getIndicator(Indicator.ATH),
          e.getIndicator(Indicator.ITW),
          e.getIndicator(Indicator.ITH)
      );

      // calculate required max value for parent node
      final String[] maxIndicators = {Indicator.AMH, Indicator.AMW, Indicator.IMH, Indicator.IMW};
      for (String indicator : maxIndicators) {
        double value = e.indic(indicator);
        if (value > pe.indic(indicator)) {
          pe.indic(indicator, value);
        }
      }

      // grant children number
      Element ppe = pe.parent();
      if (ppe != null) {
        ppe.accumIndics(new Indicator(Indicator.G, 1.0));
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
      double linkWidth = 0.0;
      double linkHeight = 0.0;
      double numImage = 0.0;
      double imageWidth = 0.0;
      double imageHeight = 0.0;

      if (node.nodeName().equals("a")) {
        ++numLink;
        linkWidth = StringUtil.parseDouble(node.attr("data-offset-width"));
        linkHeight = StringUtil.parseDouble(node.attr("data-offset-height"));
      }

      if (node.nodeName().equals("img")) {
        ++numImage;
        imageWidth = StringUtil.parseDouble(node.attr("data-offset-width"));
        imageHeight = StringUtil.parseDouble(node.attr("data-offset-height"));
      }

      node.accumIndics(
          new Indicator(Indicator.A, numLink),
          new Indicator(Indicator.AMW, linkWidth),
          new Indicator(Indicator.AAW, linkWidth),
          new Indicator(Indicator.AMH, linkHeight),
          new Indicator(Indicator.AAH, linkHeight),
          new Indicator(Indicator.ATW, linkWidth),
          new Indicator(Indicator.ATH, linkHeight),

          new Indicator(Indicator.IMG, numImage),
          new Indicator(Indicator.IMW, imageWidth),
          new Indicator(Indicator.IAW, imageWidth),
          new Indicator(Indicator.IMH, imageHeight),
          new Indicator(Indicator.IAH, imageHeight),
          new Indicator(Indicator.ITW, imageWidth),
          new Indicator(Indicator.ITH, imageHeight)
      );
    }
  }

  private double division(Element ele, String numerator, String denominator) {
    double res = 0.0;

    double d = ele.indic(denominator);

    if (d != 0) {
      res = ele.indic(numerator) / ele.indic(denominator);
    }

    return res;
  }
}
