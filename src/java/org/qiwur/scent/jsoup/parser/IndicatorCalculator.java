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
          node.getIndicator(Indicator.OCH),
          node.getIndicator(Indicator.CH),
          node.getIndicator(Indicator.OTB),
          node.getIndicator(Indicator.TB),
          node.getIndicator(Indicator.SEP)
      );

      return;
    }

    if (node instanceof Element) {
      Element e = (Element) node;

      // since the the sum has been calculated, we can get the average
      e.indic(Indicator.IAW, divide(e, Indicator.ITW, Indicator.IMG, 0.0));
      e.indic(Indicator.IAH, divide(e, Indicator.ITH, Indicator.IMG, 0.0));
      e.indic(Indicator.AAW, divide(e, Indicator.ATW, Indicator.A, 0.0));
      e.indic(Indicator.AAH, divide(e, Indicator.ATH, Indicator.A, 0.0));

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
      double _ch = 0.0;
      double _sep = 0.0;

      String text = StringUtil.stripNonChar(((TextNode) node).text(), StringUtil.DefaultKeepChars).trim();
      _ch = text.length();

      for (String sep : Indicator.separators) {
        _sep += StringUtils.countMatches(text, sep);
      }

      if (_ch > 0) {
        node.accumIndics(
            new Indicator(Indicator.CH, _ch),
            new Indicator(Indicator.OCH, _ch),
            new Indicator(Indicator.OTB, 1.0),
            new Indicator(Indicator.TB, 1.0),
            new Indicator(Indicator.SEP, _sep)
        );
      }
    }

    if (node instanceof Element) {
      double _a = 0.0;
      double _a_total_w = 0.0;
      double _a_total_h = 0.0;
      double _img = 0.0;
      double _img_total_w = 0.0;
      double _img_total_h = 0.0;

      if (node.nodeName().equals("a")) {
        ++_a;
        _a_total_w = node.sniffWidth();
        _a_total_h = node.sniffHeith();
      }

      if (node.nodeName().equals("img")) {
        ++_img;
        _img_total_w = node.sniffWidth();
        _img_total_h = node.sniffHeith();
      }

      node.accumIndics(
          new Indicator(Indicator.A, _a),
          new Indicator(Indicator.AMW, _a_total_w),
          new Indicator(Indicator.AAW, _a_total_w),
          new Indicator(Indicator.AMH, _a_total_h),
          new Indicator(Indicator.AAH, _a_total_h),
          new Indicator(Indicator.ATW, _a_total_w),
          new Indicator(Indicator.ATH, _a_total_h),

          new Indicator(Indicator.IMG, _img),
          new Indicator(Indicator.IMW, _img_total_w),
          new Indicator(Indicator.IAW, _img_total_w),
          new Indicator(Indicator.IMH, _img_total_h),
          new Indicator(Indicator.IAH, _img_total_h),
          new Indicator(Indicator.ITW, _img_total_w),
          new Indicator(Indicator.ITH, _img_total_h)
      );
    }
  }

  private double divide(Element ele, String numerator, String denominator, double divideByZeroValue) {
    double n = ele.indic(numerator), d = ele.indic(denominator);

    return d == 0 ? divideByZeroValue : n / d;
  }
}
