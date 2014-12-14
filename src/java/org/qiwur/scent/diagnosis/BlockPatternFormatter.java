package org.qiwur.scent.diagnosis;

import org.apache.hadoop.conf.Configuration;
import org.jsoup.block.BlockPattern;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Document;

public class BlockPatternFormatter extends DiagnosisFormatter {

  private final Document doc;

	public BlockPatternFormatter(Document doc, Configuration conf) {
    super(conf);
		this.doc = doc;
	}

	public void process() {
    buildHeader("Pattern", "All Patterns", "Segment", "Parent Segment", "Text");

    int counter = 0;
    for (BlockPattern pattern : BlockPattern.patterns) {
      for (DomSegment segment : doc.domSegments().getAll(pattern)) {
        String parentName = segment.hasParent() ? segment.parent().name() : "";

        buildRow(
            pattern.text(),
            segment.patternTracker().toString(),
            segment.name(),
            segment.hasParent() ? segment.parent().name() : "",
            segment.text());

        ++counter;
      }
    }

    String desc = String.format("all block patterns : %s\n total %d tagged segments",
        BlockPattern.patterns.toString(), counter);

    setDescription(desc);
	}
}
