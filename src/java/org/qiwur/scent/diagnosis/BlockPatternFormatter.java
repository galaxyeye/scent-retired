package org.qiwur.scent.diagnosis;

<<<<<<< HEAD
import org.apache.hadoop.conf.Configuration;
=======
import org.apache.commons.lang.StringUtils;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class BlockPatternFormatter extends DiagnosisFormatter {

  private final Document doc;

	public BlockPatternFormatter(Document doc, Configuration conf) {
    super(conf);
		this.doc = doc;
	}

	public void process() {
<<<<<<< HEAD
    buildHeader("Pattern", "All Patterns", "Segment", "Parent Segment", "Text");
=======
    buildHeader("Pattern", "Name", "Parent Name", "Text");
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

    int counter = 0;
    for (BlockPattern pattern : BlockPattern.patterns) {
      for (DomSegment segment : doc.domSegments().getAll(pattern)) {
        String parentName = segment.hasParent() ? segment.parent().name() : "";

        buildRow(
            pattern.text(),
<<<<<<< HEAD
            segment.patternTracker().toString(),
            segment.name(),
            segment.hasParent() ? segment.parent().name() : "",
            segment.text());
=======
            StringUtils.substring(segment.name(), 0, 20),
            StringUtils.substring(parentName, 0, 20),
            StringUtils.substring(segment.text(), 0, 50));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

        ++counter;
      }
    }

    String desc = String.format("all block patterns : %s\n total %d tagged segments",
        BlockPattern.patterns.toString(), counter);
    setDescription(desc);
	}
}
