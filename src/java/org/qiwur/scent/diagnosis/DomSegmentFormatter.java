package org.qiwur.scent.diagnosis;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Indicator;

public class DomSegmentFormatter extends DiagnosisFormatter {

  private final Document doc;

  public DomSegmentFormatter(Document doc, Configuration conf) {
    super(conf);

    this.doc = doc;
  }

  public void process() {
    for (DomSegment segment : doc.domSegments()) {
      buildRow(segment.name(), buildNestedTable(segment));
    }
  }

  private String buildNestedTable(DomSegment segment) {
    String indicators = "";
    DecimalFormat df = new DecimalFormat("#.0");
    for (Indicator indicator : segment.block().indicators()) {
      if (indicators != "") indicators += ", ";
      indicators += String.format("%s:%s", indicator.getKey(), df.format(indicator.getValue()));
    }

    String row1 = "<tr><th>Parent Segment</th><th>Patterns</th><th>Labels</th><th class='last'>Blocking Reason</th></tr>";

    String row2 = String.format(
        "<tr><td>%s</td><td>%s</td><td>%s</td><td class='last'>%s</td></tr>",
        segment.hasParent() ? segment.parent().name() : "",
        segment.patternTracker().toString(),
        segment.labelTracker().toString(),
        segment.block().attr("data-blocking-reason")
    );

    String row3 = String.format("<tr><td colspan='4' class='last'>%s</td></tr>", indicators);

    String row4 = String.format("<tr><td colspan='4' class='last'>%s</td></tr>", segment.block().attr("data-break-rule"));

    String row5 = String.format("<tr><td colspan='4' class='last'>%s</td></tr>", StringUtils.substring(segment.block().text(), 0, 800));

    return "<table class='nested'>" + row1 + row2 + row3 + row4 + row5 + "</table>";
  }
}
