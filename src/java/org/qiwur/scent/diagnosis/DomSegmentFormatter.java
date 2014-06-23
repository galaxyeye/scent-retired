package org.qiwur.scent.diagnosis;

import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class DomSegmentFormatter extends DiagnosisFormatter {

	private final Document doc;

	public DomSegmentFormatter(Document doc, Configuration conf) {
	  super(conf);

		this.doc = doc;
		buildHeader("name", "text");
	}

	public void process() {
		for (DomSegment seg : doc.domSegments()) {
		  buildRow(seg.root().prettyName(), StringUtils.substring(seg.text(), 0, 1500));
		}
	}
}
