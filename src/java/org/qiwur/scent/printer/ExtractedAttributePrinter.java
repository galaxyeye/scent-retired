package org.qiwur.scent.printer;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.qiwur.scent.jsoup.nodes.Document;

public class ExtractedAttributePrinter {

	private static final Logger logger = LogManager.getFormatterLogger(ExtractedAttributePrinter.class);

	Document doc = null;

	public ExtractedAttributePrinter(Document doc) {
		this.doc = doc;
	}

	public void process() {
	}
}
