package org.qiwur.scent.diagnosis;

<<<<<<< HEAD
import java.text.DecimalFormat;

=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;
<<<<<<< HEAD
import org.qiwur.scent.jsoup.nodes.Indicator;
=======
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

public class DomSegmentFormatter extends DiagnosisFormatter {

	private final Document doc;

	public DomSegmentFormatter(Document doc, Configuration conf) {
	  super(conf);

		this.doc = doc;
<<<<<<< HEAD
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
=======
		buildHeader("name", "text");
	}

	public void process() {
		for (DomSegment seg : doc.domSegments()) {
		  buildRow(seg.root().prettyName(), StringUtils.substring(seg.text(), 0, 1500));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
		}
	}
}
