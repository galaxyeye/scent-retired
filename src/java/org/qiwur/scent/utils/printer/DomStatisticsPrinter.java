package org.qiwur.scent.utils.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.ElementTraversor;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

public class DomStatisticsPrinter {

  private Logger logger = LogManager.getLogger(DomStatisticsPrinter.class);

  Document doc = null;

  String report = "";

  boolean segmentedOnly = true;

  public DomStatisticsPrinter(Document doc) {
    this.doc = doc;
  }

  public void process() {
    new ElementTraversor(new DomStatisticsPrintVisitor()).traverse(doc);

    logger.debug(report);
  }

  private class DomStatisticsPrintVisitor extends InterruptiveElementVisitor {

    public DomStatisticsPrintVisitor() {
      report += String.format("%-40s", "name");

      for (String name : Indicator.names) {
        report += String.format("%-13s", name);
      }

      report += "\n";
    }

    public void head(Element e, int depth) {
      if (segmentedOnly && !e.hasAttr("data-segmented")) {
        return;
      }

      // 空标签，一般仅用于布局
      if (StringUtil.in(e.tagName(), "p", "div", "ol", "ul", "li", "table") && e.indic(Indicator.D) == 0) {
        return;
      }

      if (!StringUtil.in(e.tagName(), "body", "p", "div", "ol", "ul", "li", "table", "tbody", "tr", "dl", "h1", "h2",
          "h3", "h4", "h5", "h6")) {
        return;
      }

      report += String.format("%-40s", e.prettyName());
      for (int i = 0; i < Indicator.names.length; ++i) {
        report += String.format("%-13.2f", e.indic(Indicator.names[i]));
      }

      report += "\n";
    }
  }
}
