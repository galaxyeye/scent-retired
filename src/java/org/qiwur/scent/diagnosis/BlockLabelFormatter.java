package org.qiwur.scent.diagnosis;

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;
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
	  buildHeader("Lable", "Name", "Parent Name", "Text");

    int counter = 0;
    for (String label : this.labels) {
      BlockLabel bl = BlockLabel.fromString(label);

      for (DomSegment segment : doc.domSegments().getAll(bl)) {
        double sim = segment.labelTracker().get(bl);
        String parentName = segment.hasParent() ? segment.parent().name() : "";

        buildRow(
            String.format("%s,%1.2f", label, sim), 
            StringUtils.substring(segment.name(), 0, 20),
            StringUtils.substring(parentName, 0, 20),
            StringUtils.substring(segment.text(), 0, 50));

        ++counter;
      }
    }

    String desc = String.format("all block labels : %s\n total %d tagged segments",
        Arrays.asList(labels).toString(), counter);
    setDescription(desc);
	} // process
}
