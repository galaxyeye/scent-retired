package org.qiwur.scent.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.BlockPatternTracker;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class BlockPatternPrinter {

	private static final Logger logger = LogManager.getFormatterLogger(BlockPatternPrinter.class);

	Document doc = null;

	public BlockPatternPrinter(Document doc) {
		this.doc = doc;
	}

	public void process() {
		for (DomSegment block : doc.domSegments()) {
			BlockPatternTracker tracker = block.patternTracker();

			if (!tracker.empty()) {
				logger.debug("title:%15s|tracker:%15s|body:%s",
						block.title(), tracker, block.text());
			}
		}
	}
}
