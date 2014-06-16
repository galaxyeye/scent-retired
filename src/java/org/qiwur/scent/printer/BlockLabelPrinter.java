package org.qiwur.scent.printer;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.Validate;
import org.apache.hadoop.conf.Configuration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.BlockLabel;
import org.qiwur.scent.jsoup.block.BlockLabelTracker;
import org.qiwur.scent.jsoup.block.BlockPattern;
import org.qiwur.scent.jsoup.block.BlockPatternTracker;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.block.DomSegments;
import org.qiwur.scent.jsoup.nodes.Document;
import org.qiwur.scent.jsoup.nodes.Indicator;

import ruc.irm.similarity.FuzzyProbability;

public class BlockLabelPrinter {

	private static final Logger logger = LogManager.getLogger(BlockLabelPrinter.class);

	private Map<BlockLabel, DomSegments> sortedSegments = new HashMap<BlockLabel, DomSegments>();

	private final DomSegments segments;

  private final Configuration conf;

  protected final String[] labels;

  protected StringBuilder reporter = new StringBuilder();

	public BlockLabelPrinter(Document doc, Configuration conf) {
	  Validate.notNull(doc);

		this.segments = doc.domSegments();
		this.conf = conf;
    this.labels = conf.getStrings("scent.html.block.labels");
	}

	public void process() {
		for (String label : labels) {
		  BlockLabel l = BlockLabel.fromString(label);
      sortedSegments.put(l, segments.getAll(l));
		}

		printSortedSegmentSequence();
	} // process

	void printSegmentSequence() {
    @SuppressWarnings("resource")
    Formatter formatter = new Formatter(reporter);

		for (DomSegment segment : segments) {
			BlockLabelTracker labelTracker = segment.labelTracker();
			BlockPatternTracker patternTracker = segment.patternTracker();

			boolean hasValuablePattern = false;
			if (!patternTracker.empty()) {
				hasValuablePattern |= patternTracker.maybe(BlockPattern.I_I);
				hasValuablePattern |= patternTracker.maybe(BlockPattern.LI);
			}

			if (!labelTracker.empty() || hasValuablePattern) {
				String text = segment.text();
				if (text.equals("") && segment.is(BlockPattern.LI, FuzzyProbability.MUST_BE)) {
					text = "(images : )" + segment.root().indic(Indicator.IMG);
				}

				formatter.format("title:%-15s|tracker:%-15s, %-15s|body:%s",
						segment.title(),
						labelTracker,
						patternTracker,
						StringUtils.substring(segment.text(), 0, 80));

        // logger.debug("-----------------------------------------------");
			}
		}

    logger.debug("\n{}", reporter);
	}

	void printSortedSegmentSequence() {
	  @SuppressWarnings("resource")
    Formatter formatter = new Formatter(reporter);

	  int counter = 0;
		for (BlockLabel type : sortedSegments.keySet()) {
			for (DomSegment segment : sortedSegments.get(type)) {
			  ++counter;
			  formatter.format("%-20s %-5d %-20s title:%-15s body:%-70s\n",
			      StringUtils.substring(type + ":" + segment.labelTracker().get(type), 0, 20),
            segment.root().sequence(),
            StringUtils.substring(segment.root().prettyName(), 0, 20),
            StringUtils.substring(segment.titleText(), 0, 15),
            StringUtils.substring(segment.text(), 0, 70));
				// logger.debug(segment.body().indicators());

			// logger.debug("-----------------------------------------------");
			}
		}

		logger.debug("\n{}\n total {} tagged segments\n", reporter, counter);
	}
}
