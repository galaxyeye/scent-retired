package org.qiwur.scent.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.BlockLabelTracker;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class BlockTypePrinter {

	private static final Logger logger = LogManager.getFormatterLogger(BlockTypePrinter.class);

	Document doc = null;

	public BlockTypePrinter(Document doc) {
		this.doc = doc;
	}

	public void process() {
		for (DomSegment block : doc.domSegments()) {
			BlockLabelTracker labelTracker = block.labelTracker();

			if (!labelTracker.empty()) {
				logger.debug("title:%15s|tracker:%15s|body:%s", 
						block.title(),
						labelTracker,
						block.text());
			}
		}
	}
}
