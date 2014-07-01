package org.qiwur.scent.printer;

import org.apache.commons.lang.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.block.DomSegment;
import org.qiwur.scent.jsoup.nodes.Document;

public class DomSegmentPrinter {

	private static final Logger logger = LogManager.getFormatterLogger(DomSegmentPrinter.class);

	private Document doc = null;

	public DomSegmentPrinter(Document doc) {
		this.doc = doc;

		logger.info("All known data segments : ");
	}

	public void process() {
		for (DomSegment seg : doc.domSegments()) {
<<<<<<< HEAD
      logger.debug("%s---------------------------------", seg.block().prettyName());
=======
      logger.debug("%s---------------------------------", seg.root().prettyName());
>>>>>>> 5490cb6f167ceb113c47e20161e42d7d543e59bc
			logger.debug("%s\n", StringUtils.substring(seg.text(), 0, 150));
		}
	}
}
