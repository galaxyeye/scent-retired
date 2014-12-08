package org.qiwur.scent.jsoup.parser;

import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;

public class CodeDigestCalculator extends InterruptiveElementVisitor {

  private StringBuilder accum;

  public CodeDigestCalculator(StringBuilder accum) {
    this.accum = accum;
  }

  @Override
  public void head(Element ele, int depth) {
    accum.append(ele.tagName());
  }
}
