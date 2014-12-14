package org.qiwur.scent.classifier;

import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Indicator;
import org.jsoup.select.ElementTraversor;
import org.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

public class PhraseRule extends ScentRule {

  public PhraseRule(String phrase, double score) {
    super(phrase, score);
  }

  public double getScore(Element ele) {
    new ElementTraversor(new InterruptiveElementVisitor() {

      private int startDepth = Integer.MIN_VALUE;
      private int count = 0;

      @Override
      public void head(Element ele, int depth) {
        if (startDepth == Integer.MIN_VALUE) {
          startDepth = depth;
        }

        // TODO : better fuzzy textual compare support
        // and improve textual compare performance
        if (StringUtil.humanize(ele.id()).contains(name())) ++count;
        else if (StringUtil.humanize(ele.className()).contains(name())) ++count;
        else if (ele.strippedText().contains(name())) ++count;
      }

      @Override
      public void tail(Element ele, int depth) {
        if (depth == startDepth) {
          ele.attr("data-tmp-count", String.valueOf(count));
        }
      }

    }).traverse(ele);

    double count = StringUtil.tryParseDouble(ele.attr("data-tmp-count"));
    ele.removeAttr("data-tmp-count");

    return count * score();
  }

  public String phrase() {
    return name();
  }

  protected Map<String, String> variables() {
    return variables();
  }

}
