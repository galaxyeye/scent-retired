package org.qiwur.scent.jsoup.parser;

import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.nodes.IndicatorIndex;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;

public class IndicatorIndexer extends InterruptiveElementVisitor {

  private final IndicatorIndex index;

  public IndicatorIndexer(IndicatorIndex index) {
    this.index = index;
    index.clear();
  }

  @Override
  public void head(Element ele, int depth) {
    for (String name : Indicator.names) {
      if (ele.hasIndic(name)) {
        index.put(name, ele.indic(name), ele);
      }
    }
  }
}
