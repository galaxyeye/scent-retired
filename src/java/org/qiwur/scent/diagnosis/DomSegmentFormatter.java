package org.qiwur.scent.diagnosis;

import java.text.DecimalFormat;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Indicator;

public class DomSegmentFormatter extends DiagnosisFormatter {

	private final Document doc;

	public DomSegmentFormatter(Document doc, Configuration conf) {
	  super(conf);

		this.doc = doc;
		buildHeader("Segment", "Parent", "Patterns", "Labels", "Blocking", "Detail");
	}

	public void process() {
		for (DomSegment segment : doc.domSegments()) {
		  String indicators = "";
		  DecimalFormat df = new DecimalFormat("#.0");
		  for (Indicator indicator : segment.block().indicators()) {
		    if (indicators != "") indicators += ",";
		    indicators += String.format("%s:%s", indicator.getKey(), df.format(indicator.getValue()));
		  }

		  String detail = String.format(
		      "<div>Breaked Rules<p>%s</p></div>" + 
		      "<div>Indicators<p>%s</p></div>" + 
		      "<div>Text<p>%s</p></div>",
		      segment.block().attr("data-break-rule"),
		      indicators,
		      StringUtils.substring(segment.block().richText(), 0, 800)
		  );

		  buildRow(segment.name(), 
          segment.hasParent() ? segment.parent().name() : "",
          segment.patternTracker().toString(),
          segment.labelTracker().toString(),
          segment.block().attr("data-blocking-reason"),
          detail);
		}
	}
}
