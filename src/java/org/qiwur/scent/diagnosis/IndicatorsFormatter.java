package org.qiwur.scent.diagnosis;

import java.util.ArrayList;
import java.util.Arrays;
<<<<<<< HEAD
import java.util.List;
=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

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

<<<<<<< HEAD
  boolean segmentedOnly = false;
=======
  boolean segmentedOnly = true;
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

  public IndicatorsFormatter(Document doc, Configuration conf) {
    super(conf);

    this.doc = doc;

<<<<<<< HEAD
    List<String> headers = Lists.newArrayList();
    headers.add("Element");
    headers.addAll(Arrays.asList(Indicator.names));
    buildHeader(headers);
=======
    buildHeader("name", Arrays.asList(Indicator.names));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
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
<<<<<<< HEAD
      row.add(e.prettyName());
      for (int i = 0; i < Indicator.names.length; ++i) {
        row.add(String.format("%4.2f", e.indic(Indicator.names[i])));
      }

      buildRow(row);
=======
      for (int i = 0; i < Indicator.names.length; ++i) {
        row.add(String.format("%4.2f", e.indic(Indicator.names[i])));
      }
      buildRow(e.prettyName(), row);
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
    }
  }
}
