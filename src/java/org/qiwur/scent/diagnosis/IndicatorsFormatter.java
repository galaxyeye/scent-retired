package org.qiwur.scent.diagnosis;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Element;
import org.qiwur.scent.jsoup.nodes.Indicator;
import org.qiwur.scent.jsoup.select.ElementTraversor;
import org.qiwur.scent.jsoup.select.InterruptiveElementVisitor;
import org.qiwur.scent.utils.StringUtil;

import com.google.common.collect.Lists;

public class IndicatorsFormatter extends DiagnosisFormatter {

  private final Document doc;

  boolean segmentedOnly = false;

  public IndicatorsFormatter(Document doc, Configuration conf) {
    super(conf);

    this.doc = doc;

    List<String> headers = Lists.newArrayList();
    headers.add("Element");
    headers.addAll(Arrays.asList(Indicator.names));
    buildHeader(headers);
  }

  @Override
  public void process() {
    new ElementTraversor(new DomStatisticsPrintVisitor()).traverse(doc);
  }

  private class DomStatisticsPrintVisitor extends InterruptiveElementVisitor {

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

      ArrayList<String> row = Lists.newArrayList();
      row.add(e.prettyName());
      for (int i = 0; i < Indicator.names.length; ++i) {
        row.add(String.format("%4.2f", e.indic(Indicator.names[i])));
      }

      buildRow(row);
    }
  }
}
