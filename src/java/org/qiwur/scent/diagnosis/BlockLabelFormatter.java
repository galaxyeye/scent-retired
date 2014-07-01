package org.qiwur.scent.diagnosis;

import java.util.Arrays;

<<<<<<< HEAD
=======
import org.apache.commons.lang.StringUtils;
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class BlockLabelFormatter extends DiagnosisFormatter {

  private final Document doc;

  protected final String[] labels;

	public BlockLabelFormatter(Document doc, Configuration conf) {
	  super(conf);

	  Validate.notNull(doc);

		this.doc = doc;
    this.labels = conf.getStrings("scent.classifier.block.labels");
	}

	@Override
	public void process() {
<<<<<<< HEAD
	  buildHeader("Lable", "All Labels", "Segment", "Parent Segment", "Text");
=======
	  buildHeader("Lable", "Name", "Parent Name", "Text");
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

    int counter = 0;
    for (String label : this.labels) {
      BlockLabel bl = BlockLabel.fromString(label);

      for (DomSegment segment : doc.domSegments().getAll(bl)) {
<<<<<<< HEAD
        buildRow(
            label,
            segment.labelTracker().toString(),
            segment.name(),
            segment.hasParent() ? segment.parent().name() : "",
            segment.text());
=======
        double sim = segment.labelTracker().get(bl);
        String parentName = segment.hasParent() ? segment.parent().name() : "";

        buildRow(
            String.format("%s,%1.2f", label, sim), 
            StringUtils.substring(segment.name(), 0, 20),
            StringUtils.substring(parentName, 0, 20),
            StringUtils.substring(segment.text(), 0, 50));
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc

        ++counter;
      }
    }

    String desc = String.format("all block labels : %s\n total %d tagged segments",
        Arrays.asList(labels).toString(), counter);
    setDescription(desc);
	} // process
}
