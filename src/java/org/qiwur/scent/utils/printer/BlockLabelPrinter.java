package org.qiwur.scent.utils.printer;

import java.util.Arrays;
import java.util.Formatter;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.block.BlockLabel;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Document;

public class BlockLabelPrinter {

	private static final Logger logger = LogManager.getLogger(BlockLabelPrinter.class);

  private final Configuration conf;
  private final Document doc;

  protected final String[] labels;

  protected StringBuilder reporter = new StringBuilder();

	public BlockLabelPrinter(Document doc, Configuration conf) {
	  Validate.notNull(doc);

		this.conf = conf;
		this.doc = doc;
    this.labels = conf.getStrings("scent.classifier.block.labels");
	}

	public void process() {
    @SuppressWarnings("resource")
    Formatter formatter = new Formatter(reporter);

    logger.debug("all block labels : {}", Arrays.asList(labels));
    int counter = 0;
    for (String label : this.labels) {
      for (DomSegment segment : doc.domSegments().getAll(BlockLabel.fromString(label))) {
        String parentName = "";
        if (segment.parent() != null) {
          parentName = segment.parent().block().prettyName();
        }

        ++counter;
        formatter.format("%-20s %-20s %-20s body:%-70s\n",
            StringUtils.substring(label + ":" + segment.labelTracker().get(BlockLabel.fromString(label)), 0, 20),
            StringUtils.substring(segment.block().prettyName(), 0, 20),
            StringUtils.substring(parentName, 0, 20),
            StringUtils.substring(segment.text(), 0, 70));
      }
    }

    logger.debug("\n{}\n total {} tagged segments\n", reporter, counter);
	} // process
}
