package org.qiwur.scent.utils.printer;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jsoup.block.DomSegment;
import org.jsoup.nodes.Document;

public class DomSegmentPrinter {

	private static final Logger logger = LogManager.getFormatterLogger(DomSegmentPrinter.class);

	private Document doc = null;

	public DomSegmentPrinter(Document doc) {
		this.doc = doc;

		logger.info("All known data segments : ");
	}

	public void process() {
		for (DomSegment seg : doc.domSegments()) {
      logger.debug("%s---------------------------------", seg.block().prettyName());
			logger.debug("%s\n", StringUtils.substring(seg.text(), 0, 150));
		}
	}
}
